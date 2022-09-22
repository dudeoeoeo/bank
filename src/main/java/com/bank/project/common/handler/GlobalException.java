package com.bank.project.common.handler;

import com.bank.project.common.error.ErrorResponse;
import com.bank.project.common.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity exception(NullPointerException e) {
        log.warn("[NullPointerException] message: {}", e.getMessage());
        CommonResponse body = CommonResponse.failed(e.getMessage());
        return new ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = AccountException.class)
    public ResponseEntity exception(AccountException e) {
        log.warn("[AccountException] error: {}", e.getMessage());
        CommonResponse body = CommonResponse.failed(ErrorResponse.of(e.getErrorCode()));
        return new ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity exception(RuntimeException e) {
        log.warn("[RuntimeException] error: {}", e.getMessage());
        CommonResponse body = CommonResponse.failed(e.getMessage());
        return new ResponseEntity(body, HttpStatus.BAD_REQUEST);
    }
}
