package ar.com.osmosys.pswdplus.service;

import ar.com.osmosys.pswdplus.PswdplusTestContainer;
import ar.com.osmosys.pswdplus.config.PswdConfig;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PasswordEncryption;
import com.haulmont.cuba.security.app.UserManagementService;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
Integration tests for the password history, run against the HSQLDB test
database with a fictitious user. With a history length of N, the history
table must keep the N newest used passwords, and only those (besides the
current one, stored in SEC_USER) must block a new password.
 */
public class PasswordHistoryIntegrationTest {

    @ClassRule
    public static PswdplusTestContainer cont = PswdplusTestContainer.Common.INSTANCE;

    //default "Company" group created by CUBA init scripts
    private static final UUID COMPANY_GROUP_ID = UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93");

    private Persistence persistence;
    private Metadata metadata;
    private PasswordEncryption passwordEncryption;
    private UserManagementService userManagementService;
    private PasswordService passwordService;
    private PswdConfig pswdConfig;

    private UUID userId;

    @Before
    public void setUp() throws Exception {
        persistence = cont.persistence();
        metadata = cont.metadata();
        passwordEncryption = AppBeans.get(PasswordEncryption.class);
        userManagementService = AppBeans.get(UserManagementService.NAME);
        passwordService = AppBeans.get(PasswordService.NAME);
        pswdConfig = AppBeans.get(Configuration.class).getConfig(PswdConfig.class);

        pswdConfig.setUsePswdHistory(true);
        userId = createUser("p0");
    }

    @After
    public void tearDown() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            em.setSoftDeletion(false);
            Query query = em.createQuery("delete from pswdplus$PasswordHistory ph where ph.user.id=:userId");
            query.setParameter("userId", userId);
            query.executeUpdate();
            tx.commit();
        }
        cont.deleteRecord("SEC_USER", userId);

        pswdConfig.setPswdHistoryLength(10);
        pswdConfig.setUsePswdHistory(true);
    }

    @Test
    public void historyKeepsNewestNPasswords() throws Exception {
        pswdConfig.setPswdHistoryLength(3);
        changePassword("p1", "p2", "p3", "p4");

        assertEquals(3, historyCount());
        assertTrue(isInHistory("p3"));
        assertTrue(isInHistory("p2"));
        assertTrue(isInHistory("p1"));
        assertFalse(isInHistory("p0"));
    }

    @Test
    public void historyBelowCapacityKeepsEveryPassword() throws Exception {
        pswdConfig.setPswdHistoryLength(5);
        changePassword("p1", "p2");

        assertEquals(2, historyCount());
        assertTrue(isInHistory("p1"));
        assertTrue(isInHistory("p0"));
    }

    @Test
    public void decreasedLengthIsAppliedBeforeNextChange() throws Exception {
        pswdConfig.setPswdHistoryLength(5);
        changePassword("p1", "p2", "p3", "p4");
        assertEquals(4, historyCount());

        pswdConfig.setPswdHistoryLength(2);

        //leftover records no longer block a password...
        assertTrue(isInHistory("p3"));
        assertTrue(isInHistory("p2"));
        assertFalse(isInHistory("p1"));
        assertFalse(isInHistory("p0"));

        //...and are removed on the next change
        changePassword("p1");
        assertEquals(2, historyCount());
        assertTrue(isInHistory("p4"));
        assertTrue(isInHistory("p3"));
        assertFalse(isInHistory("p2"));
    }

    @Test
    public void lengthOfOneKeepsLastUsedPassword() throws Exception {
        pswdConfig.setPswdHistoryLength(1);
        changePassword("p1", "p2");

        assertEquals(1, historyCount());
        assertTrue(isInHistory("p1"));
        assertFalse(isInHistory("p0"));
    }

    @Test
    public void lengthOfZeroKeepsLatestRecordForExpiration() throws Exception {
        pswdConfig.setPswdHistoryLength(0);
        changePassword("p1", "p2");

        assertEquals(1, historyCount());
        assertFalse(isInHistory("p1"));
        assertNotNull(lastPasswordChange());
    }

    @Test
    public void disabledHistoryStoresNothing() throws Exception {
        pswdConfig.setUsePswdHistory(false);
        changePassword("p1", "p2");

        assertEquals(0, historyCount());
    }

    private UUID createUser(String password) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            User user = metadata.create(User.class);
            String login = "pswdplus-test-" + user.getId();
            user.setLogin(login);
            user.setLoginLowerCase(login);
            user.setName("Test user");
            user.setGroup(em.getReference(Group.class, COMPANY_GROUP_ID));
            user.setPassword(hash(user.getId(), password));
            em.persist(user);
            tx.commit();
            return user.getId();
        }
    }

    private void changePassword(String... passwords) throws InterruptedException {
        for (String password : passwords) {
            //history records are ordered by change date, avoid two changes in the same millisecond
            Thread.sleep(5);
            userManagementService.changeUserPassword(userId, hash(userId, password));
        }
    }

    private boolean isInHistory(String password) {
        return passwordService.isPasswordInHistory(userId, hash(userId, password));
    }

    private long historyCount() {
        try (Transaction tx = persistence.createTransaction()) {
            Query query = persistence.getEntityManager().createQuery("select count(ph) from pswdplus$PasswordHistory ph where ph.user.id=:userId");
            query.setParameter("userId", userId);
            long count = (Long) query.getSingleResult();
            tx.commit();
            return count;
        }
    }

    //same query ExpireServiceBean uses to get the date of the last password change
    private Date lastPasswordChange() {
        try (Transaction tx = persistence.createTransaction()) {
            Query query = persistence.getEntityManager().createQuery("select max(ph.createdAt) from pswdplus$PasswordHistory ph where ph.user.id=:userId");
            query.setParameter("userId", userId);
            Date date = (Date) query.getSingleResult();
            tx.commit();
            return date;
        }
    }

    private String hash(UUID userId, String password) {
        return passwordEncryption.getPasswordHash(userId, password);
    }
}
