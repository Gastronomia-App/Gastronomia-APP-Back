package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.AddressRequestDTO;
import com.progra3.cafeteria_api.model.dto.AddressResponseDTO;
import com.progra3.cafeteria_api.model.dto.AddressUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Address;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-16T20:41:22-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public AddressResponseDTO toDTO(Address address) {
        if ( address == null ) {
            return null;
        }

        String street = null;
        String city = null;
        String province = null;
        String zipCode = null;

        street = address.getStreet();
        city = address.getCity();
        province = address.getProvince();
        zipCode = address.getZipCode();

        AddressResponseDTO addressResponseDTO = new AddressResponseDTO( street, city, province, zipCode );

        return addressResponseDTO;
    }

    @Override
    public Address toEntity(AddressRequestDTO addressRequestDTO) {
        if ( addressRequestDTO == null ) {
            return null;
        }

        Address address = new Address();

        address.setStreet( addressRequestDTO.street() );
        address.setCity( addressRequestDTO.city() );
        address.setProvince( addressRequestDTO.province() );
        address.setZipCode( addressRequestDTO.zipCode() );

        return address;
    }

    @Override
    public void updateAddressFromDTO(AddressUpdateDTO addressUpdateDTO, Address address) {
        if ( addressUpdateDTO == null ) {
            return;
        }

        if ( addressUpdateDTO.street() != null ) {
            address.setStreet( addressUpdateDTO.street() );
        }
        if ( addressUpdateDTO.city() != null ) {
            address.setCity( addressUpdateDTO.city() );
        }
        if ( addressUpdateDTO.province() != null ) {
            address.setProvince( addressUpdateDTO.province() );
        }
        if ( addressUpdateDTO.zipCode() != null ) {
            address.setZipCode( addressUpdateDTO.zipCode() );
        }
    }
}
