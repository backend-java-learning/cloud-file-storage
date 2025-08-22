package com.example.exception;

import com.example.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException userAlreadyExistsException) {
        return setMessage(ResponseEntity.status(HttpStatus.CONFLICT), userAlreadyExistsException);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException invalidCredentialsException) {
        return setMessage(ResponseEntity.status(HttpStatus.UNAUTHORIZED), invalidCredentialsException);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException badCredentialsException) {
        return setMessage(ResponseEntity.status(HttpStatus.UNAUTHORIZED), badCredentialsException);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException runtimeException) {
        return setMessage(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR), runtimeException);
    }

    private <T extends RuntimeException> ResponseEntity<ErrorResponse> setMessage(ResponseEntity.BodyBuilder bodyBuilder,
                                                                                  T exception) {
        return bodyBuilder.body(
                ErrorResponse.builder()
                        .message(exception.getMessage())
                        .build()
        );
    }
}
