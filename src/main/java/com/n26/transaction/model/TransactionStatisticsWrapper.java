package com.n26.transaction.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.n26.transaction.TransactionException;
import com.n26.transaction.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents transaction statistics (at time index i)
 */
public class TransactionStatisticsWrapper {

    private static final Logger logger = LoggerFactory.getLogger(TransactionStatisticsWrapper.class);

    private static final BigDecimal DEFAULT_MAX = BigDecimal.valueOf(Long.MIN_VALUE);
    private static final BigDecimal DEFAULT_MIN = BigDecimal.valueOf(Long.MAX_VALUE);

    @Getter
    private Statistics statistics;
    @Getter
    private ReadWriteLock lock;

    private int timeSliceInMillis;
    private int historyTimeInMillis;

    public TransactionStatisticsWrapper(int timeSliceInMillis, int historyTimeInMillis) {
        statistics = new Statistics();
        lock = new ReentrantReadWriteLock();
        this.timeSliceInMillis = timeSliceInMillis;
        this.historyTimeInMillis = historyTimeInMillis;
    }

    public void accept(Transaction transaction, Instant now) throws TransactionException {

        BigDecimal amount = transaction.amountToBigDecimal();
        logger.trace("Incoming transaction amount: {}", amount.doubleValue());
        Statistics incoming = new Statistics(transaction.getTimeInMillis(), amount, amount, amount, amount, 1L);
        logger.trace("Incoming statistics : {}", statistics);

        if (canMerge(now, incoming)) {
            logger.debug("Merging statistics with incoming");
            logger.trace("Merging statistics {} with incoming {}", this.statistics, incoming);
            mergeStatistics(incoming);
        } else {
            logger.debug("Nothing to merge, setting incoming statistics as the current statistics");
            this.statistics = incoming;
        }
    }

    void mergeStatistics(Statistics incoming) {
        logger.trace("Merging statistics");
        this.statistics.setSum(statistics.getSum().add(incoming.getSum()));
        this.statistics.setCount(statistics.getCount() + incoming.getCount());
        this.statistics.setAvg(statistics.getSum().divide(BigDecimal.valueOf(statistics.getCount()), BigDecimal.ROUND_HALF_UP));

        if (statistics.getMin().compareTo(DEFAULT_MIN) == 0 || statistics.getMin().compareTo(incoming.getMin()) > 0) {
            logger.trace("Old Min : {}, New Min: {}", statistics.getMin(), incoming.getMin());
            this.statistics.setMin(incoming.getMin());
        }
        if (statistics.getMax().compareTo(DEFAULT_MAX) == 0 || statistics.getMax().compareTo(incoming.getMax()) < 0) {
            logger.trace("Old Max : {}, New Max: {}", statistics.getMax(), incoming.getMax());
            this.statistics.setMax(incoming.getMax());
        }

        this.statistics.setTimeStamp(incoming.getTimeStamp());
        logger.trace("Merged statistics : {}", this.statistics);
    }

    boolean canMerge(Instant now, Statistics incoming) {
        return statistics.getCount() > 0 && isValidTransaction(now)
                && (incoming.getCount() > 0 || incoming.getTimeStamp() == 0 || TimeUtil.areTimesInSameSlice(timeSliceInMillis, incoming.timeStamp, this.statistics.timeStamp));
    }

    public void mergeTo(Statistics total, Instant now) {
        try {
            getLock().readLock().lock();
            if (isValidTransaction(now)) {
                total.setSum(total.getSum().add(statistics.getSum()));
                total.setCount(total.getCount() + statistics.getCount());
                total.setAvg(total.getSum().divide(BigDecimal.valueOf(total.getCount()), BigDecimal.ROUND_HALF_UP));

                if (total.getMin().equals(DEFAULT_MIN) || total.getMin().compareTo(statistics.getMin()) > 0) {
                    logger.trace("Old Min : {}, New Min: {}", total.getMin(), statistics.getMin());
                    total.setMin(statistics.getMin());
                }
                if (total.getMax().equals(DEFAULT_MAX) || total.getMax().compareTo(statistics.getMax()) < 0) {
                    logger.trace("Old Max : {}, New Max: {}", total.getMax(), statistics.getMax());
                    total.setMax(statistics.getMax());
                }
            } else {
                logger.debug("ignoring expired transaction statistics: {}", statistics);
            }
        } finally {
            getLock().readLock().unlock();
        }
    }

    private boolean isValidTransaction(Instant now) {
        return TimeUtil.isValidTransaction(historyTimeInMillis, timeSliceInMillis, now.toEpochMilli(), this.getStatistics().getTimeStamp());
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class Statistics {


        @JsonIgnore
        private long timeStamp;

        @JsonSerialize(using = PrecisionSerializer.class)
        private BigDecimal sum;

        @JsonSerialize(using = PrecisionSerializer.class)
        private BigDecimal avg;

        @JsonSerialize(using = PrecisionSerializer.class)
        private BigDecimal max;

        @JsonSerialize(using = PrecisionSerializer.class)
        private BigDecimal min;

        private Long count;


        public Statistics() {
            reset();
        }

        private void reset() {

            this.sum = this.avg = BigDecimal.ZERO;
            this.max = DEFAULT_MAX;
            this.min = DEFAULT_MIN;
            this.timeStamp = count = 0L;
        }
    }

    private static class PrecisionSerializer extends JsonSerializer<BigDecimal> {

        @Override
        public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        }
    }
}
