package com.n26.util;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TimeUtils {
    public static final long MILLIS_PER_SECOND = 100;
    public static final long TRANSACTION_TIME_LIMIT = 60000;

    public Long getCurrentTimeInMillis() {
        return Instant.now().toEpochMilli();
    }

    public Long getCurrentTimeInSeconds() {
        return Instant.now().getEpochSecond();
    }

    public Long getTransactionTimeInMillis(Instant transactionTime) {
        return transactionTime.toEpochMilli();
    }
    public Long getTransactionTimeInSeconds(Instant transactionTime) {
        return transactionTime.getEpochSecond();
    }

}
