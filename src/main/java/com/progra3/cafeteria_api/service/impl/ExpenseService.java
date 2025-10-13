package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.event.ExpenseCreatedEvent;
import com.progra3.cafeteria_api.exception.expense.ExpenseNotFoundException;
import com.progra3.cafeteria_api.exception.utilities.InvalidDateException;
import com.progra3.cafeteria_api.exception.supplier.SupplierNotFoundException;
import com.progra3.cafeteria_api.model.dto.ExpenseRequestDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseResponseDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Expense;
import com.progra3.cafeteria_api.model.entity.Supplier;
import com.progra3.cafeteria_api.model.mapper.ExpenseMapper;
import com.progra3.cafeteria_api.repository.ExpenseRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.IExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService implements IExpenseService {

    private final ExpenseRepository expenseRepository;

    private final EmployeeContext employeeContext;
    private final SupplierService supplierService;

    private final ApplicationEventPublisher eventPublisher;

    private final ExpenseMapper expenseMapper;

    private final Clock clock;

    @Override
    public ExpenseResponseDTO create(ExpenseRequestDTO dto) {
        Supplier supplier = supplierService.getEntityById(dto.supplierId());

        Expense expense = expenseMapper.toEntity(dto);
        expense.setSupplier(supplier);
        expense.setBusiness(employeeContext.getCurrentBusiness());
        expense.setDateTime(LocalDateTime.now(clock));
        eventPublisher.publishEvent(new ExpenseCreatedEvent(expense));

        return expenseMapper.toDTO(expenseRepository.save(expense));
    }

    @Override
    public Page<ExpenseResponseDTO> getExpenses(Long supplierId, Double minAmount, Double maxAmount, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findByBusiness_Id(
                supplierId,
                minAmount,
                maxAmount,
                startDate,
                endDate,
                employeeContext.getCurrentBusinessId(),
                pageable);

        return expenses.map(expenseMapper::toDTO);
    }

    @Override
    public ExpenseResponseDTO getById(Long expenseId){
        Expense expense = getEntityById(expenseId);
        if (expense.getDeleted()){
            throw new ExpenseNotFoundException(expenseId);
        }
        return expenseMapper.toDTO(expense);
    }

    @Override
    public List<Expense> getByDateTimeBetween(LocalDateTime start, LocalDateTime end){
        if (start.isAfter(end)){
            throw new InvalidDateException("Start should be earlier than end");
        }
        return expenseRepository.findByDateTimeBetweenAndBusiness_Id(start, end, employeeContext.getCurrentBusinessId());
    }

    @Override
    public ExpenseResponseDTO update(Long expenseId, ExpenseUpdateDTO dto) {
        if (!expenseRepository.existsByIdAndBusiness_Id(expenseId, employeeContext.getCurrentBusinessId()))
            throw new SupplierNotFoundException(expenseId);

        Expense expense = getEntityById(expenseId);
        expenseMapper.updateExpenseFromDTO(dto, expense);

        return expenseMapper.toDTO(expenseRepository.save(expense));
    }

    @Override
    public ExpenseResponseDTO delete(Long expenseId) {
        Expense expense = getEntityById(expenseId);

        expense.setDeleted(true);

        return expenseMapper.toDTO(expenseRepository.save(expense));
    }

    @Override
    public Expense getEntityById (Long expenseId){
        return expenseRepository.findByIdAndBusiness_Id(expenseId, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
    }
}