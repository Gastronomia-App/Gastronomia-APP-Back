package com.progra3.cafeteria_api.validation;

import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedOptionRequestDTO;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductGroup;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import com.progra3.cafeteria_api.repository.ProductOptionRepository;
import com.progra3.cafeteria_api.service.helper.ProductFinderService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ValidItemRequestValidator implements ConstraintValidator<ValidItemRequest, ItemRequestDTO> {

    private final ProductFinderService productFinderService;
    private final ProductOptionRepository productOptionRepository;

    @Override
    public boolean isValid(ItemRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.productId() == null) return false;

        Product product = productFinderService.getEntityById(dto.productId());

        return switch (product.getCompositionType()) {
            case SELECTABLE, FIXED_SELECTABLE -> validateCompositeItemRoot(product, dto, context);
            case FIXED, NONE -> validateSimpleItem(dto, product.getCompositionType().name(), context);
        };
    }

    private boolean validateSimpleItem(ItemRequestDTO dto, String type, ConstraintValidatorContext context) {
        if (dto.selectedOptions() != null && !dto.selectedOptions().isEmpty()) {
            return addError(context, type + " products do not accept selected options.", "selectedOptions");
        }
        return true;
    }

    private boolean validateCompositeItemRoot(Product product, ItemRequestDTO dto, ConstraintValidatorContext context) {
        // Check if item has configuration
        boolean hasConfiguration = dto.selectedOptions() != null && !dto.selectedOptions().isEmpty();

        // RULE 1: Root Composite items WITH configuration must have quantity = 1
        // Items WITHOUT configuration can have quantity > 1 (uniform instances)
        if (hasConfiguration && dto.quantity() != 1) {
            return addError(context, "Composite products with configuration must have quantity = 1. Add multiple items for different quantities.", "quantity");
        }

        // If no configuration, validate only mandatory groups
        if (!hasConfiguration) {
            return validateMandatoryGroups(product, product.getProductGroups(), context);
        }

        // If has configuration, validate recursively
        return validateRecursiveRules(product, dto.selectedOptions(), context);
    }

    /**
     * Recursive validation logic.
     */
    private boolean validateRecursiveRules(Product currentProduct, List<SelectedOptionRequestDTO> selectedOptions, ConstraintValidatorContext context) {
        Set<ProductGroup> groups = currentProduct.getProductGroups();

        // 1. Check Mandatory Groups
        List<SelectedOptionRequestDTO> safeOptions = selectedOptions != null ? selectedOptions : Collections.emptyList();
        if (safeOptions.isEmpty()) {
            return validateMandatoryGroups(currentProduct, groups, context);
        }

        // 2. Load Options
        Map<Long, ProductOption> loadedOptionsMap = loadOptionsBulk(safeOptions);
        SelectionAggregator aggregator = new SelectionAggregator();

        // 3. Process Selections
        for (SelectedOptionRequestDTO userSelection : safeOptions) {
            Long optionId = userSelection.productOptionId();

            if (!loadedOptionsMap.containsKey(optionId)) {
                return addError(context, "Option ID " + optionId + " not found.", "selectedOptions");
            }
            ProductOption optionEntity = loadedOptionsMap.get(optionId);

            if (!isOptionValidForProduct(optionEntity, groups)) {
                return addError(context, "Option is invalid for product " + currentProduct.getName(), "selectedOptions");
            }

            aggregator.add(optionEntity, userSelection.quantity());

            // Recursion & Nested Quantity Check
            Product innerProduct = optionEntity.getProduct();
            switch (innerProduct.getCompositionType()) {
                case SELECTABLE, FIXED_SELECTABLE:
                    // RULE 2: Nested Composite Options MUST also have quantity = 1.
                    if (userSelection.quantity() != 1) {
                        return addError(context, "Composite option '" + innerProduct.getName() + "' must have quantity = 1.", "selectedOptions");
                    }

                    // Continue recursion
                    if (!validateRecursiveRules(innerProduct, userSelection.selectedOptions(), context)) {
                        return false;
                    }
                    break;
                case FIXED, NONE:
                    // Simple options CAN have quantity > 1.
                    if (userSelection.selectedOptions() != null && !userSelection.selectedOptions().isEmpty()) {
                        return addError(context, "Simple option '" + innerProduct.getName() + "' cannot have children.", "selectedOptions");
                    }
                    break;
            }
        }

        // 4. Validate Math (parentQuantity is implicit 1 for composites due to the checks above)
        return validateMathConstraints(groups, 1, aggregator, context);
    }

    private boolean validateMathConstraints(Set<ProductGroup> groups, int parentQuantity, SelectionAggregator aggregator, ConstraintValidatorContext context) {
        for (ProductGroup group : groups) {
            int currentCount = aggregator.getGroupCount(group.getId());
            int requiredMin = group.getMinQuantity() * parentQuantity;
            int allowedMax = group.getMaxQuantity() * parentQuantity;

            if (currentCount < requiredMin || currentCount > allowedMax) {
                return addError(context, "Group '" + group.getName() + "' requires " + requiredMin + "-" + allowedMax + " selections.", "selectedOptions");
            }

            for (ProductOption optionDef : group.getOptions()) {
                int countPerOption = aggregator.getOptionCount(optionDef.getId());
                int optionMax = optionDef.getMaxQuantity() * parentQuantity;

                if (countPerOption > optionMax) {
                    return addError(context, "Option '" + optionDef.getProduct().getName() + "' max allowed is " + optionMax, "selectedOptions");
                }
            }
        }
        return true;
    }

    // --- HELPERS ---

    private boolean validateMandatoryGroups(Product product, Set<ProductGroup> groups, ConstraintValidatorContext context) {
        boolean allGroupsOptional = groups.stream().allMatch(group -> group.getMinQuantity() == 0);
        if (!allGroupsOptional) {
            return addError(context, "Product '" + product.getName() + "' requires options to be selected.", "selectedOptions");
        }
        return true;
    }

    private Map<Long, ProductOption> loadOptionsBulk(List<SelectedOptionRequestDTO> selectedOptions) {
        if (selectedOptions == null || selectedOptions.isEmpty()) return Collections.emptyMap();
        List<Long> ids = selectedOptions.stream().map(SelectedOptionRequestDTO::productOptionId).toList();
        return productOptionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));
    }

    private boolean isOptionValidForProduct(ProductOption option, Set<ProductGroup> validGroups) {
        return validGroups.stream().anyMatch(g -> g.getId().equals(option.getProductGroup().getId()));
    }

    private boolean addError(ConstraintValidatorContext context, String message, String field) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
        return false;
    }

    private static class SelectionAggregator {
        private final Map<Long, Integer> countPerGroup = new HashMap<>();
        private final Map<Long, Integer> countPerOption = new HashMap<>();

        void add(ProductOption option, int quantity) {
            countPerGroup.merge(option.getProductGroup().getId(), quantity, Integer::sum);
            countPerOption.merge(option.getId(), quantity, Integer::sum);
        }

        int getGroupCount(Long groupId) {
            return countPerGroup.getOrDefault(groupId, 0);
        }

        int getOptionCount(Long optionId) {
            return countPerOption.getOrDefault(optionId, 0);
        }
    }
}