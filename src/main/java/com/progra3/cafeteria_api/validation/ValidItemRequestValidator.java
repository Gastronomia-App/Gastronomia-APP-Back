package com.progra3.cafeteria_api.validation;

import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedProductOptionRequestDTO;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductGroup;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import com.progra3.cafeteria_api.service.helper.ProductFinderService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ValidItemRequestValidator implements ConstraintValidator<ValidItemRequest, ItemRequestDTO> {

    private final ProductFinderService productFinderService;

    @Override
    public boolean isValid(ItemRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.productId() == null) return false;

        Product product = productFinderService.getEntityById(dto.productId());

        return switch (product.getCompositionType()) {
            case SELECTABLE, FIXED_SELECTABLE -> validateSelectable(dto, product, context);
            case FIXED      -> validateFixed(dto, context);
            case NONE       -> validateNone(dto, context);
        };
    }

    private boolean validateSelectable(ItemRequestDTO dto, Product product, ConstraintValidatorContext context) {
        if (dto.quantity() != 1) {
            return error(context, "Composite products with selectable options must be added one at a time.", "quantity");
        }

        return validateOptionGroups(dto, product, context);
    }

    private boolean validateFixed(ItemRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.selectedOptions() != null && !dto.selectedOptions().isEmpty()) {
            return error(context, "Fixed-composition products do not accept selected options.", "selectedOptions");
        }
        return true;
    }

    private boolean validateNone(ItemRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.selectedOptions() != null && !dto.selectedOptions().isEmpty()) {
            return error(context, "Simple products do not accept selected options.", "selectedOptions");
        }
        return true;
    }

    private boolean validateOptionGroups(ItemRequestDTO dto, Product product, ConstraintValidatorContext context) {
        Set<ProductGroup> groups = product.getProductGroups();

        boolean allGroupsOptional = groups.stream()
                .allMatch(group -> group.getMinQuantity() == 0);

        boolean hasSelectedOptions = dto.selectedOptions() != null && !dto.selectedOptions().isEmpty();

        if (!hasSelectedOptions) {
            if (!allGroupsOptional) {
                return error(context, "This product requires at least one option to be selected.", "selectedOptions");
            }
            return true;
        }

        Map<Long, Long> optionToGroupMap = groups.stream()
                .flatMap(group -> group.getOptions().stream()
                        .map(option -> Map.entry(option.getId(), group.getId())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Long, Integer> selectedCountPerGroup = new HashMap<>();
        Map<Long, Integer> selectedCountPerOption = new HashMap<>();

        for (SelectedProductOptionRequestDTO option : dto.selectedOptions()) {

            Long productOptionId = option.productOptionId();
            Integer quantity = option.quantity();

            if (!optionToGroupMap.containsKey(productOptionId)) {
                return error(context, "Invalid option ID: " + productOptionId, "selectedOptions");
            }

            Long groupId = optionToGroupMap.get(productOptionId);
            selectedCountPerGroup.merge(groupId, quantity, Integer::sum);
            selectedCountPerOption.merge(productOptionId, quantity, Integer::sum);
        }
        for (ProductGroup group : groups) {
            int count = selectedCountPerGroup.getOrDefault(group.getId(), 0);
            if (count < group.getMinQuantity() || count > group.getMaxQuantity()) {
                return error(context, "Group '" + group.getName() + "' requires between " + group.getMinQuantity() +
                        " and " + group.getMaxQuantity() + " selections.", "selectedOptions");
            }

            for (ProductOption option : group.getOptions()) {
                int countPerOption = selectedCountPerOption.getOrDefault(option.getId(), 0);
                if (countPerOption > option.getMaxQuantity()) {
                    return error(context, "Option '" + option.getProduct().getName() +
                            "' in group '" + group.getName() + "' can be selected at most " + option.getMaxQuantity() + " times.", "selectedOptions");
                }
            }
        }

        return true;
    }

    private boolean error(ConstraintValidatorContext context, String message, String field) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
        return false;
    }
}

