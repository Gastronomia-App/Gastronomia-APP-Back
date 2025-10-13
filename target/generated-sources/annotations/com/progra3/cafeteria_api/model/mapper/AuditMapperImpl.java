package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.AuditRequestDTO;
import com.progra3.cafeteria_api.model.dto.AuditResponseDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseResponseDTO;
import com.progra3.cafeteria_api.model.dto.OrderResponseDTO;
import com.progra3.cafeteria_api.model.entity.Audit;
import com.progra3.cafeteria_api.model.entity.Expense;
import com.progra3.cafeteria_api.model.entity.Order;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-17T00:03:10-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class AuditMapperImpl implements AuditMapper {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ExpenseMapper expenseMapper;

    @Override
    public AuditResponseDTO toDTO(Audit audit) {
        if ( audit == null ) {
            return null;
        }

        Long id = null;
        LocalDateTime startTime = null;
        LocalDateTime closeTime = null;
        Double initialCash = null;
        List<OrderResponseDTO> orders = null;
        List<ExpenseResponseDTO> expenses = null;
        String auditStatus = null;
        Double totalExpensed = null;
        Double total = null;
        Double balanceGap = null;
        Boolean deleted = null;

        id = audit.getId();
        startTime = audit.getStartTime();
        closeTime = audit.getCloseTime();
        initialCash = audit.getInitialCash();
        orders = orderListToOrderResponseDTOList( audit.getOrders() );
        expenses = expenseListToExpenseResponseDTOList( audit.getExpenses() );
        if ( audit.getAuditStatus() != null ) {
            auditStatus = audit.getAuditStatus().name();
        }
        totalExpensed = audit.getTotalExpensed();
        total = audit.getTotal();
        balanceGap = audit.getBalanceGap();
        deleted = audit.getDeleted();

        AuditResponseDTO auditResponseDTO = new AuditResponseDTO( id, startTime, closeTime, initialCash, orders, expenses, auditStatus, totalExpensed, total, balanceGap, deleted );

        return auditResponseDTO;
    }

    @Override
    public Audit toEntity(AuditRequestDTO auditRequestDTO) {
        if ( auditRequestDTO == null ) {
            return null;
        }

        Audit audit = new Audit();

        audit.setStartTime( auditRequestDTO.startTime() );
        audit.setInitialCash( auditRequestDTO.initialCash() );

        return audit;
    }

    protected List<OrderResponseDTO> orderListToOrderResponseDTOList(List<Order> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderResponseDTO> list1 = new ArrayList<OrderResponseDTO>( list.size() );
        for ( Order order : list ) {
            list1.add( orderMapper.toDTO( order ) );
        }

        return list1;
    }

    protected List<ExpenseResponseDTO> expenseListToExpenseResponseDTOList(List<Expense> list) {
        if ( list == null ) {
            return null;
        }

        List<ExpenseResponseDTO> list1 = new ArrayList<ExpenseResponseDTO>( list.size() );
        for ( Expense expense : list ) {
            list1.add( expenseMapper.toDTO( expense ) );
        }

        return list1;
    }
}
