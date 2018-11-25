package com.n26.transaction.controller;

import com.n26.transaction.TransactionException;
import com.n26.transaction.api.TransactionService;
import com.n26.transaction.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController("/transactions")
public class TransactionController {


    @Autowired
    private TransactionService transactionService;

    private static Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addTransaction(@RequestBody Transaction transaction) throws TransactionException {
        logger.debug("Received transaction {}", transaction);
        transactionService.addTransaction(transaction);

        logger.debug("Transaction recorded");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private void validate(String transactionJson) {

    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteTransactions() throws TransactionException {
        logger.debug("deleting transactions");
        transactionService.deleteTransactions();
        logger.debug("transactions deleted");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
