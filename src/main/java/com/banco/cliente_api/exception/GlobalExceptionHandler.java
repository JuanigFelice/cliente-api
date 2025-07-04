package com.banco.cliente_api.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders; // Importar HttpHeaders
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode; // Importar HttpStatusCode
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.banco.cliente_api.adapter.web.dto.error.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException; // Aunque no se usa directamente en un @ExceptionHandler aquí, puede ser útil.

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Manejo de ClienteNotFoundException
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClienteNotFoundException(ClienteNotFoundException ex, HttpServletRequest request) {
        logger.warn("ClienteNotFoundException: {} for path: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Manejo de InvalidInputException
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex, HttpServletRequest request) {
        logger.warn("InvalidInputException: {} for path: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Sobrescribe el manejador de Spring para errores de validación de argumentos de método
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        String detailedErrorMessage = errors.entrySet().stream()
                .map(entry -> String.format("'%s': '%s'", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));

        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("Validation Error: {} for path: {}", detailedErrorMessage, path);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Error de validación: " + detailedErrorMessage, path);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Sobrescribe el manejador de Spring para MissingServletRequestParameterException
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String message = String.format("Parámetro requerido '%s' de tipo '%s' no encontrado.",
                ex.getParameterName(), ex.getParameterType());
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("Missing Parameter Error: {} for path: {}", message, path);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, message, path);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Sobrescribe el manejador de Spring para HttpMessageNotReadableException (problemas de formato JSON)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String message = "Solicitud inválida: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            message = String.format("Error de formato en el campo '%s'. Valor recibido '%s' no es válido para el tipo esperado '%s'.",
                    ife.getPath().stream()
                       .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : ("[" + ref.getIndex() + "]"))
                       .collect(Collectors.joining(".")),
                    ife.getValue(),
                    ife.getTargetType().getSimpleName());
        } else if (ex.getMostSpecificCause() != null) {
            message = "Solicitud inválida: " + ex.getMostSpecificCause().getMessage();
        }

        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("JSON Parse Error: {} for path: {}", message, path);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, message, path);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    // Manejo de MethodArgumentTypeMismatchException
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Parámetro '%s' debe ser de tipo '%s'. Valor recibido: '%s'",
                ex.getName(), ex.getRequiredType().getSimpleName(), ex.getValue());
        logger.warn("Type Mismatch Error: {} for path: {}", message, request.getRequestURI());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Manejo de ClientesPorProductoNotFoundException
    @ExceptionHandler(ClientesPorProductoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientesPorProductoNotFound(ClientesPorProductoNotFoundException ex, HttpServletRequest request) {
        logger.warn("ClientesPorProductoNotFoundException: {} for path: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Manejo de errores de autenticación
    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.error("Error de autenticación para la ruta [{}]: {}", path, ex.getMessage());

        String errorMessage;
        if (ex instanceof BadCredentialsException) {
            errorMessage = "Credenciales incorrectas. Verifique su usuario y/o contraseña.";
        } else if (ex instanceof DisabledException) {
            errorMessage = "Su cuenta está deshabilitada. Contacte al administrador.";
        } else {
            errorMessage = "Error de autenticación. Por favor, inténtelo de nuevo.";
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, errorMessage, path);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Manejo de errores de autorización (Acceso Denegado)
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        logger.warn("Access Denied for path [{}]: {}", path, ex.getMessage());

        String errorMessage = "Acceso denegado. No tienes los permisos necesarios para realizar esta acción.";
        if (ex instanceof AccessDeniedException) {
            errorMessage = ex.getMessage() != null && !ex.getMessage().isEmpty() ? ex.getMessage() : errorMessage;
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, errorMessage, path);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Manejador genérico para cualquier otra excepción no capturada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled Exception: {} for path: {}", ex.getMessage(), request.getRequestURI(), ex);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo más tarde.", request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}