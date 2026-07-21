package com.uepb.DesafioKnex.exceptions;

import com.uepb.DesafioKnex.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

/**
 * Handler global de exceções da API.
 *
 * Mapeamento de status HTTP:
 *  400 Bad Request   -> erros de validação (corpo/parâmetros inválidos)
 *  401 Unauthorized   -> falta de autenticação / credenciais inválidas
 *  403 Forbidden      -> ação fora do papel do usuário autenticado
 *  404 Not Found      -> recurso inexistente
 *  409 Conflict       -> violação de regra de negócio
 *  500 Internal Server Error -> qualquer erro não previsto
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------------
    // 400 - Bad Request (validação)
    // ---------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST,
                message.isBlank() ? "Dados inválidos." : message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido ou malformado.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // ---------------------------------------------------------------
    // 401 - Unauthorized (falta de autenticação)
    // ---------------------------------------------------------------

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthentication(
            Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED,
                "Autenticação necessária ou credenciais inválidas.", request);
    }

    // ---------------------------------------------------------------
    // 403 - Forbidden (ação fora do papel do usuário)
    // ---------------------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN,
                "Você não tem permissão para executar esta ação.", request);
    }

    // ---------------------------------------------------------------
    // 404 - Not Found (recurso inexistente)
    // ---------------------------------------------------------------

    @ExceptionHandler(ProductNotFound.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFound ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CartItemNotFound.class)
    public ResponseEntity<ErrorResponse> handleCartItemNotFound(
            CartItemNotFound ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ---------------------------------------------------------------
    // 200 OK - Lista vazia (não é erro, apenas ausência de dados)
    // ---------------------------------------------------------------

    @ExceptionHandler(EmptyList.class)
    public ResponseEntity<ErrorResponse> handleEmptyList(
            EmptyList ex, HttpServletRequest request) {
        // Não é um erro de fato: respondemos 200 com mensagem informativa,
        // conforme a regra de negócio de "mensagem clara ao invés de lista vazia".
        return ResponseEntity.ok(
                ErrorResponse.of(HttpStatus.OK.value(), "Informativo", ex.getMessage(), request.getRequestURI()));
    }

    // ---------------------------------------------------------------
    // 409 - Conflict (violação de regra de negócio)
    // ---------------------------------------------------------------

    @ExceptionHandler(EmptyCart.class)
    public ResponseEntity<ErrorResponse> handleEmptyCart(
            EmptyCart ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ProductAlreadySold.class)
    public ResponseEntity<ErrorResponse> handleProductAlreadySold(
            ProductAlreadySold ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExist(
            UserAlreadyExist ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ---------------------------------------------------------------
    // 500 - fallback para qualquer erro não mapeado
    // ---------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado. Tente novamente mais tarde.", request);
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}