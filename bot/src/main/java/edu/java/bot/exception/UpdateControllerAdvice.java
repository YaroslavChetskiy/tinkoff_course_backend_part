package edu.java.bot.exception;

import edu.java.bot.model.dto.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UpdateControllerAdvice {

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidRequestException(Exception exception) {
        return ApiErrorResponse.builder()
            .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .description("Некорректные параметры запроса")
            .exceptionName(exception.getClass().getName())
            .exceptionMessage(exception.getMessage())
            .stackTrace(Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .toList())
            .build();
    }
}
