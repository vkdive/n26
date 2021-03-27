package com.n26.service;

import com.n26.cache.Cache;
import com.n26.models.StatisticSummary;
import com.n26.models.TransactionRequest;
import com.n26.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;

@Service
@Slf4j
public class StatisticsService {
    private final Cache cache;
    private final TimeUtils timeUtils = new TimeUtils();
    private DoubleSummaryStatistics aggregatedStatisticsSummary;

    StatisticsService(Cache cache) {
        this.cache = cache;
        aggregatedStatisticsSummary = new DoubleSummaryStatistics();
    }

    public void delete() {
        cache.delete();
    }

    public void save(final TransactionRequest transactionRequest) {
        long transactionTimeInSeconds = timeUtils.getTransactionTimeInSeconds(transactionRequest.getTimestamp());
        Double amount = transactionRequest.getAmount();
        cache.saveTransaction(transactionTimeInSeconds, amount);
        synchronized (this) {
            aggregatedStatisticsSummary.accept(amount);
        }
    }

    @Async
    @Scheduled(fixedDelay = TimeUtils.MILLIS_PER_SECOND, initialDelay = TimeUtils.MILLIS_PER_SECOND)
    public void pruneOlderStatistics() {
        long nowInSeconds = timeUtils.getCurrentTimeInSeconds();
        long oldestTimeInSeconds = nowInSeconds - 60;
        if (cache.deleteOlderTransactionFor(oldestTimeInSeconds)) {
            synchronized (this) {
                aggregatedStatisticsSummary = cache.reCalculateAggregatedStatisticsInCache();
            }
        }
    }

    public StatisticSummary getSummary() {
        if (cache.getSize() == 0)
            return new StatisticSummary(0.0, 0.0, 0.0, 0.0, 0L);
        return new StatisticSummary(aggregatedStatisticsSummary);
    }


}
