package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Product;
import jakarta.transaction.Transactional;

public interface IStockService {
    @Transactional
    void decreaseStockForItem(Item item);

    @Transactional
    void increaseStock(Product product, int quantity);
}
