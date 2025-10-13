package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.audit.AuditInProgressException;
import com.progra3.cafeteria_api.exception.audit.AuditModificationNotAllowedException;
import com.progra3.cafeteria_api.exception.audit.AuditNotFoundException;
import com.progra3.cafeteria_api.model.dto.AuditFinalizeRequestDTO;
import com.progra3.cafeteria_api.model.dto.AuditRequestDTO;
import com.progra3.cafeteria_api.model.dto.AuditResponseDTO;
import com.progra3.cafeteria_api.model.entity.Audit;
import com.progra3.cafeteria_api.model.entity.Expense;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.AuditStatus;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import com.progra3.cafeteria_api.model.mapper.AuditMapper;
import com.progra3.cafeteria_api.repository.AuditRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.IAuditService;
import com.progra3.cafeteria_api.service.helper.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditService implements IAuditService {

    private final AuditRepository auditRepository;

    private final EmployeeContext employeeContext;

    private final OrderService orderService;
    private final ExpenseService expenseService;

    private final AuditMapper auditMapper;

    private final Clock clock;

    @Override
    public AuditResponseDTO create(AuditRequestDTO dto) {

        Audit audit = auditMapper.toEntity(dto);
        audit.setBusiness(employeeContext.getCurrentBusiness());

        audit.setStartTime(dto.startTime());
        audit.setAuditStatus(AuditStatus.IN_PROGRESS);
        audit.setRealCash(Constant.ZERO_AMOUNT);

        List<Order> orders = orderService.getByDateTimeBetween(audit.getStartTime(), LocalDateTime.now(clock));
        orders.forEach(order -> order.setAudit(audit));
        audit.setOrders(orders);

        List<Expense> expenses = expenseService.getByDateTimeBetween(audit.getStartTime(), LocalDateTime.now(clock));
        expenses.forEach(expense -> expense.setAudit(audit));
        audit.setExpenses(expenses);

        recalculateAudit(audit);
        audit.setDeleted(false);

        validateNewAudit(audit);

        return auditMapper.toDTO(auditRepository.save(audit));
    }

    @Override
    public Page<AuditResponseDTO> getAudits(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }
        if (endDate != null) {
            endDateTime = endDate.atTime(LocalTime.MAX);
        }

        Page<Audit> audits = auditRepository.findByBusiness_Id(
                startDateTime,
                endDateTime,
                employeeContext.getCurrentBusinessId(),
                pageable);

        return audits.map(auditMapper::toDTO);
    }

    @Override
    public AuditResponseDTO getById(Long auditId){
        return auditMapper.toDTO(getEntityById(auditId));
    }

    @Override
    public AuditResponseDTO finalize(Long auditId, AuditFinalizeRequestDTO dto) {
        Audit audit = getEntityById(auditId);

        if (!audit.getAuditStatus().equals(AuditStatus.IN_PROGRESS)) {
            throw new AuditModificationNotAllowedException(auditId, audit.getAuditStatus().getName());
        }

        audit.setRealCash(dto.realCash());
        audit.setCloseTime(LocalDateTime.now(clock));
        audit.setAuditStatus(AuditStatus.FINALIZED);

        recalculateAudit(audit);

        return auditMapper.toDTO(auditRepository.save(audit));
    }

    @Override
    public AuditResponseDTO cancel(Long auditId) {
        Audit audit = getEntityById(auditId);

        audit.setDeleted(true);
        audit.setAuditStatus(AuditStatus.CANCELED);

        return auditMapper.toDTO(auditRepository.save(audit));
    }

    @Override
    public Audit getEntityById(Long auditId) {
        return auditRepository.findByIdAndBusiness_Id(auditId, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new AuditNotFoundException(auditId));
    }

    @Override
    public Optional<Audit> getInProgressAudit() {
        return auditRepository.findByBusiness_IdAndAuditStatus(
                employeeContext.getCurrentBusinessId(),
                AuditStatus.IN_PROGRESS
        );
    }

    @Override
    public void recalculateAudit(Audit audit) {
        audit.setTotal(calculateTotal(audit));
        audit.setTotalExpensed(calculateExpenseTotal(audit));
        audit.setBalanceGap(audit.getRealCash() - (audit.getTotal() + audit.getInitialCash() - audit.getTotalExpensed()));
    }

    private double calculateTotal(Audit audit) {
        return audit.getOrders().stream()
                .filter(order -> order.getStatus().equals(OrderStatus.FINALIZED))
                .mapToDouble(Order::getTotal)
                .sum();
    }

    private double calculateExpenseTotal(Audit audit) {
        return audit.getExpenses().stream()
                .filter(expense -> !expense.getDeleted())
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    private void validateNewAudit(Audit newAudit) {
        getInProgressAudit().ifPresent(audit -> {
            throw new AuditInProgressException();
        });

        if (newAudit.getStartTime().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("The start time of the audit cannot be in the future.");
        }
    }
}