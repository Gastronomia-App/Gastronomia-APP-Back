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

    @Mapping(target = "dateTime", source = "dateTime")
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "audit", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    Expense toEntity(ExpenseRequestDTO expenseRequestDTO);

    @Mapping(target = "dateTime", source = "dateTime")
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "audit", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateExpenseFromDTO(ExpenseUpdateDTO expenseUpdateDTO, @MappingTarget Expense expense);
}
