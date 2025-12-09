package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.business.BusinessCuitAlreadyExistsException;
import com.progra3.cafeteria_api.exception.business.BusinessNameAlreadyExistsException;
import com.progra3.cafeteria_api.exception.business.BusinessNotFoundException;
import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import com.progra3.cafeteria_api.model.dto.BusinessResponseDTO;
import com.progra3.cafeteria_api.model.dto.BusinessUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Employee;
import com.progra3.cafeteria_api.model.enums.Role;
import com.progra3.cafeteria_api.model.mapper.BusinessMapper;
import com.progra3.cafeteria_api.model.entity.Business;
import com.progra3.cafeteria_api.repository.BusinessRepository;
import com.progra3.cafeteria_api.repository.EmployeeRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.IBusinessService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class BusinessService implements IBusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeContext employeeContext;
    private final EmployeeRepository employeeRepository;

    public BusinessService(
            BusinessRepository businessRepository,
            BusinessMapper businessMapper,
            PasswordEncoder passwordEncoder,
            @Lazy EmployeeContext employeeContext,
            EmployeeRepository employeeRepository) {
        this.businessRepository = businessRepository;
        this.businessMapper = businessMapper;
        this.passwordEncoder = passwordEncoder;
        this.employeeContext = employeeContext;
        this.employeeRepository = employeeRepository;
    }

    private BusinessResponseDTO enrich(Business business) {
        BusinessResponseDTO base = businessMapper.toDTO(business);

        long total = employeeRepository.countByBusiness_IdAndRoleNot(
                business.getId(), Role.OWNER);

        long active = employeeRepository.countByBusiness_IdAndDeletedFalseAndRoleNot(
                business.getId(), Role.OWNER);

        long inactive = employeeRepository.countByBusiness_IdAndDeletedTrueAndRoleNot(
                business.getId(), Role.OWNER);

        return new BusinessResponseDTO(
                base.id(),
                base.name(),
                base.cuit(),
                base.address(),
                base.owner(),
                base.slug(),          // new field
                (int) total,
                (int) active,
                (int) inactive
        );
    }


    private String generateSlug(String name) {
        // Basic slug generation: lower-case, remove accents, replace spaces with '-'
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}", "");

        String base = withoutAccents
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "") // keep only letters, numbers, spaces and '-'
                .trim()
                .replaceAll("\\s+", "-");        // spaces -> '-'

        return base;
    }

    private String generateUniqueSlug(String name) {
        String base = generateSlug(name);
        String slug = base;
        int suffix = 2;

        // Ensure global uniqueness
        while (businessRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix;
            suffix++;
        }

        return slug;
    }


    @Override
    @Transactional
    public BusinessResponseDTO createBusiness(BusinessRequestDTO dto) {
        Business business = businessMapper.toEntity(dto);

        // Generate slug based on business name
        String slug = generateUniqueSlug(dto.name());
        business.setSlug(slug);

        Employee owner = business.getOwner();
        owner.setDeleted(false);
        owner.setPassword(passwordEncoder.encode(owner.getPassword()));
        owner.setUsername(
                owner.getUsername()
                        + "@"
                        + business.getName().toLowerCase().replace(" ", "_")
        );
        owner.setBusiness(business);

        business.getEmployees().add(owner);

        return enrich(businessRepository.save(business));
    }

    @Override
    public Business getEntityById(Long id) {
        return businessRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessNotFoundException(id));
    }

    @Override
    public BusinessResponseDTO getBusinessById(Long id) {
        Business business = getEntityById(id);
        return enrich(business);
    }

    @Override
    @Transactional
    public BusinessResponseDTO updateBusiness(Long id, BusinessUpdateDTO dto) {
        Business business = getEntityById(id);

        Long currentBusinessId = employeeContext.getCurrentBusinessId();
        if (!business.getId().equals(currentBusinessId)) {
            throw new AccessDeniedException("No tienes permiso para modificar este negocio");
        }

        // Validate name uniqueness if changed
        if (dto.name() != null && !dto.name().equals(business.getName())) {
            if (businessRepository.existsByName(dto.name())) {
                throw new BusinessNameAlreadyExistsException(dto.name());
            }
        }

        // Validate CUIT uniqueness if changed
        if (dto.cuit() != null && !dto.cuit().equals(business.getCuit())) {
            if (businessRepository.existsByCuit(dto.cuit())) {
                throw new BusinessCuitAlreadyExistsException(dto.cuit());
            }
        }

        businessMapper.updateBusinessFromDTO(dto, business);

        return enrich(businessRepository.save(business));
    }

    @Override
    @Transactional
    public void deleteBusiness(Long id) {
        Business business = getEntityById(id);

        Long currentBusinessId = employeeContext.getCurrentBusinessId();
        if (!business.getId().equals(currentBusinessId)) {
            throw new AccessDeniedException("No tienes permiso para eliminar este negocio");
        }

        business.setDeleted(true);
        businessRepository.save(business);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponseDTO getBusinessForCurrentUser() {
        Long currentBusinessId = employeeContext.getCurrentBusinessId();
        Business business = businessRepository.findByIdAndDeletedFalse(currentBusinessId)
                .orElseThrow(() -> new BusinessNotFoundException(currentBusinessId));
        return enrich(business);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponseDTO getBusinessBySlugPublic(String slug) {
        Business business = businessRepository.findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new BusinessNotFoundException(slug));
        return enrich(business);
    }
}
