package com.api.saojeong.global.utill.exception;




import com.api.saojeong.global.utill.init.ErrorCode;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.createFailWithoutData(HttpStatus.BAD_REQUEST.value(), errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomApiResponse<?>> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.createFailWithoutData(HttpStatus.BAD_REQUEST.value(), errorMessage));
    }
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        Map<String, Object> errorResponse = Map.of(
                "status", errorCode.getStatus(),
                "code", errorCode.getCode(),
                "message", errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }
}