package com.n26.service;

import com.n26.cache.Cache;
import com.n26.models.StatisticSummary;
import com.n26.models.TransactionRequest;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.Instant;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceTest extends TestCase {

    private StatisticsService statisticsService;

    @Before
    public void setUp() {
        statisticsService = new StatisticsService(new Cache());
    }

    @Test
    public void shouldSaveTransaction() {
        TransactionRequest transaction = new TransactionRequest(100.0, Instant.now().minusSeconds(2));
        statisticsService.saveTransaction(transaction);
        StatisticSummary statisticSummary = statisticsService.getSummary();

        assertThat(statisticSummary.getCount(), is(1L));
        assertThat(statisticSummary.getSum(), is(100.0));
        assertThat(statisticSummary.getAvg(), is(100.0));
        assertThat(statisticSummary.getMax(), is(100.0));
        assertThat(statisticSummary.getMin(), is(100.0));
    }

    @Test
    public void shouldUpdateSummaryForAnTransactionWithExistingTransactionsInBuffer() {

        TransactionRequest transactionOne = new TransactionRequest(100.0, Instant.now().minusSeconds(2));
        TransactionRequest transactionTwo = new TransactionRequest(200.0, Instant.now().minusSeconds(4));
        TransactionRequest transactionThree = new TransactionRequest(300.0, Instant.now().minusSeconds(5));
        statisticsService.saveTransaction(transactionOne);
        statisticsService.saveTransaction(transactionTwo);
        statisticsService.saveTransaction(transactionThree);
        StatisticSummary statisticSummary = statisticsService.getSummary();

        assertThat(statisticSummary.getCount(), is(3L));
        assertThat(statisticSummary.getSum(), is(600.0));
        assertThat(statisticSummary.getAvg(), is(200.0));
        assertThat(statisticSummary.getMax(), is(300.0));
        assertThat(statisticSummary.getMin(), is(100.0));
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

        TransactionRequest transaction1 = new TransactionRequest(100.0, Instant.now().minusSeconds(2));
        TransactionRequest transaction2 = new TransactionRequest(200.0, Instant.now().minusSeconds(4));

        statisticsService.saveTransaction(transaction1);
        statisticsService.saveTransaction(transaction2);

        StatisticSummary statisticSummary = statisticsService.getSummary();

        assertThat(statisticSummary.getCount(), is(2L));
        assertThat(statisticSummary.getSum(), is(300.0));
        assertThat(statisticSummary.getAvg(), is(150.0));
        assertThat(statisticSummary.getMax(), is(200.0));
        assertThat(statisticSummary.getMin(), is(100.0));
    }

    @Test
    public void shouldDeleteTransaction() {

        TransactionRequest transaction1 = new TransactionRequest(100.0, Instant.now().minusSeconds(2));
        TransactionRequest transaction2 = new TransactionRequest(200.0, Instant.now().minusSeconds(4));

        statisticsService.saveTransaction(transaction1);
        statisticsService.saveTransaction(transaction2);

        StatisticSummary statisticSummary = statisticsService.getSummary();

        assertThat(statisticSummary.getCount(), is(2L));
        assertThat(statisticSummary.getSum(), is("300.0"));
        assertThat(statisticSummary.getAvg(), is("150.0"));
        assertThat(statisticSummary.getMax(), is("200.0"));
        assertThat(statisticSummary.getMin(), is("100.0"));

        statisticsService.delete();
        statisticSummary = statisticsService.getSummary();
        assertThat(statisticSummary.getCount(), is(0L));
        assertThat(statisticSummary.getSum(), is("0.00"));
        assertThat(statisticSummary.getAvg(), is("0.00"));
        assertThat(statisticSummary.getMax(), is("0.00"));
        assertThat(statisticSummary.getMin(), is("0.00"));
    }

    @Test
    public void shouldPruneOlderTransactions() throws InterruptedException {

        TransactionRequest transactionOne = new TransactionRequest(100.0, Instant.now().minusSeconds(2));
        TransactionRequest transactionTwo = new TransactionRequest(200.0, Instant.now().minusSeconds(58));
        statisticsService.saveTransaction(transactionOne);
        statisticsService.saveTransaction(transactionTwo);
        StatisticSummary statisticSummary = statisticsService.getSummary();

        assertThat(statisticSummary.getCount(), is(2L));
        assertThat(statisticSummary.getSum(), is(300.0));
        assertThat(statisticSummary.getAvg(), is(150.0));
        assertThat(statisticSummary.getMax(), is(200.0));
        assertThat(statisticSummary.getMin(), is(100.0));

        Thread.sleep(2000);
        statisticsService.pruneOlderTransaction();

        statisticSummary = statisticsService.getSummary();

        assertThat(statisticSummary.getCount(), is(1L));
        assertThat(statisticSummary.getSum(), is(100.0));
        assertThat(statisticSummary.getAvg(), is(100.0));
        assertThat(statisticSummary.getMax(), is(100.0));
        assertThat(statisticSummary.getMin(), is(100.0));
    }
}