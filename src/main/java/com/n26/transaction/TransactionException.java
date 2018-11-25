package com.n26.transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@Getter
@NoArgsConstructor
public class TransactionException extends Exception {

    private Optional<Reason> reason;


    public TransactionException(Reason reason) {
        super(reason != null ? reason.name() : "");
        this.reason = Optional.ofNullable(reason);
    }

    @Getter
    public enum Reason {
        INVALID_TIMESTAMP(HttpStatus.UNPROCESSABLE_ENTITY),
        FUTURE_DATED_TRANSACTION(HttpStatus.UNPROCESSABLE_ENTITY),
        INVALID_INPUT(HttpStatus.UNPROCESSABLE_ENTITY),
        UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
        OUTDATED_TRANSACTION(HttpStatus.NO_CONTENT),
        NULL_INPUT(HttpStatus.UNPROCESSABLE_ENTITY),
        NON_NUMERIC_INPUT(HttpStatus.UNPROCESSABLE_ENTITY);

        private HttpStatus httpStatus;

        Reason(HttpStatus httpStatus) {

            this.httpStatus = httpStatus;
        }
    }


}
