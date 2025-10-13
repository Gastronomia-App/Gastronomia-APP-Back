package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.product.NotEnoughStockException;
import com.progra3.cafeteria_api.model.entity.*;
import com.progra3.cafeteria_api.service.port.IStockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService implements IStockService {

    private final ProductService productService;
    private final ProductOptionService productOptionService;

    @Transactional
    @Override
    public void decreaseStockForItem(Item item) {
        Product product = item.getProduct();
        int quantity = item.getQuantity();

        if (product.isControlStock()) {
            checkStockAvailability(item);
        }

        switch (product.getCompositionType()) {
            case NONE -> handleNoneComposition(product, quantity);
            case SELECTABLE -> handleSelectableComposition(item, quantity);
            case FIXED -> handleFixedComposition(product, quantity);
            case FIXED_SELECTABLE -> handleFixedSelectableComposition(item, quantity);
        }
    }

    @Transactional
    @Override
    public void increaseStock(Product product, int quantity) {
        product.setStock(product.getStock() + quantity);
    }

    private void checkStockAvailability(Item item) {
        Product product = item.getProduct();
        int quantity = item.getQuantity();

        switch (product.getCompositionType()) {
            case NONE -> verifyNone(product, quantity);
            case SELECTABLE -> verifySelectable(item, quantity);
            case FIXED -> verifyFixed(product, quantity);
            case FIXED_SELECTABLE -> verifyFixedSelectable(item, quantity);
        }
    }

    private void handleNoneComposition(Product product, int quantity) {
        decreaseStock(product, quantity);
    }

    private void handleSelectableComposition(Item item, int quantity) {
        for (SelectedProductOption selectedOption : item.getSelectedOptions()) {
            ProductOption option = productOptionService.getEntityById(
                    selectedOption.getProductOption().getId());
            decreaseStock(option.getProduct(), selectedOption.getQuantity() * quantity);
        }
    }

    private void handleFixedComposition(Product product, int quantity) {
        for (ProductComponent component : product.getComponents()) {
            decreaseStock(component.getProduct(), component.getQuantity() * quantity);
        }
    }

    private void handleFixedSelectableComposition(Item item, int quantity) {
        handleFixedComposition(item.getProduct(), quantity);
        handleSelectableComposition(item, quantity);
    }

    private void verifyNone(Product product, int quantity) {
        verifyStock(product, quantity);
    }

    private void verifySelectable(Item item, int quantity) {
        for (SelectedProductOption selectedOption : item.getSelectedOptions()) {
            ProductOption option = productOptionService.getEntityById(
                    selectedOption.getProductOption().getId());
            verifyStock(option.getProduct(), selectedOption.getQuantity() * quantity);
        }
    }

    private void verifyFixed(Product product, int quantity) {
        for (ProductComponent component : product.getComponents()) {
            verifyStock(component.getProduct(), component.getQuantity() * quantity);
        }
    }

    private void verifyFixedSelectable(Item item, int quantity) {
        verifyFixed(item.getProduct(), quantity);
        verifySelectable(item, quantity);
    }

    private void verifyStock(Product product, int requiredQuantity) {
        if (product.getStock() < requiredQuantity) {
            throw new NotEnoughStockException(product.getId());
        }
    }

    private void decreaseStock(Product product, int quantity) {
        if (product.isControlStock()) {
            verifyStock(product, quantity);
            product.setStock(product.getStock() - quantity);
            productService.updateProduct(product);
        }
    }
}
