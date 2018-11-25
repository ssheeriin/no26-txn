package com.n26.transaction.service;

import com.n26.transaction.Config;
import com.n26.transaction.TransactionException;
import com.n26.transaction.model.TransactionStatisticsWrapper.Statistics;
import com.n26.transaction.util.TransactionTestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static com.n26.transaction.util.TransactionTestHelper.SECONDS;

public class StatisticsManagerImplTest {

    private StatisticsManagerImpl statisticsManager;
    private TransactionTestHelper helper = new TransactionTestHelper();
    private Config config = new Config();

    @Before
    public void setUp() {
        statisticsManager = new StatisticsManagerImpl();
        statisticsManager.setConfig(config);
        config.setMaxHistoryInMillis(60000);
        config.setTimeSliceInMillis(1000);

        statisticsManager.afterInit();

    }

    @Test(expected = TransactionException.class)
    public void testAccept_outDatedTransaction() throws TransactionException {
        Instant now = Instant.now();

        try {
            statisticsManager.accept(helper.createBackDatedTransaction(now, 120, SECONDS, "10.0"), now);
        } catch (TransactionException e) {
            Assert.assertEquals(e.getReason().orElseThrow(RuntimeException::new), TransactionException.Reason.OUTDATED_TRANSACTION);
            throw e;
        }
    }

    @Test(expected = TransactionException.class)
    public void testAccept_futureDatedTransaction() throws TransactionException {
        Instant now = Instant.now();

        try {
            statisticsManager.accept(helper.createFutureDatedTransaction(now, 5, SECONDS, "10.0"), now);
        } catch (TransactionException e) {
            Assert.assertEquals(e.getReason().orElseThrow(RuntimeException::new), TransactionException.Reason.FUTURE_DATED_TRANSACTION);
            throw e;
        }
    }

    @Test
    public void testGetTransactionIndex() throws TransactionException {
        Instant now = Instant.now();
        int idx = (int) ((now.minusSeconds(5).toEpochMilli() / config.getTimeSliceInMillis()) % (60000 / 1000));
        statisticsManager.accept(helper.createBackDatedTransaction(now, 5, SECONDS, "10.0"), now);
        int index = statisticsManager.getTransactionIndex(helper.createBackDatedTransaction(now, 5, SECONDS, "1"));
        Assert.assertEquals(idx, index);
    }

    @Test
    public void testClear() throws TransactionException {
        Instant now = Instant.now();

        try {
            statisticsManager.accept(helper.createBackDatedTransaction(now, 5, SECONDS, "10.0"), now);
            statisticsManager.accept(helper.createBackDatedTransaction(now, 2, SECONDS, "10.0"), now);
            statisticsManager.clear();
            BigDecimal amt = BigDecimal.ZERO;
            String expected = new Statistics(0, amt, amt, amt, amt, 0L).toString();
            Assert.assertEquals(statisticsManager.getStatistics().toString(), expected);
        } catch (TransactionException e) {
            Assert.fail("Unexpected Error");
        }
    }
}