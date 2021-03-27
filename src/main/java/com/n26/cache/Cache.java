package com.n26.cache;

import javaslang.collection.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class Cache {
    private ConcurrentHashMap<Long, DoubleSummaryStatistics> statisticsBuffer = new ConcurrentHashMap<>();

    public int getSize() {
        return statisticsBuffer.size();
    }

    @Async
    public void add(long transactionTimeInSeconds, double amount) {
        DoubleSummaryStatistics newPerSecondStatistic = new DoubleSummaryStatistics();
        newPerSecondStatistic.accept(amount);
        statisticsBuffer.put(transactionTimeInSeconds, newPerSecondStatistic);
        log.info("Saved transaction in cache");
    }

    public boolean deleteKey(Long TimeInSeconds) {
        if (statisticsBuffer.containsKey(TimeInSeconds)) {
            Set<Long> set = new HashSet<>();
            set.add(TimeInSeconds);
            statisticsBuffer.keySet().removeAll(set);
            log.info("deleted older transaction");
            return true;
        }
        return false;
    }

    public DoubleSummaryStatistics reCalculateAggregatedStatisticsInCache() {
        log.info("Recalculating aggregated statistics");
        return HashMap.ofAll(statisticsBuffer)
                .foldLeft(new DoubleSummaryStatistics(), (acc, tuple2) -> {
                    acc.combine(tuple2._2());
                    return acc;
                });
    }

    public void clear() {
        statisticsBuffer.clear();
    }
    public ConcurrentHashMap<Long, DoubleSummaryStatistics> getCacheContents(){
        return statisticsBuffer;
    }
}
