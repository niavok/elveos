package com.bloatit.model;

import com.bloatit.framework.right.MoneyRight;
import com.bloatit.framework.right.RightManager.Action;
import com.bloatit.model.data.DaoAccount;
import com.bloatit.model.data.DaoExternalAccount;
import com.bloatit.model.data.DaoExternalAccount.AccountType;

public class ExternalAccount extends Account {

    private DaoExternalAccount dao;

    public ExternalAccount(Actor actor, AccountType type, String bankCode) {
        this.dao = DaoExternalAccount.createAndPersist(actor.getDao(), type, bankCode);
    }

    protected ExternalAccount(DaoExternalAccount dao) {
        super();
        this.dao = dao;
    }

    protected DaoExternalAccount getDao() {
        return dao;
    }

    public String getBankCode() {
        new MoneyRight.Everything().tryAccess(calculateRole(getActorUnprotected().getLogin()), Action.READ);
        return dao.getBankCode();
    }

    public AccountType getType() {
        new MoneyRight.Everything().tryAccess(calculateRole(getActorUnprotected().getLogin()), Action.READ);
        return dao.getType();
    }

    @Override
    protected DaoAccount getDaoAccount() {
        return null;
    }

}
