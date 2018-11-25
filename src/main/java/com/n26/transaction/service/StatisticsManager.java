package com.n26.transaction.service;

import com.n26.transaction.Config;
import com.n26.transaction.TransactionException;
import com.n26.transaction.model.Transaction;
import com.n26.transaction.model.TransactionStatisticsWrapper;
import com.n26.transaction.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

/**
 * Accept the transactions and calculates statistics
 */
@Component
public class StatisticsManager {

    private TransactionStatisticsWrapper[] transactionStatisticWrappers;

    @Autowired
    private Config config;

    private int historyTimeInMillis;
    private int timeSliceInMillis;

    @PostConstruct
    public void afterInit() {
        historyTimeInMillis = config.getMaxHistoryInMillis();
        timeSliceInMillis = config.getTimeSliceInMillis();
        transactionStatisticWrappers = new TransactionStatisticsWrapper[historyTimeInMillis / timeSliceInMillis];
    }

    /**
     * Add this @transaction to statistics
     *
     * @param transaction transaction to be added
     * @param now         transaction incoming time
     * @throws TransactionException if transaction cannot be applied
     */
    public void accept(Transaction transaction, Instant now) throws TransactionException {
        assertValidTransaction(transaction, now);
        int index = getTransactionIndex(transaction);
        TransactionStatisticsWrapper statistics = getTransactionStatistic(index);

        try {
            statistics.getLock().writeLock().lock();
            statistics.accept(transaction, now);
        } finally {
            statistics.getLock().writeLock().unlock();
        }
    }

    private void assertValidTransaction(Transaction transaction, Instant now) throws TransactionException {
        if (!isValidTransaction(transaction, now)) {
            throw new TransactionException(TransactionException.Reason.OUTDATED_TRANSACTION);
        }
        if (isFutureTransaction(transaction, now)) {
            throw new TransactionException(TransactionException.Reason.FUTURE_DATED_TRANSACTION);
        }
    }

    private boolean isFutureTransaction(Transaction transaction, Instant now) throws TransactionException {
        return TimeUtil.isFutureTransaction(now.toEpochMilli(), transaction.getTimeInMillis());
    }

    private boolean isValidTransaction(Transaction transaction, Instant now) throws TransactionException {
        return TimeUtil.isValidTransaction(historyTimeInMillis, timeSliceInMillis, now.toEpochMilli(), transaction.getTimeInMillis());
    }

    private TransactionStatisticsWrapper getTransactionStatistic(int index) {
        TransactionStatisticsWrapper statistics = transactionStatisticWrappers[index];
        if (statistics == null) {
            statistics = new TransactionStatisticsWrapper(timeSliceInMillis, historyTimeInMillis);
            transactionStatisticWrappers[index] = statistics;
        }
        return statistics;
    }

    private int getTransactionIndex(Transaction transaction) throws TransactionException {
        long txnTime = getTransactionTime(transaction);
        return (int) ((txnTime / timeSliceInMillis) % transactionStatisticWrappers.length);
    }

    private long getTransactionTime(Transaction transaction) throws TransactionException {
        return transaction.getTime().orElseThrow(TransactionException::new).toEpochMilli();
    }

    public void clear() {
        transactionStatisticWrappers = new TransactionStatisticsWrapper[historyTimeInMillis / timeSliceInMillis];
    }

    public TransactionStatisticsWrapper.Statistics getStatistics() {
        TransactionStatisticsWrapper.Statistics total = new TransactionStatisticsWrapper.Statistics();
        Instant now = Instant.now();
        Arrays.stream(transactionStatisticWrappers).filter(c -> c != null && c.getStatistics().getCount() > 0).forEach(c -> c.mergeTo(total, now));
        if (total.getCount() < 1) {
            total.setMin(BigDecimal.ZERO);
            total.setMax(BigDecimal.ZERO);
        }
        return total;
    }
}
