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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


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
        // Icon can be null (optional)
        category.setIcon(categoryRequestDTO.icon());
        // If visibleInMenu is null, default to true
        Boolean visibleInMenu = categoryRequestDTO.visibleInMenu();
        category.setVisibleInMenu(visibleInMenu == null ? Boolean.TRUE : visibleInMenu);
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
    public Page<CategoryResponseDTO> getAllCategories(Pageable pageable) {
            Page<Category> categories = categoryRepository.findByBusiness_Id(employeeContext.getCurrentBusinessId(), pageable);
            return categories.map(categoryMapper::toDTO);
        }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) {
        Long businessId = employeeContext.getCurrentBusinessId();
        Category categoryToUpdate = categoryRepository.findByIdAndBusiness_Id(id, businessId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID " + id + " for the current business."));

        // Validate name uniqueness if changed
        if (!categoryRequestDTO.name().equals(categoryToUpdate.getName())) {
            if (categoryRepository.existsByNameAndBusiness_Id(categoryRequestDTO.name(), businessId)) {
                throw new com.progra3.cafeteria_api.exception.product.CategoryNameAlreadyExistsException(categoryRequestDTO.name());
            }
        }

        categoryToUpdate.setName(categoryRequestDTO.name());
        categoryToUpdate.setColor(categoryRequestDTO.color());

        // ✅ Icon: null ahora significa "borrar el icono"
        categoryToUpdate.setIcon(categoryRequestDTO.icon());

        // visibleInMenu sigue siendo opcional
        if (categoryRequestDTO.visibleInMenu() != null) {
            categoryToUpdate.setVisibleInMenu(categoryRequestDTO.visibleInMenu());
        }

        return categoryMapper.toDTO(categoryRepository.save(categoryToUpdate));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndBusiness_Id(id, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID " + id + " for the current business."));
        if (!category.getProducts().isEmpty()) {
            throw new CategoryCannotBeDeletedException("Cannot delete category with associated products.");
        }
        categoryRepository.delete(category);
    }
}
