package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.model.dto.PaymentMethodRequestDTO;
import com.progra3.cafeteria_api.model.dto.PaymentMethodResponseDTO;
import com.progra3.cafeteria_api.model.dto.PaymentMethodUpdateDTO;
import com.progra3.cafeteria_api.service.port.IPaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final IPaymentMethodService paymentMethodService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PaymentMethodResponseDTO> createPaymentMethod(@Valid @RequestBody PaymentMethodRequestDTO dto) {
        PaymentMethodResponseDTO created = paymentMethodService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'WAITER', 'CASHIER')")
    public ResponseEntity<PaymentMethodResponseDTO> getPaymentMethodById(@PathVariable Long id) {
        PaymentMethodResponseDTO paymentMethod = paymentMethodService.getById(id);
        return ResponseEntity.ok(paymentMethod);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'WAITER', 'CASHIER')")
    public ResponseEntity<Page<PaymentMethodResponseDTO>> getPaymentMethods(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PaymentMethodResponseDTO> paymentMethods = paymentMethodService.getPaymentMethods(name, pageable);
        return ResponseEntity.ok(paymentMethods);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PaymentMethodResponseDTO> updatePaymentMethod(
            @PathVariable Long id,
            @Valid @RequestBody PaymentMethodUpdateDTO dto) {
        PaymentMethodResponseDTO updated = paymentMethodService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PaymentMethodResponseDTO> deletePaymentMethod(@PathVariable Long id) {
        PaymentMethodResponseDTO deleted = paymentMethodService.delete(id);
        return ResponseEntity.ok(deleted);
    }
}

