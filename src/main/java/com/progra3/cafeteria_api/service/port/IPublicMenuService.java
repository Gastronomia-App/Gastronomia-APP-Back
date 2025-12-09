package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.DTO.PublicBusinessDTO;
import com.progra3.cafeteria_api.model.dto.CategoryResponseDTO;

import java.util.List;

public interface IPublicMenuService {

    /**
     * Returns the public menu (categories + products) for a given business slug.
     */
    List<CategoryResponseDTO> getMenuByBusinessSlug(String slug);

    PublicBusinessDTO getBusinessPublicInfo(String slug);
}
