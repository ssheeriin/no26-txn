package com.n26.transaction.model;

import com.n26.transaction.TransactionException;
import com.n26.transaction.model.TransactionStatisticsWrapper.Statistics;
import com.n26.transaction.util.TransactionTestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.TemporalUnit;

import static com.n26.transaction.util.TransactionTestHelper.SECONDS;


public class TransactionStatisticsWrapperTest {

    private final TransactionTestHelper transactionTestHelper = new TransactionTestHelper();
    private TransactionStatisticsWrapper statisticsWrapper;

    @Before
    public void setUp() throws Exception {
        statisticsWrapper = new TransactionStatisticsWrapper(1000, 60000);
    }

    @Test
    public void testCanMerge_sliceHasStatistics() {
        Instant now = Instant.now();
        try {
            statisticsWrapper.accept(transactionTestHelper.createBackDatedTransaction(now, 2, SECONDS, "10"), now);
        } catch (TransactionException e) {
            Assert.fail("Unexpected exception : " + e.getMessage());
        }
        boolean canMerge = statisticsWrapper.canMerge(now, transactionTestHelper.createDummyStatistics("10"));

        Assert.assertTrue(canMerge);
    }

    @Test
    public void testCanMerge_sliceIsEmpty() {
        Instant now = Instant.now();
        boolean canMerge = statisticsWrapper.canMerge(now, transactionTestHelper.createDummyStatistics("10.0"));
        Assert.assertFalse(canMerge);
    }

    @Test
    public void testMergeStatistics() {
        Instant now = Instant.now();
        addBackDatedTransaction(now, "20");
        Statistics s = transactionTestHelper.createDummyStatistics("10");
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
        Statistics s = transactionTestHelper.createDummyStatistics("10");
        statisticsWrapper.mergeStatistics(s);

        s = statisticsWrapper.getStatistics();
        Assert.assertEquals(s.getCount().longValue(), 1L);
        Assert.assertEquals(s.getMax().longValue(), 10L);
        Assert.assertEquals(s.getMin().longValue(), 10L);
        Assert.assertEquals(s.getSum().longValue(), 10L);
        Assert.assertEquals(s.getAvg().longValue(), 10L);
    }

    @Test
    public void testMergeTo_validTransaction() {

        Instant now = Instant.now();
        Statistics summary = new Statistics();
        try {
            statisticsWrapper.accept(transactionTestHelper.createBackDatedTransaction(now, 1, SECONDS, "1.0"), now);
            statisticsWrapper.accept(transactionTestHelper.createBackDatedTransaction(now, 5, SECONDS, "1.5"), now);
        } catch (TransactionException e) {
            Assert.fail("Unexpected exception : " + e.getMessage());
        }
        statisticsWrapper.mergeTo(summary, now);
        Assert.assertEquals(2L, summary.getCount().longValue());
        Assert.assertEquals(1.5, summary.getMax().doubleValue(), 0);
        Assert.assertEquals(1.0, summary.getMin().doubleValue(), 0);
        Assert.assertEquals(2.5, summary.getSum().doubleValue(), 0);
        Assert.assertEquals(1.25, summary.getAvg().doubleValue(), 0);
    }

    private void addBackDatedTransaction(Instant now, String amount) {
        try {
            statisticsWrapper.accept(transactionTestHelper.createBackDatedTransaction(now, 2, SECONDS, amount), now);
        } catch (TransactionException e) {
            Assert.fail("Unexpected exception : " + e.getMessage());
        }
    }

    private Statistics createDummyStatistics(String strAmount) {
        return transactionTestHelper.createDummyStatistics(strAmount);
    }

    private Transaction createBackDatedTransaction(Instant start, int offSet, TemporalUnit unit, String amount) {
        return transactionTestHelper.createBackDatedTransaction(start, offSet, unit, amount);
    }
}