package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import com.progra3.cafeteria_api.model.dto.BusinessResponseDTO;
import com.progra3.cafeteria_api.model.entity.Business;

public interface IBusinessService {
    BusinessResponseDTO createBusiness(BusinessRequestDTO dto);
    Business getEntityById(Long id);
}
