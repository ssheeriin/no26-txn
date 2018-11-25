package com.n26.transaction.model;

import com.n26.transaction.TransactionException;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Transaction {

    private String amount;
    private String timestamp;

    public static Transaction create(String time, String amount) {
        return new Transaction(time, amount);
    }

    public BigDecimal amountToBigDecimal() throws TransactionException {
        validate();
        try {
            return new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
        } catch (NumberFormatException e) {
            throw new TransactionException(TransactionException.Reason.NON_NUMERIC_INPUT);
        }
    }

    private void validate() throws TransactionException {
        if (StringUtils.isEmpty(amount) || StringUtils.isEmpty(timestamp)) {
            throw new TransactionException(TransactionException.Reason.NULL_INPUT);
        }

    }

    public Optional<Instant> getTime() throws TransactionException {
        try {
            validate();
            return Optional.ofNullable(Instant.parse(timestamp));
        } catch (DateTimeParseException e) {
            throw new TransactionException(TransactionException.Reason.INVALID_TIMESTAMP);
        }
    }

    public long getTimeInMillis() throws TransactionException {
        return getTime().orElseThrow(() -> new TransactionException(TransactionException.Reason.NULL_INPUT)).toEpochMilli();
    }
}
