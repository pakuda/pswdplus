package ar.com.osmosys.pswdplus.service;

import ar.com.osmosys.pswdplus.config.PswdConfig;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.security.entity.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service(ExpireService.NAME)
public class ExpireServiceBean implements ExpireService, Callable {


    @Inject
    PswdConfig pswdConfig;

    @Inject
    Persistence persistence;



    public String expireOldPasswords()
    {

        String expiredUsers="";

        if(pswdConfig.getUsePswdExpiration())
        {
            int days=pswdConfig.getDaysToPswdExpiration();
            Transaction tx=persistence.createTransaction();
            EntityManager em=persistence.getEntityManager();
            List<User> users;

            users=em.createQuery("select u from sec$User u where u.changePasswordAtNextLogon<>1").getResultList();


            for (User user:users)
            {
                Query qLatestPwdChange=em.createQuery("select max(ph.createdAt) from pswdplus$PasswordHistory ph where ph.user.id=:userId");
                qLatestPwdChange.setParameter("userId", user.getId());

                try{
                    Date lastPasswordChange=(Date)qLatestPwdChange.getSingleResult();
                    if (isPasswordExpired(lastPasswordChange,days))
                    {
                        user.setChangePasswordAtNextLogon(true);
                        expiredUsers=expiredUsers + user.getLogin() + ",";
                    }
                }
                catch (NullPointerException e)
                {
                    //no lastPasswordChange date for that user
                }

            }

            tx.commit();

        }

        return expiredUsers;
    }

    private boolean isPasswordExpired(Date lastChange, int validityDays)
    {
        long diff=new Date().getTime() - lastChange.getTime();
        long days= TimeUnit.DAYS.convert(diff,TimeUnit.MILLISECONDS);
        long validityDaysLong=validityDays;

        return (days>=validityDays);

    }

    public Object call() throws Exception
    {
        return null;
    }




}