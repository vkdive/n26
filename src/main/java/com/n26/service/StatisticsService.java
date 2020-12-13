package com.n26.service;

import com.n26.models.StatisticSummary;
import com.n26.models.TransactionRequest;
import com.n26.util.TimeUtils;
import javaslang.collection.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class StatisticsService {
    private ConcurrentHashMap<Long, DoubleSummaryStatistics> statisticsBuffer = new ConcurrentHashMap<>();
    private DoubleSummaryStatistics aggregatedStatisticsSummary;
    private final TimeUtils timeUtils;

    StatisticsService(TimeUtils timeUtils) {
        this.timeUtils = timeUtils;
        aggregatedStatisticsSummary = new DoubleSummaryStatistics();
    }

    public StatisticSummary getSummary() {
        if (statisticsBuffer.size() == 0)
            return new StatisticSummary(0.0, 0.0, 0.0, 0.0, 0L);
        return new StatisticSummary(aggregatedStatisticsSummary);
    }

    @Async
    public StatisticSummary save(final TransactionRequest transactionRequest) {
        long transactionTimeInSeconds = timeUtils.getTransactionTimeInSeconds(transactionRequest.getTimestamp());
        if (statisticsBuffer.containsKey(transactionTimeInSeconds)) {
            log.info("Ignoring Duplicate transaction", transactionTimeInSeconds);
        } else {
            DoubleSummaryStatistics newPerSecondStatistic = new DoubleSummaryStatistics();
            newPerSecondStatistic.accept(transactionRequest.getAmount());
            statisticsBuffer.put(transactionTimeInSeconds, newPerSecondStatistic);
        }
        log.info("Updating total statistics. Current value : {}", new StatisticSummary(aggregatedStatisticsSummary));
        synchronized (this) {
            aggregatedStatisticsSummary.accept(transactionRequest.getAmount());
        }

        StatisticSummary updatedStatisticSummary = new StatisticSummary(aggregatedStatisticsSummary);
        return updatedStatisticSummary;
    }

    @Async
    @Scheduled(fixedDelay = TimeUtils.MILLIS_PER_SECOND, initialDelay = TimeUtils.MILLIS_PER_SECOND)
    public DoubleSummaryStatistics pruneOlderStatisticsEverySecond() {
        long nowInSeconds = timeUtils.getCurrentTimeInSeconds();
        long oldestTimeInSeconds = nowInSeconds - 60;

        if (statisticsBuffer.containsKey(oldestTimeInSeconds)) {
            statisticsBuffer.remove(oldestTimeInSeconds);
            synchronized (this) {
                aggregatedStatisticsSummary = reCalculateAggregatedStatistics();
            }
            log.info("Updated total statistics: {}", new StatisticSummary(aggregatedStatisticsSummary));
        }
        return aggregatedStatisticsSummary;
    }

    private DoubleSummaryStatistics reCalculateAggregatedStatistics() {
        log.info("Recalculating total statistics Summary");
        return HashMap.ofAll(statisticsBuffer)
                .foldLeft(new DoubleSummaryStatistics(), (acc, tuple2) -> {
                    acc.combine(tuple2._2());
                    return acc;
                });
    }

    public void delete() {
        statisticsBuffer.clear();
    }
}
