package com.progra3.cafeteria_api.controller;
import com.progra3.cafeteria_api.model.DTO.PublicBusinessDTO;
import com.progra3.cafeteria_api.service.port.IPublicMenuService;
import lombok.RequiredArgsConstructor;
import com.progra3.cafeteria_api.model.dto.CategoryResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/menu")
@RequiredArgsConstructor
public class PublicMenuController {

    private final IPublicMenuService publicMenuService;

    @GetMapping("/{slug}")
    public ResponseEntity<List<CategoryResponseDTO>> getMenuBySlug(@PathVariable String slug) {
        var result = publicMenuService.getMenuByBusinessSlug(slug);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{slug}/business")
    public ResponseEntity<PublicBusinessDTO> getBusinessBySlug(@PathVariable String slug) {
        var result = publicMenuService.getBusinessPublicInfo(slug);
        return ResponseEntity.ok(result);
    }
}