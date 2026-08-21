package com.picturebook.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.picturebook.global.response.ApiResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e){

        ErrorCode errorCode = e.getErrorCode();
        
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ApiResponse.fail(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e){

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

                String message = e.getBindingResult()
            .getAllErrors()
            .stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .filter(m -> m != null && !m.isBlank())
            .orElse(errorCode.getMessage());

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ApiResponse.fail(
                errorCode.getStatus(),
                errorCode.getCode(),
                message)
            );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e){

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        String message = e.getConstraintViolations()
            .stream()
            .findFirst()
            .map(v -> v.getMessage())
            .filter(m -> m != null && !m.isBlank())
            .orElse(errorCode.getMessage());

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ApiResponse.fail(
                errorCode.getStatus(),
                errorCode.getCode(),
                message)
            );
    }
}
