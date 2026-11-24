package ar.com.osmosys.pswdplus.service;

import ar.com.osmosys.pswdplus.config.PswdConfig;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Service(PasswordService.NAME)
public class PasswordServiceBean implements PasswordService {


    @Inject
    private Persistence persistence;

    @Inject
    private PswdConfig pswdConfig;

    @Override
    public boolean isPasswordInHistory(UUID userId, String passwordHash)
    {
        List<String> history;

        Transaction tx=persistence.createTransaction();
        try {
            EntityManager em=persistence.getEntityManager();

            Query qHistory=em.createQuery("select ph.passwordHash from pswdplus$PasswordHistory ph where ph.user.id=:userId order by ph.createdAt desc");
            qHistory.setParameter("userId",userId);
            history=qHistory.getResultList();

            tx.commit();
        } finally {
            tx.end();
        }

        return isInRecentHistory(history,passwordHash,pswdConfig.getPswdHistoryLength());

    }

    /*
    A new password is validated against the N newest used passwords
    (the current one is checked apart, against SEC_USER), so older records
    (e.g. left over after decreasing the history length) must not be considered
     */
    static boolean isInRecentHistory(List<String> newestFirst, String passwordHash, int historyLength)
    {
        int window=Math.min(newestFirst.size(),Math.max(0,historyLength));

        return newestFirst.subList(0,window).contains(passwordHash);
    }


}
