package ar.com.osmosys.pswdplus.service;

import ar.com.osmosys.pswdplus.config.PswdConfig;
import ar.com.osmosys.pswdplus.entity.PasswordHistory;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.EntityAccessException;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.PasswordEncryption;
import com.haulmont.cuba.security.app.UserManagementServiceBean;
import com.haulmont.cuba.security.entity.User;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExtUserManagementServiceBean extends UserManagementServiceBean {

    private static final String CHANGE_PASSWORD_VIEW = "user.changePassword";

    @Inject
    PswdConfig pswdConfig;

    @Inject
    PasswordEncryption passwordEncryption;

    /*
    Adds an entry into PasswordHistory table. Later on,
    we'll check against the new password against the
    historical passwords
     */
    @Override
    public void changeUserPassword(UUID userId, String newPasswordHash) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId, CHANGE_PASSWORD_VIEW);
            if (user == null) {
                throw new EntityAccessException(User.class, userId);
            }

            Date changeDate=new Date();

            if(pswdConfig.getUsePswdHistory())
            {
                //we remove every historic password that falls out of the history window...
                Query qHistory=em.createQuery("select ph from pswdplus$PasswordHistory ph where ph.user.id=:userId order by ph.createdAt desc");
                qHistory.setParameter("userId", userId);
                List<PasswordHistory> history=qHistory.getResultList();

                em.setSoftDeletion(false);
                for(PasswordHistory ph : outdatedPasswords(history, pswdConfig.getPswdHistoryLength()))
                    em.remove(ph);

                //...and store the current password into the password history
                PasswordHistory passwordHistory=metadata.create(PasswordHistory.class);
                passwordHistory.setCreatedAt(changeDate);
                passwordHistory.setPasswordHash(user.getPassword());
                passwordHistory.setUser(user);
                em.persist(passwordHistory);

            }

            user.setPassword(newPasswordHash);
            user.setChangePasswordAtNextLogon(false);

            // reset remember me for user
            Query query = em.createQuery("delete from sec$RememberMeToken rt where rt.user.id=:userId");
            query.setParameter("userId", userId);
            query.executeUpdate();

            tx.commit();

            //missing catch in case of query errors...

        } finally {
            tx.end();
        }
    }

    /*
    Given the user's password history (newest first), returns the records
    to remove before storing the password being replaced, so that the history
    keeps the N newest used passwords. At least one record is always kept,
    since its date is used to compute password expiration
     */
    static <T> List<T> outdatedPasswords(List<T> newestFirst, int historyLength)
    {
        //the password being replaced takes one of the slots to keep
        int keep=Math.max(1,historyLength)-1;

        if(newestFirst.size()<=keep)
            return Collections.emptyList();

        return newestFirst.subList(keep,newestFirst.size());
    }


}
