package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.order.ItemNotFoundException;
import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemTransferRequestDTO;
import com.progra3.cafeteria_api.model.mapper.ItemMapper;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.repository.ItemRepository;
import com.progra3.cafeteria_api.service.port.IItemService;
import com.progra3.cafeteria_api.service.helper.ProductFinderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService implements IItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ProductFinderService productFinderService;
    private final StockService stockService;

    @Transactional
    @Override
    public Item createItem(Order order, ItemRequestDTO itemDTO) {
        Product product = productFinderService.getEntityById(itemDTO.productId());
        Item item = itemMapper.toEntity(itemDTO);
        item.setOrder(order);
        item.setProduct(product);
        item.setUnitPrice(product.getPrice());
        item.setDeleted(false);
        calculateTotalPrice(item);

        stockService.decreaseStockForItem(item);

        return item;
    }

    @Override
    public Item updateItem(Item itemToUpdate, ItemRequestDTO itemDTO) {
        itemToUpdate = itemMapper.updateItemFromDTO(itemDTO, itemToUpdate);
        calculateTotalPrice(itemToUpdate);
        return itemToUpdate;
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