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
            case SELECTABLE, FIXED_SELECTABLE -> validateRecursiveRules(product, dto.quantity(), dto.selectedOptions(), context);
            case FIXED, NONE -> validateNoOptionsAllowed(dto.selectedOptions(), product.getCompositionType().name(), context);
        };
    }

    private boolean validateNoOptionsAllowed(List<SelectedOptionRequestDTO> selectedOptions, String type, ConstraintValidatorContext context) {
        if (selectedOptions != null && !selectedOptions.isEmpty()) {
            return addError(context, type + " products do not accept selected options.", "selectedOptions");
        }
        return true;
    }

    /**
     * Main validation method.
     */
    private boolean validateRecursiveRules(Product currentProduct, int parentQuantity, List<SelectedOptionRequestDTO> selectedOptions, ConstraintValidatorContext context) {
        Set<ProductGroup> groups = currentProduct.getProductGroups();

        // Validate 'Required' constraint (If groups exist but no options sent)
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return validateMandatoryGroups(currentProduct, groups, context);
        }

        // Load Definitions
        Map<Long, ProductOption> loadedOptionsMap = loadOptionsBulk(selectedOptions);

        // Aggregate Selections (Count how many of each group/option)
        SelectionAggregator aggregator = new SelectionAggregator();

        // Process each selection: Validate structure and Recursion
        for (SelectedOptionRequestDTO userSelection : selectedOptions) {
            Long optionId = userSelection.productOptionId();

            // Validate Existence & Belonging
            if (!loadedOptionsMap.containsKey(optionId)) {
                return addError(context, "Option ID " + optionId + " not found or invalid.", "selectedOptions");
            }
            ProductOption optionEntity = loadedOptionsMap.get(optionId);

            if (!isOptionValidForProduct(optionEntity, groups)) {
                return addError(context, "Option " + optionEntity.getProduct().getName() + " does not belong to product " + currentProduct.getName(), "selectedOptions");
            }

            // Accumulate Counts
            aggregator.add(optionEntity, userSelection.quantity());

            // Recursive Step (Drill Down)
            if (!validateRecursion(optionEntity, userSelection, context)) {
                return false;
            }
        }

        // Validate Math Constraints (Min/Max limits)
        return validateMathConstraints(groups, parentQuantity, aggregator, context);
    }

    // --- HELPER METHODS (The "How") ---

    private boolean validateMandatoryGroups(Product product, Set<ProductGroup> groups, ConstraintValidatorContext context) {
        boolean allGroupsOptional = groups.stream().allMatch(group -> group.getMinQuantity() == 0);
        if (!allGroupsOptional) {
            return addError(context, "Product '" + product.getName() + "' requires options to be selected.", "selectedOptions");
        }
        return true;
    }

    private Map<Long, ProductOption> loadOptionsBulk(List<SelectedOptionRequestDTO> selectedOptions) {
        List<Long> ids = selectedOptions.stream().map(SelectedOptionRequestDTO::productOptionId).toList();
        return productOptionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));
    }

    private boolean isOptionValidForProduct(ProductOption option, Set<ProductGroup> validGroups) {
        // Checks if the option's group is one of the valid groups for the parent product
        return validGroups.stream().anyMatch(g -> g.getId().equals(option.getProductGroup().getId()));
    }

    private boolean validateRecursion(ProductOption optionEntity, SelectedOptionRequestDTO userSelection, ConstraintValidatorContext context) {
        Product innerProduct = optionEntity.getProduct();
        return switch (innerProduct.getCompositionType()) {
            case SELECTABLE, FIXED_SELECTABLE -> validateRecursiveRules(innerProduct, userSelection.quantity(), userSelection.selectedOptions(), context);
            case FIXED, NONE -> validateNoOptionsAllowed(userSelection.selectedOptions(), "Inner product (" + innerProduct.getName() + ")", context);
        };
    }

    private boolean validateMathConstraints(Set<ProductGroup> groups, int parentQuantity, SelectionAggregator aggregator, ConstraintValidatorContext context) {
        for (ProductGroup group : groups) {
            int currentCount = aggregator.getGroupCount(group.getId());
            int requiredMin = group.getMinQuantity() * parentQuantity;
            int allowedMax = group.getMaxQuantity() * parentQuantity;

            if (currentCount < requiredMin || currentCount > allowedMax) {
                return addError(context, "Group '" + group.getName() + "' requires between " + requiredMin + " and " + allowedMax + " selections.", "selectedOptions");
            }

            // Validate max per individual option
            for (ProductOption optionDef : group.getOptions()) {
                int countPerOption = aggregator.getOptionCount(optionDef.getId());
                int optionMax = optionDef.getMaxQuantity() * parentQuantity;

                if (countPerOption > optionMax) {
                    return addError(context, "Option '" + optionDef.getProduct().getName() + "' can be selected at most " + optionMax + " times.", "selectedOptions");
                }
            }
        }
        return true;
    }

    private boolean addError(ConstraintValidatorContext context, String message, String field) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
        return false;
    }

    /**
     * Inner helper class to encapsulate the "Map Hell" logic.
     * Keeps the main validator clean and readable.
     */
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