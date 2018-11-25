package com.n26.transaction.api;

import com.n26.transaction.TransactionException;
import com.n26.transaction.model.Transaction;
import com.n26.transaction.model.TransactionStatisticsWrapper.Statistics;

import java.time.Instant;

public interface StatisticsManager {
    void accept(Transaction transaction, Instant now) throws TransactionException;

    Statistics getStatistics();

    void clear();
}
