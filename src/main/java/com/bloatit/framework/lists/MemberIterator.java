package com.bloatit.framework.lists;

import java.util.Iterator;

import com.bloatit.framework.Member;
import com.bloatit.model.data.DaoMember;

public class MemberIterator extends IteratorBinder<Member, DaoMember> {

    public MemberIterator(Iterable<DaoMember> daoIterator) {
        super(daoIterator);
    }

    public MemberIterator(Iterator<DaoMember> daoIterator) {
        super(daoIterator);
    }

    @Override
    protected Member createFromDao(DaoMember dao) {
        return Member.create(dao);
    }

}
