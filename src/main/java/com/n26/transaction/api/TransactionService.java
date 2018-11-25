package com.n26.transaction.api;

import com.n26.transaction.TransactionException;
import com.n26.transaction.model.Transaction;

public interface TransactionService {
    void addTransaction(Transaction transaction) throws TransactionException;

    void deleteTransactions() throws TransactionException;
}
