package com.n26.transaction;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class Config {

    @Value("${transaction.time.maxHistoryInMillis}")
    public int maxHistoryInMillis;
    @Value("${transaction.time.timeSliceInMillis}")
    public int timeSliceInMillis;
}
