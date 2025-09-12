package com.example.exception;

import com.example.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException resourceNotFoundException) {
        return setMessage(ResponseEntity.status(HttpStatus.NOT_FOUND), resourceNotFoundException);
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPath(InvalidPathException invalidPathException) {
        return setMessage(ResponseEntity.badRequest(), invalidPathException);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(StorageException storageException) {
        return setMessage(ResponseEntity.internalServerError(), "Something went wrong with storage");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(Exception runtimeException) {
        //TODO: add loger
        log.error("Exception appeared", runtimeException);
        return setMessage(ResponseEntity.internalServerError(), "Something went wrong");
    }

    private <T extends RuntimeException> ResponseEntity<ErrorResponse> setMessage(ResponseEntity.BodyBuilder bodyBuilder,
                                                                                  T exception) {
        return setMessage(bodyBuilder, exception.getMessage());
    }

    private <T extends RuntimeException> ResponseEntity<ErrorResponse> setMessage(ResponseEntity.BodyBuilder bodyBuilder,
                                                                                  String message) {
        return bodyBuilder.body(new ErrorResponse(message));
    }
}
