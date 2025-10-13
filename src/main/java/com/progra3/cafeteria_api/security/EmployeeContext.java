package com.progra3.cafeteria_api.security;

import com.progra3.cafeteria_api.model.entity.Business;
import com.progra3.cafeteria_api.service.impl.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeContext {

    private final BusinessService businessService;

    public Long getCurrentEmployeeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((EmployeeDetails) auth.getPrincipal()).getId();
    }

    public Long getCurrentBusinessId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((EmployeeDetails) auth.getPrincipal()).getBusinessId();
    }

    public Business getCurrentBusiness() {
        Long businessId = getCurrentBusinessId();
        return businessService.getEntityById(businessId);
    }
}