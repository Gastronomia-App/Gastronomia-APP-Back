package com.progra3.cafeteria_api.exception.handler;

import com.progra3.cafeteria_api.exception.audit.AuditInProgressException;
import com.progra3.cafeteria_api.exception.audit.AuditModificationNotAllowedException;
import com.progra3.cafeteria_api.exception.audit.AuditNotFoundException;
import com.progra3.cafeteria_api.exception.business.BusinessCuitAlreadyExistsException;
import com.progra3.cafeteria_api.exception.business.BusinessNameAlreadyExistsException;
import com.progra3.cafeteria_api.exception.business.BusinessNotFoundException;
import com.progra3.cafeteria_api.exception.customer.CustomerAlreadyActiveException;
import com.progra3.cafeteria_api.exception.customer.CustomerDniAlreadyExistsException;
import com.progra3.cafeteria_api.exception.customer.CustomerEmailAlreadyExistsException;
import com.progra3.cafeteria_api.exception.customer.CustomerNotFoundException;
import com.progra3.cafeteria_api.exception.customer.CustomerPhoneNumberAlreadyExistsException;
import com.progra3.cafeteria_api.exception.expense.ExpenseNotFoundException;
import com.progra3.cafeteria_api.exception.order.ItemNotFoundException;
import com.progra3.cafeteria_api.exception.order.OrderModificationNotAllowedException;
import com.progra3.cafeteria_api.exception.order.OrderNotFoundException;
import com.progra3.cafeteria_api.exception.product.CategoryCannotBeDeletedException;
import com.progra3.cafeteria_api.exception.product.CategoryNameAlreadyExistsException;
import com.progra3.cafeteria_api.exception.product.CategoryNotFoundException;
import com.progra3.cafeteria_api.exception.product.NotEnoughStockException;
import com.progra3.cafeteria_api.exception.product.ProductGroupCannotBeDeletedException;
import com.progra3.cafeteria_api.exception.product.ProductGroupNameAlreadyExistsException;
import com.progra3.cafeteria_api.exception.product.ProductGroupNotFoundException;
import com.progra3.cafeteria_api.exception.product.ProductNameAlreadyExistsException;
import com.progra3.cafeteria_api.exception.product.ProductNotFoundException;
import com.progra3.cafeteria_api.exception.product.ProductOptionNotFoundException;
import com.progra3.cafeteria_api.exception.seating.SeatingAlreadyExistsException;
import com.progra3.cafeteria_api.exception.seating.SeatingModificationNotAllowed;
import com.progra3.cafeteria_api.exception.seating.SeatingNotFoundException;
import com.progra3.cafeteria_api.exception.supplier.SupplierAlreadyActiveException;
import com.progra3.cafeteria_api.exception.supplier.SupplierCuitAlreadyExistsException;
import com.progra3.cafeteria_api.exception.supplier.SupplierEmailAlreadyExistsException;
import com.progra3.cafeteria_api.exception.supplier.SupplierLegalNameAlreadyExistsException;
import com.progra3.cafeteria_api.exception.supplier.SupplierNotFoundException;
import com.progra3.cafeteria_api.exception.supplier.SupplierPhoneNumberAlreadyExistsException;
import com.progra3.cafeteria_api.exception.user.*;
import com.progra3.cafeteria_api.exception.utilities.InvalidDateException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ResponseMessage response = ResponseMessage.builder()
                .message("Validation errors: " + errors)
                .status(HttpStatus.BAD_REQUEST.value())
                .code("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ResponseMessage> handleSQLException(SQLException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("DATABASE_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- AUDITS --------------------

    @ExceptionHandler(AuditNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleAuditNotFoundException(AuditNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("AUDIT_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(AuditInProgressException.class)
    public ResponseEntity<ResponseMessage> handleAuditInProgressException(AuditInProgressException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("AUDIT_IN_PROGRESS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(AuditModificationNotAllowedException.class)
    public ResponseEntity<ResponseMessage> handleAuditModificationNotAllowedException(AuditModificationNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("AUDIT_MODIFICATION_NOT_ALLOWED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- CUSTOMERS --------------------

    @ExceptionHandler(CustomerAlreadyActiveException.class)
    public ResponseEntity<ResponseMessage> handleCustomerActiveException(CustomerAlreadyActiveException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("CUSTOMER_ALREADY_ACTIVE")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("CUSTOMER_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(CustomerDniAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleCustomerDniAlreadyExistsException(CustomerDniAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("CUSTOMER_DNI_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(CustomerEmailAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleCustomerEmailAlreadyExistsException(CustomerEmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("CUSTOMER_EMAIL_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(CustomerPhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleCustomerPhoneNumberAlreadyExistsException(CustomerPhoneNumberAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- EXPENSES --------------------

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleExpenseNotFoundException(ExpenseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("EXPENSE_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- ORDERS --------------------

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleItemNotFoundException(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("ORDER_ITEM_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(OrderModificationNotAllowedException.class)
    public ResponseEntity<ResponseMessage> handleOrderModificationNotAllowedException(OrderModificationNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("ORDER_MODIFICATION_NOT_ALLOWED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleOrderNotFoundException(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("ORDER_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- PRODUCTS / STOCK --------------------

    @ExceptionHandler(CategoryCannotBeDeletedException.class)
    public ResponseEntity<ResponseMessage> handleCannotDeleteCategoryException(CategoryCannotBeDeletedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("CATEGORY_CANNOT_BE_DELETED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleCategoryNotFoundException(CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("CATEGORY_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(CategoryNameAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleCategoryNameAlreadyExistsException(CategoryNameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("CATEGORY_NAME_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(NotEnoughStockException.class)
    public ResponseEntity<ResponseMessage> handleNotEnoughStockException(NotEnoughStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("NOT_ENOUGH_STOCK")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(ProductGroupCannotBeDeletedException.class)
    public ResponseEntity<ResponseMessage> handleProductGroupCannotBeDeletedException(ProductGroupCannotBeDeletedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("PRODUCT_GROUP_CANNOT_BE_DELETED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(ProductGroupNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleProductGroupNotFoundException(ProductGroupNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("PRODUCT_GROUP_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(ProductGroupNameAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleProductGroupNameAlreadyExistsException(ProductGroupNameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("PRODUCT_GROUP_NAME_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleProductNotFoundException(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("PRODUCT_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(ProductNameAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleProductNameAlreadyExistsException(ProductNameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("PRODUCT_NAME_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(ProductOptionNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleProductOptionNotFoundException(ProductOptionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("PRODUCT_OPTION_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- SEATING --------------------

    @ExceptionHandler(SeatingAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleSeatingAlreadyExistsException(SeatingAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("SEATING_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(SeatingModificationNotAllowed.class)
    public ResponseEntity<ResponseMessage> handleSeatingModificationNotAllowedException(SeatingModificationNotAllowed ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("SEATING_MODIFICATION_NOT_ALLOWED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(SeatingNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleSeatingNotFoundException(SeatingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("SEATING_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- SUPPLIERS --------------------

    @ExceptionHandler(SupplierNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleSupplierNotFoundException(SupplierNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("SUPPLIER_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(SupplierAlreadyActiveException.class)
    public ResponseEntity<ResponseMessage> handleSupplierAlreadyActiveException(SupplierAlreadyActiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("SUPPLIER_ALREADY_ACTIVE")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(SupplierCuitAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleSupplierCuitAlreadyExistsException(SupplierCuitAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("SUPPLIER_CUIT_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(SupplierLegalNameAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleSupplierLegalNameAlreadyExistsException(SupplierLegalNameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("SUPPLIER_LEGAL_NAME_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(SupplierEmailAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleSupplierEmailAlreadyExistsException(SupplierEmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("SUPPLIER_EMAIL_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(SupplierPhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleSupplierPhoneNumberAlreadyExistsException(SupplierPhoneNumberAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("SUPPLIER_PHONE_NUMBER_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- BUSINESS --------------------

    @ExceptionHandler(BusinessNameAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleBusinessNameAlreadyExistsException(BusinessNameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("BUSINESS_NAME_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(BusinessCuitAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleBusinessCuitAlreadyExistsException(BusinessCuitAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("BUSINESS_CUIT_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(BusinessNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleBusinessNotFoundException(BusinessNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("BUSINESS_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- USERS / EMPLOYEES --------------------

    @ExceptionHandler(OwnerAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleAdminAlreadyExistsException(OwnerAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("OWNER_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(OwnerCannotBeDeletedException.class)
    public ResponseEntity<ResponseMessage> handleAdminCannotBeDeletedException(OwnerCannotBeDeletedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("OWNER_CANNOT_BE_DELETED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(EmployeeAlreadyActiveException.class)
    public ResponseEntity<ResponseMessage> handleEmployeeAlreadyActiveException(EmployeeAlreadyActiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("EMPLOYEE_ALREADY_ACTIVE")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(EmployeeCannotBeDeletedException.class)
    public ResponseEntity<ResponseMessage> handleEmployeeCannotBeDeletedException(EmployeeCannotBeDeletedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("EMPLOYEE_CANNOT_BE_DELETED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(EmployeeDeletedException.class)
    public ResponseEntity<ResponseMessage> handleEmployeeDeletedException(EmployeeDeletedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("EMPLOYEE_DELETED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("EMPLOYEE_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(EmployeePermissionException.class)
    public ResponseEntity<ResponseMessage> handleEmployeePermissionException(EmployeePermissionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("EMPLOYEE_PERMISSION_DENIED")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ResponseMessage> handleInvalidPasswordException(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("INVALID_PASSWORD")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(NoLoggedUserException.class)
    public ResponseEntity<ResponseMessage> handleNoLoggedUserException(NoLoggedUserException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("NO_LOGGED_USER")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("USERNAME_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("EMAIL_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("PHONE_NUMBER_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(DniAlreadyExistsException.class)
    public ResponseEntity<ResponseMessage> handleDniAlreadyExistsException(DniAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .code("DNI_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // -------------------- UTILITIES --------------------

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ResponseMessage> handleInvalidDateException(InvalidDateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseMessage.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("INVALID_DATE")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseMessage.builder()
                        .message("Unexpected internal server error")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .code("INTERNAL_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
