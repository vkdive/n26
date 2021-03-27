package com.n26.controllers;

import com.n26.models.TransactionRequest;
import com.n26.service.StatisticsService;
import com.n26.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import static com.n26.util.TimeUtils.TRANSACTION_TIME_LIMIT;

@Slf4j
@Controller
public class TransactionController {

    private final StatisticsService statisticsService;
    private final TimeUtils timeUtils;

    public TransactionController(TimeUtils timeUtils, StatisticsService statisticsService) {
        this.timeUtils = timeUtils;
        this.statisticsService = statisticsService;
    }

    @PostMapping("/transactions")
    public ResponseEntity transact(@RequestBody TransactionRequest transaction) {
        Long transactionTimeInMillis = timeUtils.getTransactionTimeInMillis(transaction.getTimestamp());
        Long currentTimeInMillis = timeUtils.getCurrentTimeInMillis();
        if (transactionTimeInMillis > currentTimeInMillis){
            return new ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (currentTimeInMillis - transactionTimeInMillis < TRANSACTION_TIME_LIMIT) {
            statisticsService.saveTransaction(transaction);
            return new ResponseEntity(HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
