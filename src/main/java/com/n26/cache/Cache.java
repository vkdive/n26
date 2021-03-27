package com.n26.cache;

import com.n26.models.StatisticSummary;
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
public class Cache {
    private ConcurrentHashMap<Long, DoubleSummaryStatistics> statisticsBuffer = new ConcurrentHashMap<>();
    private DoubleSummaryStatistics aggregatedStatisticsSummary;
    private final TimeUtils timeUtils = new TimeUtils();

    Cache() {
        aggregatedStatisticsSummary = new DoubleSummaryStatistics();
    }

    public StatisticSummary getSummary() {
        if (statisticsBuffer.size() == 0)
            return new StatisticSummary(0.0, 0.0, 0.0, 0.0, 0L);
        return new StatisticSummary(aggregatedStatisticsSummary);
    }

    @Async
    public void saveTransaction(long transactionTimeInSeconds, double amount) {
        DoubleSummaryStatistics newPerSecondStatistic = new DoubleSummaryStatistics();
        newPerSecondStatistic.accept(amount);
        statisticsBuffer.put(transactionTimeInSeconds, newPerSecondStatistic);
        log.info("Updating total statistics");
        synchronized (this) {
            aggregatedStatisticsSummary.accept(amount);
        }
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
