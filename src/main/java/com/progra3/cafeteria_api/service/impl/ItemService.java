package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.order.ItemNotFoundException;
import com.progra3.cafeteria_api.exception.product.ProductOptionNotFoundException;
import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemTransferRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedOptionTransferRequestDTO;
import com.progra3.cafeteria_api.model.mapper.ItemMapper;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.SelectedOption;
import com.progra3.cafeteria_api.repository.ItemRepository;
import com.progra3.cafeteria_api.service.port.IItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
                // Re-link the new options to the existing item
                tempItem.getSelectedOptions().forEach(opt -> {
                    opt.setItem(updatedItem);
                    // Recursively fix root item references if needed, though Mapper handles it
                    updatedItem.addOption(opt);
                });
            }
        }

        calculateItemPrices(itemToUpdate);

        return itemRepository.save(itemToUpdate);
    }

    /**
     * Calculates Unit Price and Total Price based on the Product and Selected Options.
     * Logic: Item Unit Price = Product Base Price + Sum(Option Costs)
     */
    private void calculateItemPrices(Item item) {

        // Calculate total extra cost from options recursively
        double optionsTotalCost = 0.0;
        if (item.getSelectedOptions() != null) {
            optionsTotalCost = item.getSelectedOptions().stream()
                    .mapToDouble(this::calculateRecursiveOptionCost)
                    .sum();
        }

        double unitPrice = item.getProduct().getPrice();

        item.setUnitPrice(unitPrice);
        item.setTotalPrice(unitPrice * item.getQuantity() + optionsTotalCost);
    }

    /**
     * Recursive calculation for Option Tree.
     * Cost = (Self Price Increase + Sum(Children Costs)) * Quantity
     */
    private double calculateRecursiveOptionCost(SelectedOption option) {
        double optionCost = 0.0;

        if (option.getSelectedOptions() != null) {
            optionCost = option.getSelectedOptions().stream()
                    .mapToDouble(this::calculateRecursiveOptionCost)
                    .sum();
        }

        double priceIncrease = option.getProductOption().getPriceIncrease();

        return (priceIncrease + optionCost) * option.getQuantity();
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

        // Validation
        if (dto.quantity() > originalItem.getQuantity()) {
            throw new IllegalArgumentException("Cannot move more items than available");
        }

        // SCENARIO A: Full Transfer (Optimization)
        // If we are moving the TOTAL quantity, we simply switch the Order reference.
        if (dto.quantity().equals(originalItem.getQuantity())) {
            originalItem.setOrder(toOrder);
            return itemRepository.save(originalItem);
        }

        // SCENARIO B: Split Transfer (Extraction)
        // We are moving a partial quantity OR specific options
        return extractAndMoveItem(originalItem, toOrder, dto);
    }

    private Item extractAndMoveItem(Item sourceItem, Order targetOrder, ItemTransferRequestDTO dto) {
        // Decrease quantity on Source Root
        sourceItem.setQuantity(sourceItem.getQuantity() - dto.quantity());

        // Create Target Root Item
        Item newItem = Item.builder()
                .product(sourceItem.getProduct())
                .order(targetOrder)
                .quantity(dto.quantity())
                .comment(sourceItem.getComment())
                .deleted(false)
                .build();

        // Process Options Extraction (If any defined in DTO)
        if (dto.optionsToMove() != null) {
            for (SelectedOptionTransferRequestDTO optionDto : dto.optionsToMove()) {
                // Find the source option entity
                SelectedOption sourceOption = findOptionInList(sourceItem.getSelectedOptions(), optionDto.selectedOptionId());

                // Extract (Cut from source, paste to target)
                extractAndMoveOptionRecursive(sourceOption, newItem, null, optionDto);

                // CLEAN UP LOGIC: Remove option if quantity becomes 0
                if (sourceOption.getQuantity() <= 0) {
                    sourceItem.getSelectedOptions().remove(sourceOption);
                }
            }
        }

        // Recalculate Prices for BOTH items (Source modified, New created)
        // Note: Prices must be calculated for the new item before saving
        calculateItemPrices(newItem);
        Item savedNewItem = itemRepository.save(newItem);

        // CLEAN UP LOGIC: Handle Source Item deletion or update
        // (This block is theoretically reached only if quantity > 0 but options were stripped,
        // or if logic changes, but safeguard remains).
        if (sourceItem.getQuantity() <= 0) {
            itemRepository.delete(sourceItem);
        } else {
            calculateItemPrices(sourceItem);
            itemRepository.save(sourceItem);
        }

        return savedNewItem;
    }

    /**
     * Recursive method to Extract quantity from Source Option and Add to Target Option.
     */
    private void extractAndMoveOptionRecursive(SelectedOption sourceOption, Item newRootItem, SelectedOption newParentOption, SelectedOptionTransferRequestDTO dto) {

        // Validate extraction quantity
        if (dto.quantity() > sourceOption.getQuantity()) {
            throw new IllegalArgumentException("Cannot move more option quantity than available");
        }

        // Decrease Source
        sourceOption.setQuantity(sourceOption.getQuantity() - dto.quantity());

        // Create Target Option
        SelectedOption newOption = SelectedOption.builder()
                .item(newRootItem)
                .parentOption(newParentOption)
                .productOption(sourceOption.getProductOption())
                .quantity(dto.quantity()) // The moved quantity
                .selectedOptions(new ArrayList<>())
                .build();

        // Attach to hierarchy
        if (newParentOption != null) {
            newParentOption.addSelectedOption(newOption);
        } else {
            newRootItem.addOption(newOption);
        }

        // D. Handle Children Recursively
        if (dto.optionsToMove() != null) {
            for (SelectedOptionTransferRequestDTO optionDto : dto.optionsToMove()) {
                SelectedOption sourceChild = findOptionInList(sourceOption.getSelectedOptions(), optionDto.selectedOptionId());
                extractAndMoveOptionRecursive(sourceChild, newRootItem, newOption, optionDto);

                // CLEAN UP LOGIC: Remove child option if quantity becomes 0
                if (sourceChild.getQuantity() <= 0) {
                    sourceOption.getSelectedOptions().remove(sourceChild);
                }
            }
        }
    }

    /**
     * Helper to find an option by ID in a list
     */
    private SelectedOption findOptionInList(List<SelectedOption> list, Long id) {
        return list.stream()
                .filter(opt -> opt.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ProductOptionNotFoundException(id));
    }
}