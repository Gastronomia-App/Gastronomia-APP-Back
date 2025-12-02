package com.progra3.cafeteria_api.exception.handler;

import com.progra3.cafeteria_api.exception.audit.*;
import com.progra3.cafeteria_api.exception.business.*;
import com.progra3.cafeteria_api.exception.customer.*;
import com.progra3.cafeteria_api.exception.expense.ExpenseNotFoundException;
import com.progra3.cafeteria_api.exception.order.*;
import com.progra3.cafeteria_api.exception.paymentmethod.*;
import com.progra3.cafeteria_api.exception.product.*;
import com.progra3.cafeteria_api.exception.seating.*;
import com.progra3.cafeteria_api.exception.supplier.*;
import com.progra3.cafeteria_api.exception.user.*;
import com.progra3.cafeteria_api.exception.utilities.InvalidDateException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalHandler extends ResponseEntityExceptionHandler {

    private ProblemDetail createProblemDetail(Exception ex, HttpStatus status, String errorCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setProperty("code", errorCode);
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String detail = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setProperty("code", "VALIDATION_ERROR");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String detail = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setProperty("code", "VALIDATION_ERROR");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setProperty("code", "CONSTRAINT_VIOLATION");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(SQLException.class)
    public ProblemDetail handleSQLException(SQLException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "DATABASE_ERROR");
    }

    // -------------------- AUDITS --------------------

    @ExceptionHandler(AuditNotFoundException.class)
    public ProblemDetail handleAuditNotFoundException(AuditNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "AUDIT_NOT_FOUND");
    }

    @ExceptionHandler(AuditInProgressException.class)
    public ProblemDetail handleAuditInProgressException(AuditInProgressException ex) {
        return createProblemDetail(ex, HttpStatus.BAD_REQUEST, "AUDIT_IN_PROGRESS");
    }

    @ExceptionHandler(AuditModificationNotAllowedException.class)
    public ProblemDetail handleAuditModificationNotAllowedException(AuditModificationNotAllowedException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "AUDIT_MODIFICATION_NOT_ALLOWED");
    }

    // -------------------- CUSTOMERS --------------------

    @ExceptionHandler(CustomerAlreadyActiveException.class)
    public ProblemDetail handleCustomerActiveException(CustomerAlreadyActiveException ex) {
        return createProblemDetail(ex, HttpStatus.BAD_REQUEST, "CUSTOMER_ALREADY_ACTIVE");
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFoundException(CustomerNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND");
    }

    @ExceptionHandler(CustomerDniAlreadyExistsException.class)
    public ProblemDetail handleCustomerDniAlreadyExistsException(CustomerDniAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "CUSTOMER_DNI_ALREADY_EXISTS");
    }

    @ExceptionHandler(CustomerEmailAlreadyExistsException.class)
    public ProblemDetail handleCustomerEmailAlreadyExistsException(CustomerEmailAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "CUSTOMER_EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(CustomerPhoneNumberAlreadyExistsException.class)
    public ProblemDetail handleCustomerPhoneNumberAlreadyExistsException(CustomerPhoneNumberAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS");
    }

    // -------------------- EXPENSES --------------------

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ProblemDetail handleExpenseNotFoundException(ExpenseNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "EXPENSE_NOT_FOUND");
    }

    // -------------------- ORDERS --------------------

    @ExceptionHandler(ItemNotFoundException.class)
    public ProblemDetail handleItemNotFoundException(ItemNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "ORDER_ITEM_NOT_FOUND");
    }

    @ExceptionHandler(OrderModificationNotAllowedException.class)
    public ProblemDetail handleOrderModificationNotAllowedException(OrderModificationNotAllowedException ex) {
        return createProblemDetail(ex, HttpStatus.BAD_REQUEST, "ORDER_MODIFICATION_NOT_ALLOWED");
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFoundException(OrderNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND");
    }

    // -------------------- PRODUCTS / STOCK --------------------

    @ExceptionHandler(CategoryCannotBeDeletedException.class)
    public ProblemDetail handleCannotDeleteCategoryException(CategoryCannotBeDeletedException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "CATEGORY_CANNOT_BE_DELETED");
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handleCategoryNotFoundException(CategoryNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND");
    }

    @ExceptionHandler(CategoryNameAlreadyExistsException.class)
    public ProblemDetail handleCategoryNameAlreadyExistsException(CategoryNameAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "CATEGORY_NAME_ALREADY_EXISTS");
    }

    @ExceptionHandler(NotEnoughStockException.class)
    public ProblemDetail handleNotEnoughStockException(NotEnoughStockException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "NOT_ENOUGH_STOCK");
    }

    @ExceptionHandler(ProductGroupCannotBeDeletedException.class)
    public ProblemDetail handleProductGroupCannotBeDeletedException(ProductGroupCannotBeDeletedException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "PRODUCT_GROUP_CANNOT_BE_DELETED");
    }

    @ExceptionHandler(ProductGroupNotFoundException.class)
    public ProblemDetail handleProductGroupNotFoundException(ProductGroupNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "PRODUCT_GROUP_NOT_FOUND");
    }

    @ExceptionHandler(ProductGroupNameAlreadyExistsException.class)
    public ProblemDetail handleProductGroupNameAlreadyExistsException(ProductGroupNameAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "PRODUCT_GROUP_NAME_ALREADY_EXISTS");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFoundException(ProductNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND");
    }

    @ExceptionHandler(ProductNameAlreadyExistsException.class)
    public ProblemDetail handleProductNameAlreadyExistsException(ProductNameAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "PRODUCT_NAME_ALREADY_EXISTS");
    }

    @ExceptionHandler(ProductOptionNotFoundException.class)
    public ProblemDetail handleProductOptionNotFoundException(ProductOptionNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "PRODUCT_OPTION_NOT_FOUND");
    }

    // -------------------- SEATING --------------------

    @ExceptionHandler(SeatingAlreadyExistsException.class)
    public ProblemDetail handleSeatingAlreadyExistsException(SeatingAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "SEATING_ALREADY_EXISTS");
    }

    @ExceptionHandler(SeatingModificationNotAllowed.class)
    public ProblemDetail handleSeatingModificationNotAllowedException(SeatingModificationNotAllowed ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "SEATING_MODIFICATION_NOT_ALLOWED");
    }

    @ExceptionHandler(SeatingNotFoundException.class)
    public ProblemDetail handleSeatingNotFoundException(SeatingNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "SEATING_NOT_FOUND");
    }

    // -------------------- SUPPLIERS --------------------

    @ExceptionHandler(SupplierNotFoundException.class)
    public ProblemDetail handleSupplierNotFoundException(SupplierNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "SUPPLIER_NOT_FOUND");
    }

    @ExceptionHandler(SupplierAlreadyActiveException.class)
    public ProblemDetail handleSupplierAlreadyActiveException(SupplierAlreadyActiveException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "SUPPLIER_ALREADY_ACTIVE");
    }

    @ExceptionHandler(SupplierCuitAlreadyExistsException.class)
    public ProblemDetail handleSupplierCuitAlreadyExistsException(SupplierCuitAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "SUPPLIER_CUIT_ALREADY_EXISTS");
    }

    @ExceptionHandler(SupplierLegalNameAlreadyExistsException.class)
    public ProblemDetail handleSupplierLegalNameAlreadyExistsException(SupplierLegalNameAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "SUPPLIER_LEGAL_NAME_ALREADY_EXISTS");
    }

    @ExceptionHandler(SupplierEmailAlreadyExistsException.class)
    public ProblemDetail handleSupplierEmailAlreadyExistsException(SupplierEmailAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "SUPPLIER_EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(SupplierPhoneNumberAlreadyExistsException.class)
    public ProblemDetail handleSupplierPhoneNumberAlreadyExistsException(SupplierPhoneNumberAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "SUPPLIER_PHONE_NUMBER_ALREADY_EXISTS");
    }

    // -------------------- BUSINESS --------------------

    @ExceptionHandler(BusinessNameAlreadyExistsException.class)
    public ProblemDetail handleBusinessNameAlreadyExistsException(BusinessNameAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "BUSINESS_NAME_ALREADY_EXISTS");
    }

    @ExceptionHandler(BusinessCuitAlreadyExistsException.class)
    public ProblemDetail handleBusinessCuitAlreadyExistsException(BusinessCuitAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "BUSINESS_CUIT_ALREADY_EXISTS");
    }

    @ExceptionHandler(BusinessNotFoundException.class)
    public ProblemDetail handleBusinessNotFoundException(BusinessNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "BUSINESS_NOT_FOUND");
    }

    // -------------------- USERS / EMPLOYEES --------------------

    @ExceptionHandler(OwnerAlreadyExistsException.class)
    public ProblemDetail handleAdminAlreadyExistsException(OwnerAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "OWNER_ALREADY_EXISTS");
    }

    @ExceptionHandler(OwnerCannotBeDeletedException.class)
    public ProblemDetail handleAdminCannotBeDeletedException(OwnerCannotBeDeletedException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "OWNER_CANNOT_BE_DELETED");
    }

    @ExceptionHandler(EmployeeAlreadyActiveException.class)
    public ProblemDetail handleEmployeeAlreadyActiveException(EmployeeAlreadyActiveException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "EMPLOYEE_ALREADY_ACTIVE");
    }

    @ExceptionHandler(EmployeeCannotBeDeletedException.class)
    public ProblemDetail handleEmployeeCannotBeDeletedException(EmployeeCannotBeDeletedException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "EMPLOYEE_CANNOT_BE_DELETED");
    }

    @ExceptionHandler(EmployeeDeletedException.class)
    public ProblemDetail handleEmployeeDeletedException(EmployeeDeletedException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "EMPLOYEE_DELETED");
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND");
    }

    @ExceptionHandler(EmployeePermissionException.class)
    public ProblemDetail handleEmployeePermissionException(EmployeePermissionException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "EMPLOYEE_PERMISSION_DENIED");
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPasswordException(InvalidPasswordException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "INVALID_PASSWORD");
    }

    @ExceptionHandler(NoLoggedUserException.class)
    public ProblemDetail handleNoLoggedUserException(NoLoggedUserException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "NO_LOGGED_USER");
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS");
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ProblemDetail handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "PHONE_NUMBER_ALREADY_EXISTS");
    }

    @ExceptionHandler(DniAlreadyExistsException.class)
    public ProblemDetail handleDniAlreadyExistsException(DniAlreadyExistsException ex) {
        return createProblemDetail(ex, HttpStatus.CONFLICT, "DNI_ALREADY_EXISTS");
    }

    // -------------------- PAYMENT METHODS --------------------

    @ExceptionHandler(com.progra3.cafeteria_api.exception.paymentmethod.PaymentMethodNotFoundException.class)
    public ProblemDetail handlePaymentMethodNotFoundException(com.progra3.cafeteria_api.exception.paymentmethod.PaymentMethodNotFoundException ex) {
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "PAYMENT_METHOD_NOT_FOUND");
    }

    @ExceptionHandler(com.progra3.cafeteria_api.exception.paymentmethod.PaymentMethodBusinessMismatchException.class)
    public ProblemDetail handlePaymentMethodBusinessMismatchException(com.progra3.cafeteria_api.exception.paymentmethod.PaymentMethodBusinessMismatchException ex) {
        return createProblemDetail(ex, HttpStatus.BAD_REQUEST, "PAYMENT_METHOD_BUSINESS_MISMATCH");
    }

    @ExceptionHandler(com.progra3.cafeteria_api.exception.order.PaymentMethodRequiredException.class)
    public ProblemDetail handlePaymentMethodRequiredException(com.progra3.cafeteria_api.exception.order.PaymentMethodRequiredException ex) {
        return createProblemDetail(ex, HttpStatus.BAD_REQUEST, "PAYMENT_METHOD_REQUIRED");
    }

    // -------------------- UTILITIES --------------------

    @ExceptionHandler(InvalidDateException.class)
    public ProblemDetail handleInvalidDateException(InvalidDateException ex) {
        return createProblemDetail(ex, HttpStatus.BAD_REQUEST, "INVALID_DATE");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail pd = createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
        pd.setDetail("Unexpected internal server error");
        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ProblemDetail pd = createProblemDetail(ex, HttpStatus.CONFLICT, "DATABASE_CONSTRAINT_VIOLATION");
        pd.setDetail("Database constraint violation");
        return pd;
    }
}