package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ExpenseRequestDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseResponseDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Expense;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {SupplierMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpenseMapper {
    @Mapping(target = "date", source = "dateTime")
    ExpenseResponseDTO toDTO(Expense expense);

    Expense toEntity(ExpenseRequestDTO expenseRequestDTO);

    void updateExpenseFromDTO(ExpenseUpdateDTO expenseUpdateDTO, @MappingTarget Expense expense);
}
