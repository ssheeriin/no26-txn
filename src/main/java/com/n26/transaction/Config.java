package com.n26.transaction;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Config {

    @Value("${transaction.time.maxHistoryInMillis}")
    public int maxHistoryInMillis;
    @Value("${transaction.time.timeSliceInMillis}")
    public int timeSliceInMillis;
}
