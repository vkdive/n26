package com.n26.cache;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CacheTest {
    Cache cache;

    @Before
    public void setUp(){
        cache = new Cache();
    }

    @Test
    public void shouldGetCacheSize() {
        cache.add(Instant.now().getEpochSecond(), 100.00);
        assertThat(cache.getSize(), is(1));

    }

    @Test
    public void shouldSaveValuesInCache() {
        long epochSecond = Instant.now().getEpochSecond();
        cache.add(epochSecond, 200.00);
        ConcurrentHashMap<Long, DoubleSummaryStatistics> cacheContents = cache.getCacheContents();
        assertThat(cacheContents.get(epochSecond).getSum(),is(200.0));
    }

    @Test
    public void shouldDeleteKeyInCache() {
        long epochSecond = Instant.now().getEpochSecond();
        cache.add(epochSecond, 200.00);
        ConcurrentHashMap<Long, DoubleSummaryStatistics> cacheContents = cache.getCacheContents();
        assertThat(cacheContents.get(epochSecond).getSum(),is(200.0));
        assertTrue(cache.deleteKey(epochSecond));
        cacheContents = cache.getCacheContents();
        assertThat(cacheContents.size(), is(0));
    }

    @Test
    public void reCalculateAggregatedStatisticsInCache() {
        long epochSecond = Instant.now().getEpochSecond();
        cache.add(epochSecond, 200.00);
        cache.add(epochSecond-1, 300.00);
        DoubleSummaryStatistics statistics = cache.reCalculateAggregatedStatisticsInCache();
        assertThat(statistics.getCount(),is(2L));
        assertThat(statistics.getSum(),is(500.0));
        assertThat(statistics.getAverage(),is(250.0));
        assertThat(statistics.getMax(),is(300.0));
        assertThat(statistics.getMin(),is(200.00));
        cache.add(epochSecond-2, 400.00);

        statistics = cache.reCalculateAggregatedStatisticsInCache();
        assertThat(statistics.getCount(),is(3L));
        assertThat(statistics.getSum(),is(900.0));
        assertThat(statistics.getAverage(),is(300.0));
        assertThat(statistics.getMax(),is(400.0));
        assertThat(statistics.getMin(),is(200.00));

    }

    @Test
    public void shouldClearAllContentsInCache() {
        long epochSecond = Instant.now().getEpochSecond();
        cache.add(epochSecond, 100.00);
        cache.add(epochSecond-1, 200.00);
        cache.add(epochSecond-2, 300.00);
        cache.add(epochSecond-3, 400.00);
        ConcurrentHashMap<Long, DoubleSummaryStatistics> cacheContents = cache.getCacheContents();
        assertThat(cacheContents.get(epochSecond).getSum(),is(100.0));
        assertThat(cacheContents.get(epochSecond-1).getSum(),is(200.0));
        assertThat(cacheContents.get(epochSecond-2).getSum(),is(300.0));
        assertThat(cacheContents.get(epochSecond-3).getSum(),is(400.0));
        cache.clear();
        cacheContents = cache.getCacheContents();
        assertThat(cacheContents.size(), is(0));
    }
}