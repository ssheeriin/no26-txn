package com.n26.transaction.service;

import com.n26.transaction.TransactionException;
import com.n26.transaction.api.StatisticsManager;
import com.n26.transaction.api.StatisticsService;
import com.n26.transaction.api.TransactionService;
import com.n26.transaction.model.Transaction;
import com.n26.transaction.model.TransactionStatisticsWrapper.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TransactionServiceFacade implements TransactionService, StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceFacade.class);

    @Autowired
    private StatisticsManager statisticsManager;

    @Override
    public void addTransaction(Transaction transaction) throws TransactionException {
        statisticsManager.accept(transaction, Instant.now());
        logger.debug("Transaction {} added", transaction);
    }

    @Override
    public void deleteTransactions() {
        statisticsManager.clear();
        logger.debug("Transactions deleted");
    }

    @Override
    public Statistics getStatistics() {
        return statisticsManager.getStatistics();
    }
}
