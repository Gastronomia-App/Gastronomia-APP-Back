package com.progra3.cafeteria_api.service.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SortUtils {

    public Sort buildSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.unsorted();

        String[] parts = sortParam.trim().split(",");
        if (parts.length != 2) return Sort.unsorted();

        String property = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();

        try {
            return Sort.by(new Sort.Order(Sort.Direction.valueOf(direction), property));
        } catch (IllegalArgumentException e) {
            System.out.println("Dirección inválida: " + direction);
            return Sort.unsorted();
        }
    }
}