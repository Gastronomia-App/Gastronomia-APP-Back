package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.product.ProductNameAlreadyExistsException;
import com.progra3.cafeteria_api.exception.product.ProductNotFoundException;
import com.progra3.cafeteria_api.model.dto.ProductComponentRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.mapper.ProductComponentMapper;
import com.progra3.cafeteria_api.model.mapper.ProductMapper;
import com.progra3.cafeteria_api.model.entity.Category;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductComponent;
import com.progra3.cafeteria_api.model.entity.ProductGroup;
import com.progra3.cafeteria_api.repository.ProductRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.IProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.progra3.cafeteria_api.model.enums.CompositionType.*;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;

    private final EmployeeContext employeeContext;
    private final CategoryService categoryService;
    private final ProductGroupService productGroupService;
    private final ProductComponentMapper productComponentMapper;

    private final ProductMapper productMapper;

    @Transactional
    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        Category category = categoryService.getEntityById(productRequestDTO.categoryId());
        Product product = productMapper.toEntity(productRequestDTO);
        product.setBusiness(employeeContext.getCurrentBusiness());
        product.setCategory(category);

        updateProductRelationships(product, productRequestDTO);

        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findByIdAndBusiness_IdWithComponents(id, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toDTO(product);
    }

    @Override
    public Page<ProductResponseDTO> getProducts(String name, Long categoryId, Integer minStock, Integer maxStock, Boolean composite, Pageable pageable) {
        Page<Product> products = productRepository.findByBusiness_Id(
                name,
                categoryId,
                minStock,
                maxStock,
                composite,
                employeeContext.getCurrentBusinessId(),
                pageable);

        return products.map(productMapper::toDTO);
    }

    @Transactional
    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Product product = getEntityById(id);
        Long businessId = employeeContext.getCurrentBusinessId();

        // Validate name uniqueness if changed
        if (!productRequestDTO.name().equals(product.getName())) {
            if (productRepository.existsByNameAndBusiness_Id(productRequestDTO.name(), businessId)) {
                throw new ProductNameAlreadyExistsException(productRequestDTO.name());
            }
        }

        Category category = categoryService.getEntityById(productRequestDTO.categoryId());

        productMapper.updateProductFromDTO(product, productRequestDTO);
        product.setCategory(category);

        return productMapper.toDTO(productRepository.save(product));
    }

    @Transactional
    @Override
    public ProductResponseDTO updateProduct(Product updatedProduct) {
        return productMapper.toDTO(productRepository.save(updatedProduct));
    }

    @Transactional
    @Override
    public ProductResponseDTO deleteProduct(Long id) {
        Product product = getEntityById(id);
        ProductResponseDTO productDTO = productMapper.toDTO(product);

        // 1. Clear ProductGroups (ManyToMany relationship)
        product.getProductGroups().forEach(group -> group.getUsedByProducts().remove(product));
        product.getProductGroups().clear();

        // 2. Clear usedInProductOptions (will cascade delete due to orphanRemoval = true)
        product.getUsedInProductOptions().clear();

        // 3. Clear usedInProducts (ProductComponents where this product is used)
        product.getUsedInProducts().clear();

        // 4. Delete the product (components will be deleted automatically due to orphanRemoval)
        productRepository.delete(product);

        return productDTO;
    }

    @Override
    public Product getEntityById(Long productId) {
        return productRepository.findByIdAndBusiness_IdWithComponents(productId, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Transactional
    @Override
    public ProductResponseDTO assignGroupToProduct(Long productId, Long groupId) {
        Product product = getEntityById(productId);
        ProductGroup group = productGroupService.getEntityById(groupId);

        product.getProductGroups().add(group);
        adjustComposite(product);

        return productMapper.toDTO(productRepository.save(product));
    }

    @Transactional
    @Override
    public ProductResponseDTO removeGroupFromProduct(Long productId, Long groupId) {
        Product product = getEntityById(productId);
        ProductGroup group = productGroupService.getEntityById(groupId);

        product.getProductGroups().remove(group);
        adjustComposite(product);

        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO addComponentsToProduct(Long productId, List<ProductComponentRequestDTO> dtos) {
        Product product = getEntityById(productId);
        dtos.forEach(dto -> addComponent(product, dto));
        adjustComposite(product);
        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO updateProductComponent(Long productId, Long componentId, Integer quantity) {
        Product product = getEntityById(productId);
        product.getComponents().stream()
                .filter(component -> component.getId().equals(componentId))
                .findFirst()
                .ifPresent(component -> component.setQuantity(quantity));

        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO removeComponentFromProduct(Long productId, Long componentId) {
        Product product = getEntityById(productId);
        product.getComponents().removeIf(component -> component.getId().equals(componentId));
        adjustComposite(product);

        return productMapper.toDTO(productRepository.save(product));
    }

    private void updateProductRelationships(Product product, ProductRequestDTO productRequestDTO) {
        updateComponents(product, productRequestDTO.components());
        updateProductGroups(product, productRequestDTO.productGroups());
        adjustComposite(product);
    }

    private void updateComponents(Product product, List<ProductComponentRequestDTO> componentDTOs) {
        if (componentDTOs == null) {
            componentDTOs = java.util.Collections.emptyList();
        }

        java.util.Set<Long> requestedIds = componentDTOs.stream()
            .map(ProductComponentRequestDTO::id)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());

        // Remove components that were not sent (deleted)
        product.getComponents().removeIf(component -> !requestedIds.contains(component.getId()));

        // Update or create components
        for (ProductComponentRequestDTO dto : componentDTOs) {
            if (dto.id() != null) {
                // Update existing component
                updateExistingComponent(product, dto);
            } else {
                // Create new component
                createNewComponent(product, dto);
            }
        }
    }

    private void updateExistingComponent(Product product, ProductComponentRequestDTO dto) {
        product.getComponents().stream()
            .filter(comp -> comp.getId().equals(dto.id()))
            .findFirst()
            .ifPresent(component -> {
                component.setQuantity(dto.quantity());
                // Update product reference if changed
                if (!component.getProduct().getId().equals(dto.productId())) {
                    Product newChildProduct = getEntityById(dto.productId());
                    component.setProduct(newChildProduct);
                }
            });
    }

    private void createNewComponent(Product product, ProductComponentRequestDTO dto) {
        Product childProduct = getEntityById(dto.productId());
        ProductComponent newComponent = productComponentMapper.toEntity(dto);
        newComponent.setProduct(childProduct);
        newComponent.setParentProduct(product);
        product.getComponents().add(newComponent);
    }

    private void updateProductGroups(Product product, List<Long> groupIds) {
        if (groupIds == null) {
            groupIds = java.util.Collections.emptyList();
        }

        java.util.Set<Long> requestedIds = new java.util.HashSet<>(groupIds);

        // Remove groups that were not sent (deleted)
        product.getProductGroups().removeIf(group -> !requestedIds.contains(group.getId()));

        // Add new groups
        java.util.Set<Long> currentIds = product.getProductGroups().stream()
            .map(ProductGroup::getId)
            .collect(java.util.stream.Collectors.toSet());

        groupIds.stream()
            .filter(id -> !currentIds.contains(id))
            .forEach(id -> {
                ProductGroup group = productGroupService.getEntityById(id);
                product.getProductGroups().add(group);
            });
    }

    private void addComponent(Product parentProduct, ProductComponentRequestDTO dto) {
        Product childProduct = getEntityById(dto.productId());
        ProductComponent component = productComponentMapper.toEntity(dto);
        component.setProduct(childProduct);
        component.setParentProduct(parentProduct);
        parentProduct.getComponents().add(component);
    }

    private void adjustComposite(Product product) {
        boolean hasGroups = !product.getProductGroups().isEmpty();
        boolean hasComponents = !product.getComponents().isEmpty();

        if (hasGroups && hasComponents) {
            product.setCompositionType(FIXED_SELECTABLE);
        } else if (hasGroups) {
            product.setCompositionType(SELECTABLE);
        } else if (hasComponents) {
            product.setCompositionType(FIXED);
        } else {
            product.setCompositionType(NONE);
        }

        product.setComposite(hasGroups || hasComponents);
    }


}
