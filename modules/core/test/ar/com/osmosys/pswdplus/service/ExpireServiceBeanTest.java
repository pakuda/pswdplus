package ar.com.osmosys.pswdplus.service;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
Unit tests for ExpireServiceBean.isPasswordExpired.
A password configured to last N days must be considered expired
once N full days have elapsed since the last change.
 */
public class ExpireServiceBeanTest {

    private static final int VALIDITY_DAYS = 30;

    private ExpireServiceBean expireService;
    private Method isPasswordExpired;

    @Before
    public void setUp() throws Exception {
        expireService = new ExpireServiceBean();
        isPasswordExpired = ExpireServiceBean.class.getDeclaredMethod("isPasswordExpired", Date.class, int.class);
        isPasswordExpired.setAccessible(true);
    }

    @Test
    public void passwordChangedJustNowIsNotExpired() throws Exception {
        assertFalse(isExpired(ago(0, 0), VALIDITY_DAYS));
    }

    @Test
    public void passwordChangedOneHourBeforeValidityEndsIsNotExpired() throws Exception {
        assertFalse(isExpired(ago(VALIDITY_DAYS - 1, 23), VALIDITY_DAYS));
    }

    @Test
    public void passwordIsExpiredWhenValidityDaysHaveElapsed() throws Exception {
        assertTrue(isExpired(ago(VALIDITY_DAYS, 0), VALIDITY_DAYS));
    }

    @Test
    public void passwordIsExpiredDuringTheDayAfterValidityEnds() throws Exception {
        assertTrue(isExpired(ago(VALIDITY_DAYS, 23), VALIDITY_DAYS));
    }

    @Test
    public void passwordIsExpiredOneDayAfterValidityEnds() throws Exception {
        assertTrue(isExpired(ago(VALIDITY_DAYS + 1, 0), VALIDITY_DAYS));
    }

    @Test
    public void veryOldPasswordIsExpired() throws Exception {
        assertTrue(isExpired(ago(365, 0), VALIDITY_DAYS));
    }

    @Test
    public void oneDayValidityIsNotExpiredBeforeTwentyFourHours() throws Exception {
        assertFalse(isExpired(ago(0, 23), 1));
    }

    @Test
    public void oneDayValidityIsExpiredAfterTwentyFourHours() throws Exception {
        assertTrue(isExpired(ago(1, 0), 1));
    }

    @Test
    public void passwordChangedInTheFutureIsNotExpired() throws Exception {
        //e.g. clock skew between servers
        assertFalse(isExpired(ago(0, -1), VALIDITY_DAYS));
    }

    private boolean isExpired(Date lastChange, int validityDays) throws Exception {
        return (Boolean) isPasswordExpired.invoke(expireService, lastChange, validityDays);
    }

    private Date ago(long days, long hours) {
        long millis = TimeUnit.DAYS.toMillis(days) + TimeUnit.HOURS.toMillis(hours);
        return new Date(System.currentTimeMillis() - millis);
    }
}
