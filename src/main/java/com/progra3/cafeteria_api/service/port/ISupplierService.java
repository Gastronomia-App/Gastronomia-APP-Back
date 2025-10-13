package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.SupplierRequestDTO;
import com.progra3.cafeteria_api.model.dto.SupplierResponseDTO;
import com.progra3.cafeteria_api.model.dto.SupplierUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISupplierService {
    SupplierResponseDTO create(SupplierRequestDTO dto);
    Page<SupplierResponseDTO> getSuppliers(String tradeName, String legalName, String cuit, Pageable pageable);
    SupplierResponseDTO getById(Long supplierId);
    SupplierResponseDTO update(Long supplierId, SupplierUpdateDTO dto);
    SupplierResponseDTO delete(Long supplierId);

    Supplier getEntityById (Long supplierId);
}
