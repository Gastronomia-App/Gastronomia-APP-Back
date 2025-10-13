package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.supplier.SupplierAlreadyActiveException;
import com.progra3.cafeteria_api.exception.supplier.SupplierNotFoundException;
import com.progra3.cafeteria_api.model.dto.SupplierRequestDTO;
import com.progra3.cafeteria_api.model.dto.SupplierResponseDTO;
import com.progra3.cafeteria_api.model.dto.SupplierUpdateDTO;
import com.progra3.cafeteria_api.model.mapper.SupplierMapper;
import com.progra3.cafeteria_api.model.entity.Supplier;
import com.progra3.cafeteria_api.repository.SupplierRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.ISupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierService implements ISupplierService {

    private final SupplierRepository supplierRepository;

    private final EmployeeContext employeeContext;

    private final SupplierMapper supplierMapper;

    @Override
    public SupplierResponseDTO create(SupplierRequestDTO dto) {
        Supplier supplier = supplierMapper.toEntity(dto);
        supplier.setBusiness(employeeContext.getCurrentBusiness());

        if (supplier.getCuit() != null){
            Optional<Supplier> existingSupplier = validateSupplier(supplier);

            if (existingSupplier.isPresent()) {
                supplier = existingSupplier.get();
            }
        }

        supplier.setDeleted(false);

        return supplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    public Page<SupplierResponseDTO> getSuppliers(String tradeName, String legalName, String cuit, Pageable pageable) {
        Page<Supplier> suppliers = supplierRepository.findByBusiness_Id(
                tradeName,
                legalName,
                cuit,
                employeeContext.getCurrentBusinessId(),
                pageable);

        return suppliers.map(supplierMapper::toDTO);
    }

    @Override
    public SupplierResponseDTO getById(Long supplierId) {
        Supplier supplier = getEntityById(supplierId);

        return supplierMapper.toDTO(supplier);
    }

    @Override
    public SupplierResponseDTO update(Long supplierId, SupplierUpdateDTO dto) {

        Supplier supplier = getEntityById(supplierId);
        supplier = supplierMapper.updateSupplierFromDTO(dto, supplier);

        return supplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponseDTO delete(Long supplierId) {
        Supplier supplier = getEntityById(supplierId);

        supplier.setDeleted(true);

        return supplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    public Supplier getEntityById(Long supplierId) {
        Supplier supplier = supplierRepository.findByIdAndBusiness_Id(supplierId, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));

        if (supplier.getDeleted()) throw new SupplierNotFoundException(supplier.getId());

        return supplier;
    }

    private Optional<Supplier> validateSupplier(Supplier supplier){

        Optional<Supplier> optionalSupplier = supplierRepository.findByCuitAndBusiness_Id(supplier.getCuit(), employeeContext.getCurrentBusinessId());

        if (optionalSupplier.isPresent()) {

            if (!optionalSupplier.get().getDeleted()) {
                throw new SupplierAlreadyActiveException("Cuit already exists for another supplier in this business");
            }

            return optionalSupplier;
        }

        return optionalSupplier;
    }
}