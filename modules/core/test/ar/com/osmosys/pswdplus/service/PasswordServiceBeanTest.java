package ar.com.osmosys.pswdplus.service;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
Unit tests for PasswordServiceBean.isInRecentHistory.
With a history length of N, a new password is validated against the
N newest used passwords (the current one is checked apart, against
SEC_USER), so only those N records must be taken into account.
 */
public class PasswordServiceBeanTest {

    //newest first, as returned by the history query
    private static final List<String> HISTORY = Arrays.asList("h4", "h3", "h2", "h1");

    @Test
    public void passwordWithinWindowIsInHistory() {
        assertTrue(PasswordServiceBean.isInRecentHistory(HISTORY, "h4", 5));
        assertTrue(PasswordServiceBean.isInRecentHistory(HISTORY, "h1", 5));
    }

    @Test
    public void wholeHistoryIsConsideredWhenItMatchesLength() {
        assertTrue(PasswordServiceBean.isInRecentHistory(HISTORY, "h1", 4));
    }

    @Test
    public void unknownPasswordIsNotInHistory() {
        assertFalse(PasswordServiceBean.isInRecentHistory(HISTORY, "other", 5));
    }

    @Test
    public void emptyHistoryContainsNothing() {
        assertFalse(PasswordServiceBean.isInRecentHistory(Collections.emptyList(), "h1", 5));
    }

    @Test
    public void onlyNewestNRecordsAreConsidered() {
        //history built with N=5, length later decreased to 2
        assertTrue(PasswordServiceBean.isInRecentHistory(HISTORY, "h4", 2));
        assertTrue(PasswordServiceBean.isInRecentHistory(HISTORY, "h3", 2));
        assertFalse(PasswordServiceBean.isInRecentHistory(HISTORY, "h2", 2));
        assertFalse(PasswordServiceBean.isInRecentHistory(HISTORY, "h1", 2));
    }

    @Test
    public void lengthOfOneOnlyConsidersLastUsedPassword() {
        assertTrue(PasswordServiceBean.isInRecentHistory(HISTORY, "h4", 1));
        assertFalse(PasswordServiceBean.isInRecentHistory(HISTORY, "h3", 1));
    }

    @Test
    public void nonPositiveLengthIgnoresHistory() {
        assertFalse(PasswordServiceBean.isInRecentHistory(HISTORY, "h4", 0));
        assertFalse(PasswordServiceBean.isInRecentHistory(HISTORY, "h4", -1));
    }
}
