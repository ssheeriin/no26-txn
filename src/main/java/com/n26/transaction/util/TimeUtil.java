package com.n26.transaction.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.TemporalUnit;

@Component
public class TimeUtil {


    public static long minimumStartingTransactionTimeOf(int maxHistoryInMillis, int timeSliceInMillis, long timestamp) {
        return timestamp - maxHistoryInMillis + timeSliceInMillis;
    }

    public static boolean isFutureTransaction(final long referenceTimeStamp, final long timestamp) {
        return timestamp >= referenceTimeStamp;
    }

    public static boolean isValidTransaction(int historyTimeMillis, int sliceTimeMillis, long reference, long timeStamp) {
        return timeStamp >= minimumStartingTransactionTimeOf(historyTimeMillis, sliceTimeMillis, reference);
    }

    public static boolean areTimesInSameSlice(int timeSliceInMillis, final long time1, final long time2) {
        final long slice1 = time1 / timeSliceInMillis;
        final long slice2 = time2 / timeSliceInMillis;
        return slice1 == slice2;
    }

    public static Instant getFutureTime(int offset, TemporalUnit unit) {
        return Instant.now().plus(offset, unit);
    }

    public static Instant getHistoricTime(int offset, TemporalUnit unit) {
        return Instant.now().minus(offset, unit);
    }

    public static Instant getHistoricTime(Instant time, int offset, TemporalUnit unit) {
        return time.minus(offset, unit);
    }

    public static long getHistoricTimeMillis(Instant time, int offset, TemporalUnit unit) {
        return time.minus(offset, unit).toEpochMilli();
    }

    public static long getFutureTimeMillis(int offset, TemporalUnit unit) {
        return getFutureTime(offset, unit).toEpochMilli();
    }

    public static long getHistoricTimeMillis(int offset, TemporalUnit unit) {
        return getHistoricTime(offset, unit).toEpochMilli();
    }
}
