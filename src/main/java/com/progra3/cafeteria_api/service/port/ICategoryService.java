package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.CategoryRequestDTO;
import com.progra3.cafeteria_api.model.dto.CategoryResponseDTO;
import com.progra3.cafeteria_api.model.entity.Category;

import java.util.List;

public interface ICategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO);
    CategoryResponseDTO getCategoryById(Long id);
    List<CategoryResponseDTO> getAllCategories();
    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO);
    void deleteCategory(Long id);

    Category getEntityById(Long id);
}
