package com.n26.controllers;

import com.n26.models.StatisticSummary;
import com.n26.service.StatisticsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public StatisticSummary get() {
        return statisticsService.getSummary();
    }
}
