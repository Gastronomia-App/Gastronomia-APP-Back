package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.AddressResponseDTO;
import com.progra3.cafeteria_api.model.dto.SupplierRequestDTO;
import com.progra3.cafeteria_api.model.dto.SupplierResponseDTO;
import com.progra3.cafeteria_api.model.dto.SupplierUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Address;
import com.progra3.cafeteria_api.model.entity.Supplier;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-16T20:41:22-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class SupplierMapperImpl implements SupplierMapper {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public SupplierResponseDTO toDTO(Supplier supplier) {
        if ( supplier == null ) {
            return null;
        }

        Long id = null;
        String legalName = null;
        String tradeName = null;
        String cuit = null;
        String phoneNumber = null;
        String email = null;
        AddressResponseDTO address = null;
        Boolean deleted = null;

        id = supplier.getId();
        legalName = supplier.getLegalName();
        tradeName = supplier.getTradeName();
        cuit = supplier.getCuit();
        phoneNumber = supplier.getPhoneNumber();
        email = supplier.getEmail();
        address = addressMapper.toDTO( supplier.getAddress() );
        deleted = supplier.getDeleted();

        SupplierResponseDTO supplierResponseDTO = new SupplierResponseDTO( id, legalName, tradeName, cuit, phoneNumber, email, address, deleted );

        return supplierResponseDTO;
    }

    @Override
    public Supplier toEntity(SupplierRequestDTO supplierRequestDTO) {
        if ( supplierRequestDTO == null ) {
            return null;
        }

        Supplier supplier = new Supplier();

        supplier.setLegalName( supplierRequestDTO.legalName() );
        supplier.setTradeName( supplierRequestDTO.tradeName() );
        supplier.setCuit( supplierRequestDTO.cuit() );
        supplier.setPhoneNumber( supplierRequestDTO.phoneNumber() );
        supplier.setEmail( supplierRequestDTO.email() );
        supplier.setAddress( addressMapper.toEntity( supplierRequestDTO.address() ) );

        return supplier;
    }

    @Override
    public Supplier updateSupplierFromDTO(SupplierUpdateDTO supplierUpdateDTO, Supplier supplier) {
        if ( supplierUpdateDTO == null ) {
            return supplier;
        }

        if ( supplierUpdateDTO.legalName() != null ) {
            supplier.setLegalName( supplierUpdateDTO.legalName() );
        }
        if ( supplierUpdateDTO.tradeName() != null ) {
            supplier.setTradeName( supplierUpdateDTO.tradeName() );
        }
        if ( supplierUpdateDTO.cuit() != null ) {
            supplier.setCuit( supplierUpdateDTO.cuit() );
        }
        if ( supplierUpdateDTO.phoneNumber() != null ) {
            supplier.setPhoneNumber( supplierUpdateDTO.phoneNumber() );
        }
        if ( supplierUpdateDTO.email() != null ) {
            supplier.setEmail( supplierUpdateDTO.email() );
        }
        if ( supplierUpdateDTO.address() != null ) {
            if ( supplier.getAddress() == null ) {
                supplier.setAddress( new Address() );
            }
            addressMapper.updateAddressFromDTO( supplierUpdateDTO.address(), supplier.getAddress() );
        }

        return supplier;
    }
}
