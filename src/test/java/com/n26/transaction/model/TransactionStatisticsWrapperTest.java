package com.n26.transaction.model;

import com.n26.transaction.TransactionException;
import com.n26.transaction.model.TransactionStatisticsWrapper.Statistics;
import com.n26.transaction.util.TimeUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalUnit;

import static java.time.temporal.ChronoUnit.SECONDS;


public class TransactionStatisticsWrapperTest {

    private TransactionStatisticsWrapper statisticsWrapper;

    @Before
    public void setUp() throws Exception {
        statisticsWrapper = new TransactionStatisticsWrapper(1000, 60000);
    }

    @Test
    public void testCanMerge_sliceHasStatistics() {
        Instant now = Instant.now();
        try {
            statisticsWrapper.accept(createBackDatedTransaction(now, 2, SECONDS, "10"), now);
        } catch (TransactionException e) {
            Assert.fail("Unexpected exception : " + e.getMessage());
        }
        boolean canMerge = statisticsWrapper.canMerge(now, createDummyStatistics("10"));

        Assert.assertTrue(canMerge);
    }

    @Test
    public void testCanMerge_sliceIsEmpty() {
        Instant now = Instant.now();
        boolean canMerge = statisticsWrapper.canMerge(now, createDummyStatistics("10.0"));
        Assert.assertFalse(canMerge);
    }

    @Test
    public void testMergeStatistics() {
        Instant now = Instant.now();
        addBackDatedTransaction(now, "20");
        Statistics s = createDummyStatistics("10");
        statisticsWrapper.mergeStatistics(s);

        s = statisticsWrapper.getStatistics();
        Assert.assertEquals(s.getCount().longValue(), 2L);
        Assert.assertEquals(s.getMax().longValue(), 20L);
        Assert.assertEquals(s.getMin().longValue(), 10L);
        Assert.assertEquals(s.getSum().longValue(), 30L);
        Assert.assertEquals(s.getAvg().longValue(), 15L);
    }

    @Test
    public void testMergeStatistics_NoPreviousStatistics() {
        Statistics s = createDummyStatistics("10");
        statisticsWrapper.mergeStatistics(s);

        s = statisticsWrapper.getStatistics();
        Assert.assertEquals(s.getCount().longValue(), 1L);
        Assert.assertEquals(s.getMax().longValue(), 10L);
        Assert.assertEquals(s.getMin().longValue(), 10L);
        Assert.assertEquals(s.getSum().longValue(), 10L);
        Assert.assertEquals(s.getAvg().longValue(), 10L);
    }

    private void addBackDatedTransaction(Instant now, String amount) {
        try {
            statisticsWrapper.accept(createBackDatedTransaction(now, 2, SECONDS, amount), now);
        } catch (TransactionException e) {
            Assert.fail("Unexpected exception : " + e.getMessage());
        }
    }

    private Statistics createDummyStatistics(String strAmount) {
        BigDecimal amount = new BigDecimal(strAmount);
        return new Statistics(Instant.now().toEpochMilli(), amount, amount, amount, amount, 1L);
    }

    private Transaction createBackDatedTransaction(Instant start, int offSet, TemporalUnit unit, String amount) {
        Instant time = TimeUtil.getHistoricTime(start, offSet, unit);
        return Transaction.create(amount, time.toString());
    }
}