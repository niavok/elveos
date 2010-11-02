package com.bloatit.framework.lists;

import java.util.Iterator;

import com.bloatit.framework.Demand;
import com.bloatit.model.data.DaoDemand;

public class DemandIterator extends com.bloatit.framework.lists.IteratorBinder<Demand, DaoDemand> {

    public DemandIterator(Iterable<DaoDemand> daoIterator) {
        super(daoIterator);
    }

    public DemandIterator(Iterator<DaoDemand> daoIterator) {
        super(daoIterator);
    }

    @Override
    protected Demand createFromDao(DaoDemand dao) {
        return Demand.create(dao);
    }

}