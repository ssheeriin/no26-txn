package com.n26.transaction.service;

import com.n26.transaction.TransactionException;
import com.n26.transaction.api.StatisticsService;
import com.n26.transaction.api.TransactionService;
import com.n26.transaction.model.Transaction;
import com.n26.transaction.model.TransactionStatisticsWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TransactionServiceImpl implements TransactionService, StatisticsService {

    @Autowired
    private StatisticsManager statisticsManager;

    @Override
    public void addTransaction(Transaction transaction) throws TransactionException {
        statisticsManager.accept(transaction, Instant.now());
    }

    private void validate(Transaction transaction) throws TransactionException {
        if (StringUtils.isBlank(transaction.getAmount())) {
            throw new TransactionException(TransactionException.Reason.INVALID_INPUT);
        }
    }

    @Override
    public void deleteTransactions() throws TransactionException {
        statisticsManager.clear();
    }

    @Override
    public TransactionStatisticsWrapper.Statistics getStatistics() throws TransactionException {
        return statisticsManager.getStatistics();
    }
}
