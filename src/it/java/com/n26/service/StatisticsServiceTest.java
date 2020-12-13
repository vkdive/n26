package com.n26.service;

import com.n26.models.StatisticSummary;
import com.n26.models.TransactionRequest;
import com.n26.util.TimeUtils;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.DoubleSummaryStatistics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceTest extends TestCase {

    @InjectMocks
    private StatisticsService statisticsService;

    @Mock
    private TimeUtils mockTimeUtils;

    @Test
    public void shouldSaveTransaction() {
        TransactionRequest transaction = new TransactionRequest(100.0, LocalDateTime.now().minusSeconds(2));
        StatisticSummary statisticSummary = statisticsService.save(transaction);

        assertThat(statisticSummary.getCount(), is(1L));
        assertThat(statisticSummary.getSum(), is(100.0));
        assertThat(statisticSummary.getAvg(), is(100.0));
        assertThat(statisticSummary.getMax(), is(100.0));
        assertThat(statisticSummary.getMin(), is(100.0));
    }

    @Test
    public void shouldNotUpdatedSummaryGivenATransactionToSaveAndExistingTransactionsInBuffer() {

        TransactionRequest transactionOne = new TransactionRequest(100.0, LocalDateTime.now().minusSeconds(2));
        TransactionRequest transactionTwo = new TransactionRequest(200.0, LocalDateTime.now().minusSeconds(4));
        TransactionRequest transactionThree = new TransactionRequest(300.0, LocalDateTime.now().minusSeconds(5));
        statisticsService.save(transactionOne);
        statisticsService.save(transactionTwo);

        StatisticSummary statisticSummaryThree = statisticsService.save(transactionThree);

        assertThat(statisticSummaryThree.getCount(), is(3L));
        assertThat(statisticSummaryThree.getSum(), is(600.0));
        assertThat(statisticSummaryThree.getAvg(), is(200.0));
        assertThat(statisticSummaryThree.getMax(), is(300.0));
        assertThat(statisticSummaryThree.getMin(), is(100.0));
    }

    @Test
    public void shouldReturnOriginalStatisticsSummaryIfNoOlderStatisticsFoundForPrune() {

        TransactionRequest transactionOne = new TransactionRequest(100.0, LocalDateTime.now().minusSeconds(2));
        TransactionRequest transactionTwo = new TransactionRequest(200.0,  LocalDateTime.now().minusSeconds(4));
        statisticsService.save(transactionOne);
        StatisticSummary originalSummary = statisticsService.save(transactionTwo);

        DoubleSummaryStatistics summaryStatistics = statisticsService.pruneOlderStatisticsEverySecond();

        assertThat(summaryStatistics.getCount(), is(originalSummary.getCount()));
        assertThat(summaryStatistics.getMax(), is(originalSummary.getMax()));
    }

    @Test
    public void shouldReturnDefaultSummaryStatisticsIfNothingIsThereInTheBuffer() {
        StatisticSummary defaultSummary = statisticsService.getSummary();

        assertThat(defaultSummary.getCount(), is(0L));
        assertThat(defaultSummary.getSum(), is(0.0));
        assertThat(defaultSummary.getAvg(), is(0.0));
        assertThat(defaultSummary.getMax(), is(0.0));
        assertThat(defaultSummary.getMin(), is(0.0));
    }

    @Test
    public void shouldReturnSummaryStatisticsAsPerContentsOfBuffer() {

        TransactionRequest transaction1 = new TransactionRequest(100.0, LocalDateTime.now().minusSeconds(2));
        TransactionRequest transaction2 = new TransactionRequest(200.0, LocalDateTime.now().minusSeconds(4));

        statisticsService.save(transaction1);
        statisticsService.save(transaction2);

        StatisticSummary statisticSummary = statisticsService.getSummary();

        assertThat(statisticSummary.getCount(), is(2L));
        assertThat(statisticSummary.getSum(), is(300.0));
        assertThat(statisticSummary.getAvg(), is(150.0));
        assertThat(statisticSummary.getMax(), is(200.0));
        assertThat(statisticSummary.getMin(), is(100.0));
    }

}