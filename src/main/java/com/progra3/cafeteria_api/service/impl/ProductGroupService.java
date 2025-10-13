package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.product.ProductGroupCannotBeDeletedException;
import com.progra3.cafeteria_api.exception.product.ProductGroupNotFoundException;
import com.progra3.cafeteria_api.exception.product.ProductOptionNotFoundException;
import com.progra3.cafeteria_api.model.dto.ProductOptionRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.dto.ProductGroupRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductGroupResponseDTO;
import com.progra3.cafeteria_api.model.mapper.ProductGroupMapper;
import com.progra3.cafeteria_api.model.mapper.ProductOptionMapper;
import com.progra3.cafeteria_api.model.entity.ProductGroup;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import com.progra3.cafeteria_api.repository.ProductGroupRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.IProductGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductGroupService implements IProductGroupService {

    private final ProductGroupRepository productGroupRepository;

    private final EmployeeContext employeeContext;
    private final ProductOptionService productOptionService;

    private final ProductGroupMapper productGroupMapper;
    private final ProductOptionMapper productOptionMapper;

    @Override
    public ProductGroupResponseDTO createProductGroup(ProductGroupRequestDTO dto) {
        ProductGroup productGroup = productGroupMapper.toEntity(dto);
        productGroup.setBusiness(employeeContext.getCurrentBusiness());

        return productGroupMapper.toDTO(productGroupRepository.save(productGroup));
    }

    @Override
    public ProductGroup getEntityById(Long productGroupId) {
        return productGroupRepository.findByIdAndBusiness_Id(productGroupId, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new ProductGroupNotFoundException(productGroupId));
    }

    @Override
    public ProductGroupResponseDTO getProductGroupById(Long productGroupId) {
        ProductGroup productGroup = getEntityById(productGroupId);

        return productGroupMapper.toDTO(productGroup);
    }

    @Override
    public List<ProductGroupResponseDTO> getAllProductGroups() {
        List<ProductGroup> productGroups = productGroupRepository.findByBusiness_Id(employeeContext.getCurrentBusinessId());

        return productGroupMapper.toDTOList(productGroups);
    }

    @Override
    public ProductGroupResponseDTO updateProductGroup(Long productGroupId, ProductGroupRequestDTO dto) {
        ProductGroup productGroup = getEntityById(productGroupId);

        productGroup = productGroupMapper.updateProductGroupFromDTO(productGroup, dto);

        return productGroupMapper.toDTO(productGroupRepository.save(productGroup));
    }

    @Override
    public void deleteProductGroup(Long productGroupId) {
        ProductGroup productGroup = getEntityById(productGroupId);

        if (!productGroup.getUsedByProducts().isEmpty()) {
            throw new ProductGroupCannotBeDeletedException(productGroupId);
        }

        productGroupRepository.delete(productGroup);
    }

    @Override
    public List<ProductOptionResponseDTO> addProductOptions(Long id, List<ProductOptionRequestDTO> dtos) {
        ProductGroup productGroup = getEntityById(id);

        return dtos.stream()
                .map(dto -> addProductOption(productGroup, dto))
                .toList();
    }

    @Override
    public List<ProductOptionResponseDTO> getProductOptions(Long id) {
        return getEntityById(id).getOptions()
                .stream()
                .map(productOptionMapper::toDTO)
                .toList();
    }


    @Override
    public ProductOptionResponseDTO updateProductOption(Long groupId, Long optionId, ProductOptionRequestDTO dto) {
        ProductGroup group = getEntityById(groupId);

        ProductOption optionToUpdate = group.getOptions().stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new ProductOptionNotFoundException(optionId));

        ProductOption updatedOption = productOptionService.updateProductOption(optionToUpdate, dto);

        return productOptionMapper.toDTO(updatedOption);
    }

    @Override
    public ProductOptionResponseDTO removeProductOption(Long id, Long optionId) {
        ProductGroup productGroup = getEntityById(id);

        ProductOption option = productGroup.getOptions().stream()
                .filter(opt -> opt.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new ProductOptionNotFoundException(optionId));

        productGroup.getOptions().remove(option);

        productGroupRepository.save(productGroup);

        return productOptionMapper.toDTO(option);
    }

    private ProductOptionResponseDTO addProductOption(ProductGroup productGroup, ProductOptionRequestDTO dto) {
        ProductOption option = productOptionService.createProductOption(productGroup, dto);

        productGroup.getOptions().add(option);

        return productOptionMapper.toDTO(option);
    }
}
