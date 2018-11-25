package com.n26.transaction.api;

import com.n26.transaction.TransactionException;
import com.n26.transaction.model.TransactionStatisticsWrapper;

public interface StatisticsService {
    TransactionStatisticsWrapper.Statistics getStatistics() throws TransactionException;
}
