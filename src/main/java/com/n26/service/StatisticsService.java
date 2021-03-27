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

    public StatisticsService(Cache cache) {
        this.cache = cache;
        aggregatedStatisticsSummary = new DoubleSummaryStatistics();
    }

    public void saveTransaction(final TransactionRequest transactionRequest) {
        long transactionTimeInSeconds = timeUtils.getTransactionTimeInSeconds(transactionRequest.getTimestamp());
        Double amount = transactionRequest.getAmount();
        cache.add(transactionTimeInSeconds, amount);
        synchronized (this) {
            aggregatedStatisticsSummary.accept(amount);
        }
    }

    @Async
    @Scheduled(fixedDelay = TimeUtils.MILLIS_PER_SECOND, initialDelay = TimeUtils.MILLIS_PER_SECOND)
    public void pruneOlderTransaction() {
        long nowInSeconds = timeUtils.getCurrentTimeInSeconds();
        long oldestTimeInSeconds = nowInSeconds - 60;
        if (cache.deleteKey(oldestTimeInSeconds)) {
            log.info("deleting older value for {}:", oldestTimeInSeconds);
            synchronized (this) {
                aggregatedStatisticsSummary = cache.reCalculateAggregatedStatisticsInCache();
            }
        }
    }

    public StatisticSummary getSummary() {
        log.info("cache contents {}:",cache.getCacheContents());
        log.info("time {}:", timeUtils.getCurrentTimeInSeconds());
        if (cache.getSize() == 0)
            return new StatisticSummary("0.00", "0.00", "0.00", "0.00", 0L);
        return new StatisticSummary(aggregatedStatisticsSummary);
    }

    public void delete() {
        cache.clear();
        aggregatedStatisticsSummary = new DoubleSummaryStatistics();
    }
}
