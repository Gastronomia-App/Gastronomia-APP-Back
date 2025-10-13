package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.AddressResponseDTO;
import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import com.progra3.cafeteria_api.model.dto.BusinessResponseDTO;
import com.progra3.cafeteria_api.model.entity.Business;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-17T00:03:10-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class BusinessMapperImpl implements BusinessMapper {

    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public BusinessResponseDTO toDTO(Business business) {
        if ( business == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String cuit = null;
        AddressResponseDTO address = null;

        id = business.getId();
        name = business.getName();
        cuit = business.getCuit();
        address = addressMapper.toDTO( business.getAddress() );

        BusinessResponseDTO businessResponseDTO = new BusinessResponseDTO( id, name, cuit, address );

        return businessResponseDTO;
    }

    @Override
    public Business toEntity(BusinessRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Business business = new Business();

        business.setName( dto.name() );
        business.setCuit( dto.cuit() );
        business.setAddress( addressMapper.toEntity( dto.address() ) );
        business.setOwner( employeeMapper.toEntity( dto.owner() ) );

        return business;
    }
}
