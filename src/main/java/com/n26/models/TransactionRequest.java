package com.n26.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionRequest {
    private Double amount;
    private Instant timestamp;
}
