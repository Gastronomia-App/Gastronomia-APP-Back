package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.order.ItemNotFoundException;
import com.progra3.cafeteria_api.exception.product.ProductOptionNotFoundException;
import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemTransferRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedProductOptionRequestDTO;
import com.progra3.cafeteria_api.model.mapper.ItemMapper;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.SelectedProductOption;
import com.progra3.cafeteria_api.repository.ItemRepository;
import com.progra3.cafeteria_api.repository.ProductOptionRepository;
import com.progra3.cafeteria_api.service.port.IItemService;
import com.progra3.cafeteria_api.service.helper.ProductFinderService;
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
    private final ProductFinderService productFinderService;
    private final StockService stockService;
    private final ProductOptionRepository productOptionRepository;

    @Transactional
    @Override
    public Item createItem(Order order, ItemRequestDTO itemDTO) {
        Product product = productFinderService.getEntityById(itemDTO.productId());
        Item item = itemMapper.toEntity(itemDTO);
        item.setOrder(order);
        item.setProduct(product);
        item.setUnitPrice(product.getPrice());
        item.setDeleted(false);

        // Manejar selectedOptions
        if (itemDTO.selectedOptions() != null && !itemDTO.selectedOptions().isEmpty()) {
            item.setSelectedOptions(convertSelectedOptions(itemDTO.selectedOptions()));
        }

        calculateTotalPrice(item);

        stockService.decreaseStockForItem(item);

        return item;
    }

    @Override
    public Item updateItem(Item itemToUpdate, ItemRequestDTO itemDTO) {
        itemToUpdate = itemMapper.updateItemFromDTO(itemDTO, itemToUpdate);

        // Actualizar selectedOptions
        if (itemDTO.selectedOptions() != null) {
            itemToUpdate.getSelectedOptions().clear();
            if (!itemDTO.selectedOptions().isEmpty()) {
                itemToUpdate.setSelectedOptions(convertSelectedOptions(itemDTO.selectedOptions()));
            }
        }

        calculateTotalPrice(itemToUpdate);
        return itemToUpdate;
    }

    private List<SelectedProductOption> convertSelectedOptions(List<SelectedProductOptionRequestDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }

        return dtos.stream()
                .map(dto -> SelectedProductOption.builder()
                        .productOption(productOptionRepository.findById(dto.productOptionId())
                                .orElseThrow(() -> new ProductOptionNotFoundException(dto.productOptionId())))
                        .quantity(dto.quantity())
                        .build())
                .toList();
    }

    @Override
    public List<Item> transferItems(Order fromOrder, Order toOrder, List<ItemTransferRequestDTO> itemsToMove) {
        return itemsToMove.stream()
                .map(dto -> transferItem(fromOrder, toOrder, dto))
                .toList();
    }

    private void validateTransferQuantity(int quantityToMove, int availableQuantity) {
        if (quantityToMove < 1 || quantityToMove > availableQuantity) {
            throw new IllegalArgumentException("Transfer quantity must be between 1 and " + availableQuantity);
        }
    }

    private Item transferItem(Order fromOrder, Order toOrder, ItemTransferRequestDTO dto) {
        int quantityToTransfer = dto.quantity();

        Item originalItem = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new ItemNotFoundException(dto.itemId()));

        validateTransferQuantity(quantityToTransfer, originalItem.getQuantity());

        return transferOrSplitItem(originalItem, toOrder, quantityToTransfer, fromOrder);
    }

    private Item transferOrSplitItem(Item originalItem, Order toOrder, int quantityToTransfer, Order fromOrder) {

        if (quantityToTransfer == originalItem.getQuantity()) {
            fromOrder.getItems().remove(originalItem);
            itemRepository.delete(originalItem);
        } else {
            originalItem.setQuantity(originalItem.getQuantity() - quantityToTransfer);
            calculateTotalPrice(originalItem);
        }

        return Item.builder()
                .product(originalItem.getProduct())
                .order(toOrder)
                .comment(originalItem.getComment())
                .unitPrice(originalItem.getUnitPrice())
                .quantity(quantityToTransfer)
                .totalPrice(originalItem.getUnitPrice() * quantityToTransfer)
                .deleted(false)
                .build();
    }

    private void calculateTotalPrice(Item item) {
        item.setTotalPrice(item.getUnitPrice() * item.getQuantity());
    }
}