package com.bloatit.model.data;

import java.math.BigDecimal;

import junit.framework.TestCase;

import com.bloatit.model.data.DaoExternalAccount.AccountType;
import com.bloatit.model.data.util.SessionManager;
import com.bloatit.model.exceptions.NotEnoughMoneyException;

public class TransactionTest extends TestCase {

    public void testCreateAndPersist() {
        SessionManager.beginWorkUnit();

        try {
            yo.getInternalAccount().setAmount(new BigDecimal("200"));
            fred.getInternalAccount().setAmount(new BigDecimal("200"));

            DaoTransaction.createAndPersist(yo.getInternalAccount(), tom.getInternalAccount(), new BigDecimal("120"));
            assertEquals(0, yo.getInternalAccount().getAmount().compareTo(new BigDecimal("80")));
            assertEquals(0, tom.getInternalAccount().getAmount().compareTo(new BigDecimal("120")));

            SessionManager.flush();
            b219.setExternalAccount(DaoExternalAccount.createAndPersist(b219, AccountType.IBAN, "plop"));

            DaoTransaction.createAndPersist(fred.getInternalAccount(), b219.getExternalAccount(), new BigDecimal("120"));
            assertEquals(0, fred.getInternalAccount().getAmount().compareTo(new BigDecimal("80")));
            assertEquals(0, b219.getExternalAccount().getAmount().compareTo(new BigDecimal("120")));
        } catch (NotEnoughMoneyException e) {
            fail();
        }

        SessionManager.endWorkUnitAndFlush();
    }

    private DaoMember yo;
    private DaoMember tom;
    private DaoMember fred;

    private DaoGroup b219;

    protected void setUp() throws Exception {
        super.setUp();
        SessionManager.reCreateSessionFactory();
        SessionManager.beginWorkUnit();
        {
            tom = DaoMember.createAndPersist("Thomas", "password", "tom@gmail.com");
            tom.setFirstname("Thomas");
            tom.setLastname("Guyard");
            SessionManager.flush();
        }
        {
            fred = DaoMember.createAndPersist("Fred", "other", "fred@gmail.com");
            fred.setFirstname("Frédéric");
            fred.setLastname("Bertolus");
            SessionManager.flush();
        }
        {
            yo = DaoMember.createAndPersist("Yo", "plop", "yo@gmail.com");
            yo.setFirstname("Yoann");
            yo.setLastname("Plénet");
            SessionManager.flush();

            DaoGroup.createAndPersiste("Other", "plop@plop.com", DaoGroup.Right.PUBLIC).addMember(yo, false);
            DaoGroup.createAndPersiste("myGroup", "plop@plop.com", DaoGroup.Right.PUBLIC).addMember(yo, false);
            (b219 = DaoGroup.createAndPersiste("b219", "plop@plop.com", DaoGroup.Right.PRIVATE)).addMember(yo, true);
        }

        SessionManager.endWorkUnitAndFlush();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        if (SessionManager.getSessionFactory().getCurrentSession().getTransaction().isActive()) {
            SessionManager.endWorkUnitAndFlush();
        }
        SessionManager.getSessionFactory().close();
    }

}
