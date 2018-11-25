package com.n26.transaction.util;

import com.n26.transaction.model.Transaction;
import com.n26.transaction.model.TransactionStatisticsWrapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class TransactionTestHelper {
    public static final ChronoUnit SECONDS = ChronoUnit.SECONDS;

    public TransactionTestHelper() {
    }

    public TransactionStatisticsWrapper.Statistics createDummyStatistics(String strAmount) {
        BigDecimal amount = new BigDecimal(strAmount);
        return new TransactionStatisticsWrapper.Statistics(Instant.now().toEpochMilli(), amount, amount, amount, amount, 1L);
    }

    public Transaction createBackDatedTransaction(Instant start, int offSet, TemporalUnit unit, String amount) {
        Instant time = TimeUtil.getHistoricTime(start, offSet, unit);
        return Transaction.create(amount, time.toString());
    }

    public Transaction createFutureDatedTransaction(Instant start, int offSet, TemporalUnit unit, String amount) {
        Instant time = TimeUtil.getFutureTime(start, offSet, unit);
        return Transaction.create(amount, time.toString());
    }
}