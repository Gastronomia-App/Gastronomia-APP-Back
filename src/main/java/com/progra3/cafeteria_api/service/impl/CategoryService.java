package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.product.CategoryCannotBeDeletedException;
import com.progra3.cafeteria_api.exception.product.CategoryNotFoundException;
import com.progra3.cafeteria_api.model.dto.CategoryRequestDTO;
import com.progra3.cafeteria_api.model.dto.CategoryResponseDTO;
import com.progra3.cafeteria_api.model.entity.Category;
import com.progra3.cafeteria_api.model.mapper.CategoryMapper;
import com.progra3.cafeteria_api.repository.CategoryRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    private final EmployeeContext employeeContext;

    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        Category category = categoryMapper.toEntity(categoryRequestDTO);
        category.setBusiness(employeeContext.getCurrentBusiness());
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    public Category getEntityById(Long id) {
        return categoryRepository.findByIdAndBusiness_Id(id, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID " + id + " for the current business."));
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        return categoryRepository.findByIdAndBusiness_Id(id, employeeContext.getCurrentBusinessId())
                .map(categoryMapper::toDTO)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID " + id + " for the current business."));
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findByBusiness_Id(employeeContext.getCurrentBusinessId())
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) {
        Category categoryToUpdate = categoryRepository.findByIdAndBusiness_Id(id, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID " + id + " for the current business."));

        categoryToUpdate.setName(categoryRequestDTO.name());
        return categoryMapper.toDTO(categoryRepository.save(categoryToUpdate));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndBusiness_Id(id, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID " + id + " for the current business."));
        if (category.getProducts().stream().anyMatch(product -> !product.getDeleted())) {
            throw new CategoryCannotBeDeletedException("Cannot delete category with associated products.");
        }
        categoryRepository.delete(category);
    }
}
