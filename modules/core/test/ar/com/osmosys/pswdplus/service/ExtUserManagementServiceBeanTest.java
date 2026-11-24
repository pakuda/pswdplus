package ar.com.osmosys.pswdplus.service;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
Unit tests for ExtUserManagementServiceBean.outdatedPasswords.
Before storing the password being replaced, every record that would
fall out of the window must be removed, so that the history ends up
with the N newest used passwords. At least one record is always kept,
since its date is used to compute password expiration.
 */
public class ExtUserManagementServiceBeanTest {

    @Test
    public void emptyHistoryRemovesNothing() {
        assertEquals(Collections.emptyList(), outdated(Collections.emptyList(), 5));
    }

    @Test
    public void historyBelowCapacityRemovesNothing() {
        assertEquals(Collections.emptyList(), outdated(history("h4", "h3", "h2", "h1"), 5));
    }

    @Test
    public void fullHistoryRemovesOldest() {
        //N=5: 5 records + the replaced one would exceed N
        assertEquals(history("h1"), outdated(history("h5", "h4", "h3", "h2", "h1"), 5));
    }

    @Test
    public void decreasedLengthRemovesEveryRecordOutOfWindow() {
        //history built with N=5, length later decreased to 2
        assertEquals(history("h4", "h3", "h2", "h1"), outdated(history("h5", "h4", "h3", "h2", "h1"), 2));
    }

    @Test
    public void lengthOfOneKeepsOnlyReplacedPassword() {
        assertEquals(history("h2", "h1"), outdated(history("h2", "h1"), 1));
    }

    @Test
    public void nonPositiveLengthKeepsOnlyReplacedPasswordForExpiration() {
        assertEquals(history("h2", "h1"), outdated(history("h2", "h1"), 0));
        assertEquals(history("h2", "h1"), outdated(history("h2", "h1"), -1));
    }

    private List<String> outdated(List<String> newestFirst, int historyLength) {
        return ExtUserManagementServiceBean.outdatedPasswords(newestFirst, historyLength);
    }

    private List<String> history(String... newestFirst) {
        return Arrays.asList(newestFirst);
    }
}
