//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.Query;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;

import com.bloatit.data.queries.QueryCollection;
import com.bloatit.framework.exceptions.FatalErrorException;
import com.bloatit.framework.exceptions.NonOptionalParameterException;
import com.bloatit.framework.utils.PageIterable;

/**
 * An offer is a developer offer to a demand.
 */
@Entity
public  class DaoOffer extends DaoKudosable {

    /**
     * This is demand on which this offer is done.
     */
    @ManyToOne(optional = false)
    private DaoDemand demand;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
    @OrderBy("expirationDate ASC")
    private  List<DaoBatch> batches = new ArrayList<DaoBatch>();

    /**
     * The expirationDate is calculated from the batches variables.
     */
    @Basic(optional = false)
    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    @DateBridge(resolution = Resolution.DAY)
    private Date expirationDate;

    @Basic(optional = false)
    private int currentBatch;

    /**
     * The amount represents the money the member want to have to make his
     * offer. This is a calculated field used for performance speedup.
     * <code>(= foreach batches; amount += baches.getAmount)</code>
     */
    @Basic(optional = false)
    private BigDecimal amount;

    @Basic(optional = false)
    private boolean isDraft;

    // ======================================================================
    // Construction
    // ======================================================================

    /**
     * Create a DaoOffer.
     * 
     * @param member is the author of the offer. Must be non null.
     * @param demand is the demand on which this offer is made. Must be non
     *            null.
     * @throws NonOptionalParameterException if a parameter is null.
     * @throws FatalErrorException if the amount is < 0 or if the Date is in the
     *             future.
     */
    public DaoOffer( DaoMember member,
                     DaoDemand demand,
                     BigDecimal amount,
                     DaoDescription description,
                     Date dateExpire,
                     int secondsBeforeValidation) {
        super(member);
        if (demand == null) {
            throw new NonOptionalParameterException();
        }
        this.demand = demand;
        this.amount = BigDecimal.ZERO; // Will be updated by addBatch
        this.expirationDate = new Date();// Will be updated by addBatch
        this.currentBatch = 0;
        this.setDraft(true);
        addBatch(new DaoBatch(dateExpire, amount, description, this, secondsBeforeValidation));
    }

    public void cancelEverythingLeft() {
        for (int i = currentBatch; i < batches.size(); ++i) {
            batches.get(i).cancelBatch();
        }
        currentBatch = batches.size();
    }

    public void addBatch( DaoBatch batch) {
        if (isDraft() == false) {
            throw new FatalErrorException("You cannot add a batch on a non draft offer.");
        }
        amount = batch.getAmount().add(amount);
         Date expiration = batch.getExpirationDate();
        if (expirationDate.before(expiration)) {
            expirationDate = expiration;
        }
        batches.add(batch);
    }

    public boolean hasBatchesLeft() {
        return currentBatch < batches.size();
    }

    void passToNextBatch() {
        currentBatch++;
    }

    void batchHasARelease( DaoBatch batch) {
        // Find next batch. Passe it into developing state.
        for (int i = 0; i < batches.size(); ++i) {
            if (batches.get(i).equals(batch)) {
                if ((i + 1) < batches.size()) {
                    batches.get(i + 1).setDeveloping();
                }
                break;
            }
        }
    }

    public void setDraft( boolean isDraft) {
        this.isDraft = isDraft;
    }

    // ======================================================================
    // Getters
    // ======================================================================

    public boolean isDraft() {
        return isDraft;
    }

    /**
     * @return All the batches for this offer. (Even the MasterBatch).
     */
    public PageIterable<DaoBatch> getBatches() {
         String query = "from DaoBatch where offer = :this order by expirationDate, id";
         String queryCount = "select count(*) from DaoBatch where offer = :this";
        return new QueryCollection<DaoBatch>( //
                                             SessionManager.createQuery(query).setEntity("this", this),//
                                             SessionManager.createQuery(queryCount).setEntity("this", this));//
    }

    public DaoBatch getCurrentBatch() {
        return batches.get(currentBatch);
    }

    /**
     * @return a cloned version of the expirationDate attribute.
     */
    public Date getExpirationDate() {
        return (Date) expirationDate.clone();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    // TODO comment; it make sure the sum returned is 100.
    int getBatchPercent( DaoBatch current) {
        if (batches.size() == 1) {
            return 100;
        }

        int alreadyReturned = 0;
        for (int i = 0; i < batches.size(); ++i) {
            // Calculate the percent of the batch
             DaoBatch batch = batches.get(i);
             int percent = batch.getAmount().divide(amount, RoundingMode.HALF_EVEN).multiply(new BigDecimal("100")).intValue();
            if (current.equals(batch)) {
                // is the current is the last one
                if (i == (batches.size() - 1)) {
                    return 100 - alreadyReturned;
                }
                return percent;
            }
            // Save how much has been sent.
            alreadyReturned += percent;
        }
        throw new FatalErrorException("This offer has no batch, or the 'current' batch isn't found");
    }

    public boolean hasRelease() {
         Query query = SessionManager.createFilter(batches, "SELECT count(*) WHERE this.releases is not empty");
        return !((Long) query.uniqueResult()).equals(0L);
    }

    // ======================================================================
    // Visitor.
    // ======================================================================

    @Override
    public <ReturnType> ReturnType accept( DataClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    protected DaoOffer() {
        super();
    }

    public DaoDemand getDemand() {
        return demand;
    }

    // ======================================================================
    // equals and hashcode.
    // ======================================================================

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
         int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((demand == null) ? 0 : demand.hashCode());
        result = prime * result + ((expirationDate == null) ? 0 : expirationDate.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
         DaoOffer other = (DaoOffer) obj;
        if (amount == null) {
            if (other.amount != null) {
                return false;
            }
        } else if (!amount.equals(other.amount)) {
            return false;
        }
        if (demand == null) {
            if (other.demand != null) {
                return false;
            }
        } else if (!demand.equals(other.demand)) {
            return false;
        }
        if (expirationDate == null) {
            if (other.expirationDate != null) {
                return false;
            }
        } else if (!expirationDate.equals(other.expirationDate)) {
            return false;
        }
        return true;
    }

}
