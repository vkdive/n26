package com.n26.models;

public class TransactionResponse {

    private final long id;
    private final String content;

    public TransactionResponse(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

}
