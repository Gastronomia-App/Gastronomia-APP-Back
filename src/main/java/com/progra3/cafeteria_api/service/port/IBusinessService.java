package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import com.progra3.cafeteria_api.model.dto.BusinessResponseDTO;
import com.progra3.cafeteria_api.model.dto.BusinessUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Business;

public interface IBusinessService {
    BusinessResponseDTO createBusiness(BusinessRequestDTO dto);
    Business getEntityById(Long id);
    BusinessResponseDTO getBusinessById(Long id);
    BusinessResponseDTO updateBusiness(Long id, BusinessUpdateDTO dto);
    void deleteBusiness(Long id);
    BusinessResponseDTO getBusinessForCurrentUser();
    BusinessResponseDTO getBusinessBySlugPublic(String slug);
}
