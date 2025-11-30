package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.order.ItemNotFoundException;
import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemTransferRequestDTO;
import com.progra3.cafeteria_api.model.mapper.ItemMapper;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.SelectedOption;
import com.progra3.cafeteria_api.repository.ItemRepository;
import com.progra3.cafeteria_api.service.port.IItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService implements IItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final StockService stockService;

    @Transactional
    @Override
    public Item createItem(Order order, ItemRequestDTO itemDTO) {
        Item item = itemMapper.toEntity(itemDTO);
        item.setOrder(order);
        calculateItemPrices(item);
        item = itemRepository.save(item);
        stockService.decreaseStockForItem(item);
        return item;
    }

    @Transactional
    @Override
    public Item updateItem(Item itemToUpdate, ItemRequestDTO itemDTO) {
        Item updatedItem = itemMapper.updateItemFromDTO(itemDTO, itemToUpdate);

        if (itemDTO.selectedOptions() != null) {
            updatedItem.getSelectedOptions().clear();
            Item tempItem = itemMapper.toEntity(itemDTO);
            if (tempItem.getSelectedOptions() != null) {
                tempItem.getSelectedOptions().forEach(opt -> {
                    opt.setItem(updatedItem);
                    updatedItem.addOption(opt);
                });
            }
        }
        calculateItemPrices(itemToUpdate);
        return itemRepository.save(itemToUpdate);
    }

    @Transactional
    @Override
    public List<Item> transferItems(Order fromOrder, Order toOrder, List<ItemTransferRequestDTO> itemsToMove) {
        return itemsToMove.stream()
                .map(dto -> transferSingleItem(toOrder, dto))
                .toList();
    }

    private Item transferSingleItem(Order toOrder, ItemTransferRequestDTO dto) {
        Item originalItem = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new ItemNotFoundException(dto.itemId()));

        // Validations
        if (dto.quantity() > originalItem.getQuantity()) {
            throw new IllegalArgumentException("Cannot move more items than available");
        }

        boolean hasOptions = originalItem.getSelectedOptions() != null && !originalItem.getSelectedOptions().isEmpty();
        boolean isPartialMove = !dto.quantity().equals(originalItem.getQuantity());

        // RULE CHECK: Composite Items (with options) MUST be moved entirely because Quantity is always 1.
        if (hasOptions && isPartialMove) {
            throw new IllegalArgumentException("Composite items cannot be split partially. Move the entire item.");
        }

        // SCENARIO A: Full Transfer (Composite or Simple)
        // Optimized: Just switch the Order owner.
        if (!isPartialMove) {
            originalItem.setOrder(toOrder);
            return itemRepository.save(originalItem);
        }

        // SCENARIO B: Partial Split (ONLY for Simple Items)
        // Since it's a simple item, we don't need complex recursive option splitting logic.
        return splitSimpleItem(originalItem, toOrder, dto.quantity());
    }

    private Item splitSimpleItem(Item sourceItem, Order targetOrder, Integer quantityToMove) {
        // 1. Decrease Source
        sourceItem.setQuantity(sourceItem.getQuantity() - quantityToMove);
        calculateItemPrices(sourceItem); // Recalculate total for source
        itemRepository.save(sourceItem);

        // 2. Create Target
        Item newItem = Item.builder()
                .product(sourceItem.getProduct())
                .order(targetOrder)
                .quantity(quantityToMove)
                .comment(sourceItem.getComment())
                .deleted(false)
                .unitPrice(sourceItem.getUnitPrice())
                .build();

        // Calculate total for new item
        calculateItemPrices(newItem);

        return itemRepository.save(newItem);
    }

    private void calculateItemPrices(Item item) {
        double optionsTotalCost = 0.0;

        if (item.getSelectedOptions() != null) {
            optionsTotalCost = item.getSelectedOptions().stream()
                    .mapToDouble(this::calculateRecursiveOptionCost)
                    .sum();
        }

        double unitPrice = item.getProduct().getPrice();
        item.setUnitPrice(unitPrice + optionsTotalCost);

        // Total = (Base + Options) * Qty
        item.setTotalPrice(unitPrice * item.getQuantity());
    }

    private double calculateRecursiveOptionCost(SelectedOption option) {
        double optionCost = 0.0;
        if (option.getSelectedOptions() != null) {
            optionCost = option.getSelectedOptions().stream()
                    .mapToDouble(this::calculateRecursiveOptionCost)
                    .sum();
        }
        return (option.getProductOption().getPriceIncrease() + optionCost) * option.getQuantity();
    }
}