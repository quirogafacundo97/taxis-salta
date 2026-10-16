
package com.unsa.taxis.exception;

import com.unsa.taxis.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    @Test
    void debeDevolverBadRequestCuandoHayErroresDeValidacion()
            throws NoSuchMethodException {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        BindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(new FieldError(
                "request",
                "whatsappId",
                "no debe estar vacío"
        ));

        MethodParameter parameter = mock(MethodParameter.class);

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        parameter,
                        bindingResult
                );

        ResponseEntity<ErrorResponse> response =
                handler.manejarErrorValidacion(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("whatsappId"));
        assertTrue(response.getBody().getMessage().contains(
                "no debe estar vacío"
        ));
    }
}
