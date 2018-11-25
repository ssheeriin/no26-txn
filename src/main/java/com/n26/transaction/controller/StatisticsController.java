package com.n26.transaction.controller;

import com.n26.transaction.TransactionException;
import com.n26.transaction.api.StatisticsService;
import com.n26.transaction.model.TransactionStatisticsWrapper.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController("/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<Statistics> sendStatistics() throws TransactionException {
        return new ResponseEntity<>(statisticsService.getStatistics(), HttpStatus.OK);
    }

}
