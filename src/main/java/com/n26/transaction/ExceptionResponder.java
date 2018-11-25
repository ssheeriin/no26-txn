package com.n26.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@RequestMapping
public class ExceptionResponder {
    private Logger logger = LoggerFactory.getLogger(ExceptionResponder.class);

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<?> handler(TransactionException ex) {

        TransactionException.Reason reason = ex.getReason().orElse(TransactionException.Reason.UNEXPECTED_ERROR);
        logger.error(reason.name(), ex);
        return new ResponseEntity<>(reason.getHttpStatus());
    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public void handleServiceCallException(MethodArgumentTypeMismatchException e) {
        logger.error(e.getMessage(), e);
    }
}
