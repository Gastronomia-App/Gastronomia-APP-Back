package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.ExpenseRequestDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseResponseDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface IExpenseService {
    ExpenseResponseDTO create(ExpenseRequestDTO dto);
    Page<ExpenseResponseDTO> getExpenses(Long supplierId, Double minAmount, Double maxAmount, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    ExpenseResponseDTO getById(Long expenseId);
    ExpenseResponseDTO update(Long expenseId, ExpenseUpdateDTO expenseRequestDTO);
    ExpenseResponseDTO delete(Long expenseId);

    Expense getEntityById (Long expenseId);
    List<Expense> getByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
