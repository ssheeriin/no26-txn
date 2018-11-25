package com.n26.transaction.service;

import com.n26.transaction.Config;
import com.n26.transaction.TransactionException;
import com.n26.transaction.api.StatisticsManager;
import com.n26.transaction.model.Transaction;
import com.n26.transaction.model.TransactionStatisticsWrapper;
import com.n26.transaction.model.TransactionStatisticsWrapper.Statistics;
import com.n26.transaction.util.TimeUtil;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

import static com.n26.transaction.TransactionException.Reason.FUTURE_DATED_TRANSACTION;
import static com.n26.transaction.TransactionException.Reason.OUTDATED_TRANSACTION;

/**
 * Accept the transactions and calculates statistics
 */
@Service
public class StatisticsManagerImpl implements StatisticsManager {

    private static Logger logger = LoggerFactory.getLogger(StatisticsManager.class);

    private TransactionStatisticsWrapper[] transactionStatisticWrappers;

    @Autowired
    @Setter
    private Config config;

    private int historyTimeInMillis;
    private int timeSliceInMillis;

    @PostConstruct
    void afterInit() {
        historyTimeInMillis = config.getMaxHistoryInMillis();
        timeSliceInMillis = config.getTimeSliceInMillis();
        transactionStatisticWrappers = new TransactionStatisticsWrapper[historyTimeInMillis / timeSliceInMillis];
    }

    /**
     * Add a <b>transaction</b> to the statistics
     *
     * @param transaction transaction to be added
     * @param now         transaction incoming time
     * @throws TransactionException if transaction cannot be applied
     */
    @Override
    public void accept(Transaction transaction, Instant now) throws TransactionException {
        assertValidTransaction(transaction, now);

        int index = getTransactionIndex(transaction);
        logger.trace("Index for transaction {} : {}", transaction, index);

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
            logger.warn("Transaction {} is outdated", transaction);
            throw new TransactionException(OUTDATED_TRANSACTION);
        }
        if (isFutureTransaction(transaction, now)) {
            logger.warn("Transaction {} is in future", transaction);
            throw new TransactionException(FUTURE_DATED_TRANSACTION);
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

    int getTransactionIndex(Transaction transaction) throws TransactionException {
        long txnTime = getTransactionTime(transaction);
        logger.trace("Transaction time (mills) : {}", txnTime);

        return (int) ((txnTime / timeSliceInMillis) % transactionStatisticWrappers.length);
    }

    private long getTransactionTime(Transaction transaction) throws TransactionException {
        return transaction.getTime().orElseThrow(TransactionException::new).toEpochMilli();
    }

    public void clear() {
        transactionStatisticWrappers = new TransactionStatisticsWrapper[historyTimeInMillis / timeSliceInMillis];
    }

    @Override
    public Statistics getStatistics() {
        Statistics total = new Statistics();
        Instant now = Instant.now();
        Arrays.stream(transactionStatisticWrappers).filter(wrapper -> wrapper != null && wrapper.getStatistics().getCount() > 0)
                .forEach(wrapper -> wrapper.mergeTo(total, now));

        if (total.getCount() < 1) {
            logger.trace("No transactions found");
            total.setMin(BigDecimal.ZERO);
            total.setMax(BigDecimal.ZERO);
        }
        return total;
    }
}
