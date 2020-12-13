package com.n26.integration;

import com.n26.models.StatisticSummary;
import com.n26.models.TransactionRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void integrationTest() throws InterruptedException {
        TransactionRequest tenSecondsEarlierTransaction = new TransactionRequest(200.0, LocalDateTime.now().minusSeconds(10));
        ResponseEntity<TransactionRequest> recentTxnOneResponse = restTemplate.postForEntity("/transactions", tenSecondsEarlierTransaction, TransactionRequest.class);
        assertThat(recentTxnOneResponse.getStatusCode(), is(HttpStatus.CREATED));

        TransactionRequest fiftyNineSecondsEarlierTransaction = new TransactionRequest(100.0, LocalDateTime.now().minusSeconds(59));
        ResponseEntity<TransactionRequest> recentTxnTwoResponse = restTemplate.postForEntity("/transactions", fiftyNineSecondsEarlierTransaction, TransactionRequest.class);
        assertThat(recentTxnTwoResponse.getStatusCode(), is(HttpStatus.CREATED));

        TransactionRequest eightySecondsEarlierTransaction = new TransactionRequest(100.0, LocalDateTime.now().minusSeconds(80));
        ResponseEntity<TransactionRequest> olderTxnResponse = restTemplate.postForEntity("/transactions", eightySecondsEarlierTransaction, TransactionRequest.class);
        assertThat(olderTxnResponse.getStatusCode(), is(HttpStatus.NO_CONTENT));


        ResponseEntity<StatisticSummary> statisticsResponseOne =
                restTemplate.getForEntity("/statistics", StatisticSummary.class);
        assertThat(statisticsResponseOne.getStatusCode(), is(HttpStatus.OK));
        StatisticSummary expectedStatisticSummaryOne = statisticsResponseOne.getBody();
        assertThat(expectedStatisticSummaryOne.getCount(), is(2L));
        assertThat(expectedStatisticSummaryOne.getSum(), is(300.0));
        assertThat(expectedStatisticSummaryOne.getAvg(), is(150.0));
        assertThat(expectedStatisticSummaryOne.getMax(), is(200.0));
        assertThat(expectedStatisticSummaryOne.getMin(), is(100.0));

        Thread.sleep(2 * 1000);

        ResponseEntity<StatisticSummary> statisticsResponseTwo =
                restTemplate.getForEntity("/statistics", StatisticSummary.class);
        assertThat(statisticsResponseOne.getStatusCode(), is(HttpStatus.OK));
        StatisticSummary expectedStatisticSummaryTwo = statisticsResponseTwo.getBody();
        assertThat(expectedStatisticSummaryTwo.getCount(), is(1L));
        assertThat(expectedStatisticSummaryTwo.getSum(), is(200.0));
        assertThat(expectedStatisticSummaryTwo.getAvg(), is(200.0));
        assertThat(expectedStatisticSummaryTwo.getMax(), is(200.0));
        assertThat(expectedStatisticSummaryTwo.getMin(), is(200.0));

    }
}
