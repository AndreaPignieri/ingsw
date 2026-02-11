package com.dietiestates25.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleUserAlreadyExists_ReturnsConflict() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User exists");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUserAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User exists", response.getBody().get("message"));
    }

    @Test
    void handleResourceNotFound_ReturnsNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().get("message"));
    }

    @Test
    void handleBadCredentials_ReturnsUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody().get("message"));
    }

    @Test
    void handleAccessDenied_ReturnsForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Denied");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().get("message"));
    }

    @Test
    void handleFileStorageException_ReturnsInternalServerError() {
        FileStorageException ex = new FileStorageException("File error", null);
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleFileStorageException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("File error", response.getBody().get("message"));
    }

    @Test
    void handleMethodArgumentNotValidException_ReturnsBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        FieldError fieldError = new FieldError("object", "field", "defaultMessage");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("defaultMessage", response.getBody().get("field"));
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().get("message").contains("Generic error"));
    }

    @Test
    void handleAgentServiceException_ReturnsInternalServerError() {
        AgentServiceException ex = new AgentServiceException("Agent Error");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAgentServiceException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Agent Error", response.getBody().get("message"));
    }
}
