package com.n26.controllers;

import com.n26.models.StatisticSummary;
import com.n26.service.StatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteController {

    private final StatisticsService statisticsService;

    public DeleteController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public ResponseEntity get() {
        statisticsService.delete();
        return new ResponseEntity(HttpStatus.OK);
    }
}
