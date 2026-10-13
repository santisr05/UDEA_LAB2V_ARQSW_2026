package com.udea.lab1v2026.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void illegalArgument_returns400() {
        ProblemDetail problem = handler.handleIllegalArgument(new IllegalArgumentException("dato invalido"));

        assertEquals(400, problem.getStatus());
        assertEquals("dato invalido", problem.getDetail());
    }

    @Test
    void customerNotFound_returns404() {
        ProblemDetail problem = handler.handleCustomerNotFound(new CustomerNotFoundException("no existe"));

        assertEquals(404, problem.getStatus());
        assertEquals("no existe", problem.getDetail());
    }

    @Test
    void dataIntegrity_returns409() {
        ProblemDetail problem = handler.handleDataIntegrity(new DataIntegrityViolationException("duplicado"));

        assertEquals(409, problem.getStatus());
    }
}
