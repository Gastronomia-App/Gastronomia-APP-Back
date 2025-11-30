package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.event.StockLowEvent;
import com.progra3.cafeteria_api.event.StockOutEvent;
import com.progra3.cafeteria_api.exception.product.NotEnoughStockException;
import com.progra3.cafeteria_api.model.entity.*;
import com.progra3.cafeteria_api.service.port.IStockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService implements IStockService {

    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Transactional
    @Override
    public void decreaseStockForItem(Item item) {
        // 1. Validation Phase (Read-Only)
        // We traverse the entire tree to ensure ALL components are available before modifying anything.
        validateStockAvailabilityRecursively(item);

        // 2. Execution Phase (Write)
        // If validation passed, we proceed to decrease stock.
        decreaseStockRecursively(item);
    }

    @Transactional
    @Override
    public void increaseStock(Product product, int quantity) {
        updateProductStock(product, -quantity); // Negative decrease = Increase
    }

    // =================================================================================
    // SECTION 1: VALIDATION LOGIC
    // =================================================================================

    private void validateStockAvailabilityRecursively(Item item) {
        Product rootProduct = item.getProduct();
        int itemQty = item.getQuantity();

        // 1. Validate Root Item
        verifyStock(rootProduct, itemQty);

        // 2. Validate Fixed Components (e.g., Bread, Meat defined in Product)
        if (rootProduct.getComponents() != null && !rootProduct.getComponents().isEmpty()) {
            validateFixedComponents(rootProduct, itemQty);
        }

        // 3. Validate Selected Options (Recursive Tree)
        if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
            validateOptionsTree(item.getSelectedOptions());
        }
    }

    private void validateFixedComponents(Product product, int parentQuantity) {
        for (ProductComponent component : product.getComponents()) {
            // Required Qty = Component definition * Parent Quantity
            int requiredQty = component.getQuantity() * parentQuantity;
            verifyStock(component.getProduct(), requiredQty);
        }
    }

    private void validateOptionsTree(List<SelectedOption> options) {
        for (SelectedOption option : options) {
            // 1. Validate the option product itself
            verifyStock(option.getProductOption().getProduct(), option.getQuantity());

            // 2. Recursion: Validate children of this option
            if (option.getSelectedOptions() != null && !option.getSelectedOptions().isEmpty()) {
                validateOptionsTree(option.getSelectedOptions());
            }
        }
    }

    private void verifyStock(Product product, int requiredQuantity) {
        if (product.isControlStock() && product.getStock() < requiredQuantity) {
            throw new NotEnoughStockException(product.getId());
        }
    }

    // =================================================================================
    // SECTION 2: EXECUTION LOGIC
    // =================================================================================

    private void decreaseStockRecursively(Item item) {
        Product rootProduct = item.getProduct();
        int itemQty = item.getQuantity();

        // 1. Decrease Root Item
        processStockUpdate(rootProduct, itemQty);

        // 2. Decrease Fixed Components
        if (rootProduct.getComponents() != null && !rootProduct.getComponents().isEmpty()) {
            processFixedComponents(rootProduct, itemQty);
        }

        // 3. Decrease Selected Options (Recursive Tree)
        if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
            processOptionsTree(item.getSelectedOptions());
        }
    }

    private void processFixedComponents(Product product, int parentQuantity) {
        for (ProductComponent component : product.getComponents()) {
            int requiredQty = component.getQuantity() * parentQuantity;
            processStockUpdate(component.getProduct(), requiredQty);
        }
    }

    private void processOptionsTree(List<SelectedOption> options) {
        for (SelectedOption option : options) {
            // 1. Decrease this option
            processStockUpdate(option.getProductOption().getProduct(), option.getQuantity());

            // 2. Recursion
            if (option.getSelectedOptions() != null && !option.getSelectedOptions().isEmpty()) {
                processOptionsTree(option.getSelectedOptions());
            }
        }
    }

    /**
     * Core method to update DB and fire events.
     */
    private void processStockUpdate(Product product, int quantityToDecrease) {
        if (!product.isControlStock()) return;

        int previousStock = product.getStock();

        // Safety check (redundant if validated, but good for concurrency safety)
        if (previousStock < quantityToDecrease) {
            throw new NotEnoughStockException(product.getId());
        }

        int newStock = previousStock - quantityToDecrease;

        // Update Entity
        product.setStock(newStock);
        productService.updateProduct(product);

        // Notify
        checkStockLevels(product, previousStock, newStock);
    }

    // =================================================================================
    // SECTION 3: NOTIFICATIONS & HELPERS
    // =================================================================================

    /**
     * Helper to reuse increase/decrease logic
     */
    private void updateProductStock(Product product, int quantityToSubtract) {
        int previousStock = product.getStock();
        int newStock = previousStock - quantityToSubtract;

        product.setStock(newStock);
        productService.updateProduct(product);

        // Only check alerts when decreasing
        if (quantityToSubtract > 0) {
            checkStockLevels(product, previousStock, newStock);
        }
    }

    private void checkStockLevels(Product product, int previousStock, int newStock) {
        Long businessId = product.getBusiness().getId();

        if (newStock == 0 && previousStock > 0) {
            eventPublisher.publishEvent(new StockOutEvent(product, businessId));
        } else if (newStock > 0 && newStock <= LOW_STOCK_THRESHOLD && previousStock > LOW_STOCK_THRESHOLD) {
            eventPublisher.publishEvent(new StockLowEvent(product, businessId));
        }
    }
}