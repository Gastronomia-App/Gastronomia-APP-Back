package com.progra3.cafeteria_api.model.DTO;


public record PublicBusinessDTO(
        Long id,
        String name,
        String slug,
        String phoneNumber,
        String addressStreet,
        String addressCity,
        String addressZipCode,
        String addressProvince
) {}