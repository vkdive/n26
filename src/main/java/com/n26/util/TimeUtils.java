package com.n26.util;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TimeUtils {
    public static final long MILLIS_PER_SECOND = 1000;
    public static final long TRANSACTION_TIME_LIMIT = 60000;


    public ZonedDateTime getCurrentTime() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault());
    }
    public Long getCurrentTimeInMillis() {
        return getCurrentTime().toInstant().toEpochMilli();
    }

    public Long getCurrentTimeInSeconds() {
        return getCurrentTime().toInstant().getEpochSecond();
    }

    public Long getTransactionTimeInMillis(LocalDateTime transactionTime) {
        return transactionTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public Long getTransactionTimeInSeconds(LocalDateTime transactionTime) {
        return transactionTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

}
