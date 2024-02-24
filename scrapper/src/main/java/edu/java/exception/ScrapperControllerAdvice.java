package edu.java.exception;

import edu.java.dto.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ScrapperControllerAdvice {

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidRequestException(Exception exception) {
        return buildErrorResponse(
            exception,
            String.valueOf(HttpStatus.BAD_REQUEST.value()),
            "Некорректные параметры запроса"
        );
    }

    @ExceptionHandler(ChatAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleChatAlreadyRegisteredException(ChatAlreadyRegisteredException exception) {
        return buildErrorResponse(
            exception,
            String.valueOf(HttpStatus.CONFLICT.value()),
            "Чат уже зарегистрирован"
        );
    }

    @ExceptionHandler(ChatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleChatNotFoundException(ChatNotFoundException exception) {
        return buildErrorResponse(
            exception,
            String.valueOf(HttpStatus.NOT_FOUND.value()),
            "Чат не найден"
        );
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleLinkAlreadyTrackedException(LinkAlreadyTrackedException exception) {
        return buildErrorResponse(
            exception,
            String.valueOf(HttpStatus.CONFLICT.value()),
            "Ссылка уже отслеживается"
        );
    }

    @ExceptionHandler(LinkNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleLinkNotFoundException(LinkNotFoundException exception) {
        return buildErrorResponse(
            exception,
            String.valueOf(HttpStatus.NOT_FOUND.value()),
            "Ссылка не найдена"
        );
    }

    private ApiErrorResponse buildErrorResponse(
        Exception exception,
        String code,
        String description
    ) {

        return ApiErrorResponse.builder()
            .code(code)
            .description(description)
            .exceptionName(exception.getClass().getName())
            .exceptionMessage(exception.getMessage())
            .stackTrace(Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .toList())
            .build();
    }
}
