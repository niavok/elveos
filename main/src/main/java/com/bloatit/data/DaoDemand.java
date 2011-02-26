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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import com.bloatit.common.Log;
import com.bloatit.data.exceptions.NotEnoughMoneyException;
import com.bloatit.data.queries.QueryCollection;
import com.bloatit.data.search.DaoDemandSearchFilterFactory;
import com.bloatit.framework.exceptions.FatalErrorException;
import com.bloatit.framework.exceptions.NonOptionalParameterException;
import com.bloatit.framework.utils.PageIterable;

/**
 * A DaoDemand is a kudosable content. It has a translatable description, and
 * can have a specification and some offers. The state of the demand is managed
 * by its super class DaoKudosable. On a demand we can add some comment and some
 * contriutions.
 */
@Entity
@Indexed
@FullTextFilterDef(name = "searchFilter", impl = DaoDemandSearchFilterFactory.class)
public  class DaoDemand extends DaoKudosable implements DaoCommentable {

    /**
     * This is the state of the demand. It's used in the workflow modeling. The
     * order is important !
     */
    public enum DemandState {
        /** No offers, waiting for money and offer */
        PENDING,

        /** One or more offer, waiting for money */
        PREPARING,

        /** Development in progress */
        DEVELOPPING,

        /** Something went wrong, the demand is canceled */
        DISCARDED,

        /** All is good, the developer is paid and the users are happy */
        FINISHED
    }

    /**
     * This is a calculated value with the sum of the value of all
     * contributions.
     */
    @Basic(optional = false)
    @Field(store = Store.NO)
    private BigDecimal contribution;

    @Basic(optional = false)
    @Field(store = Store.NO)
    @Enumerated
    private DemandState demandState;

    /**
     * A description is a translatable text with an title.
     */
    @OneToOne(optional = false)
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    private DaoDescription description;

    @OneToMany(mappedBy = "demand")
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    private  List<DaoOffer> offers = new ArrayList<DaoOffer>(0);

    @OneToMany(mappedBy = "demand")
    @Cascade(value = { CascadeType.ALL })
    private  List<DaoContribution> contributions = new ArrayList<DaoContribution>(0);

    @OneToMany
    @Cascade(value = { CascadeType.ALL })
    @OrderBy("id")
    @IndexedEmbedded
    private  List<DaoComment> comments = new ArrayList<DaoComment>(0);

    /**
     * The selected offer is the offer that is most likely to be validated and
     * used. If an offer is selected and has enough money and has a elapse time
     * done then this offer go into dev.
     */
    @ManyToOne(optional = true)
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    private DaoOffer selectedOffer;

    @ManyToOne
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    private DaoProject project;

    @Basic(optional = true)
    private Date validationDate;

    // ======================================================================
    // Construct.
    // ======================================================================

    /**
     * @see #DaoDemand(DaoMember, DaoDescription, DaoProject)
     */
    public static DaoDemand createAndPersist( DaoMember member,  DaoDescription description,  DaoProject project) {
         Session session = SessionManager.getSessionFactory().getCurrentSession();
         DaoDemand demand = new DaoDemand(member, description, project);
        try {
            session.save(demand);
        } catch ( HibernateException e) {
            session.getTransaction().rollback();
            SessionManager.getSessionFactory().getCurrentSession().beginTransaction();
            throw e;
        }
        return demand;
    }

    /**
     * Create a DaoDemand and set its state to the state PENDING.
     * 
     * @param member is the author of the demand
     * @param description is the description ...
     * @throws NonOptionalParameterException if any of the parameter is null.
     */
    private DaoDemand( DaoMember member,  DaoDescription description,  DaoProject project) {
        super(member);
        if (description == null || project == null) {
            throw new NonOptionalParameterException();
        }
        this.project = project;
        project.addDemand(this);
        this.description = description;
        this.validationDate = null;
        setSelectedOffer(null);
        this.contribution = BigDecimal.ZERO;
        setDemandState(DemandState.PENDING);
    }

    /**
     * Delete this DaoDemand from the database. "this" will remain, but
     * unmapped. (You shoudn't use it then)
     */
    public void delete() {
         Session session = SessionManager.getSessionFactory().getCurrentSession();
        session.delete(this);
    }

    @Override
    public void addComment( DaoComment comment) {
        comments.add(comment);
    }

    /**
     * Add a contribution to a demand.
     * 
     * @param member the author of the contribution
     * @param amount the > 0 amount of euros on this contribution
     * @param comment a <= 144 char comment on this contribution
     * @throws NotEnoughMoneyException
     */
    public void addContribution( DaoMember member,  BigDecimal amount,  String comment) throws NotEnoughMoneyException {
        if (amount == null) {
            throw new NonOptionalParameterException();
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            Log.data().fatal("Cannot create a contribution with this amount " + amount.toEngineeringString() + " by member " + member.getId());
            throw new FatalErrorException("The amount of a contribution cannot be <= 0.", null);
        }
        if (comment != null && comment.length() > DaoContribution.COMMENT_MAX_LENGTH) {
            Log.data().fatal("The comment of a contribution must be <= 144 chars long.");
            throw new FatalErrorException("Comments lenght of Contribution must be < 144.", null);
        }

        contributions.add(new DaoContribution(member, this, amount, comment));
        contribution = contribution.add(amount);
    }

    /**
     * Add a new offer for this demand. If there is no selected offer, select
     * this one.
     */
    public void addOffer( DaoOffer offer) {
        offers.add(offer);
    }

    /**
     * delete offer from this demand AND FROM DB !
     * 
     * @param offer the offer we want to delete.
     */
    public void removeOffer( DaoOffer offer) {
        offers.remove(offer);
        if (offer.equals(selectedOffer)) {
            selectedOffer = null;
        }
        SessionManager.getSessionFactory().getCurrentSession().delete(offer);
    }

    public void computeSelectedOffer() {
        selectedOffer = getCurrentOffer();
    }

    public void setSelectedOffer( DaoOffer selectedOffer) {
        this.selectedOffer = selectedOffer;
    }

    public void setValidationDate( Date validationDate) {
        this.validationDate = validationDate;
    }

    void validateContributions( int percent) {
        if (selectedOffer == null) {
            throw new FatalErrorException("The selectedOffer shouldn't be null here !");
        }
        if (percent == 0) {
            return;
        }
        for ( DaoContribution contribution : getContributions()) {
            try {
                if (contribution.getState() == DaoContribution.State.PENDING) {
                    contribution.validate(selectedOffer, percent);
                }
            } catch ( NotEnoughMoneyException e) {
                Log.data().fatal("Cannot validate contribution, not enought money.", e);
            }
        }
    }

    /**
     * Called by contribution when canceled.
     * 
     * @param amount
     */
    void cancelContribution( BigDecimal amount) {
        this.contribution = this.contribution.subtract(amount);
    }

    public void setDemandState( DemandState demandState) {
        this.demandState = demandState;
    }

    // ======================================================================
    // Getters.
    // ======================================================================

    public DaoDescription getDescription() {
        return description;
    }

    /**
     * The current offer is the offer with the max popularity then the min
     * amount.
     * 
     * @return the current offer for this demand, or null if there is no offer.
     */
    private DaoOffer getCurrentOffer() {
        // If there is no validated offer then we try to find a pending offer
         String queryString = "FROM DaoOffer " + //
                "WHERE demand = :this " + //
                "AND state <= :state " + // <= PENDING and VALIDATED.
                "AND popularity = (select max(popularity) from DaoOffer where demand = :this) " + //
                "AND popularity >= 0 " + //
                "ORDER BY amount ASC, creationDate DESC";
        try {
            return (DaoOffer) SessionManager.createQuery(queryString)
                                            .setEntity("this", this)
                                            .setParameter("state", DaoKudosable.PopularityState.PENDING)
                                            .iterate()
                                            .next();
        } catch ( NoSuchElementException e) {
            return null;
        }
    }

    public PageIterable<DaoOffer> getOffers() {
        return new MappedList<DaoOffer>(offers);
    }

    public DemandState getDemandState() {
        return demandState;
    }

    public PageIterable<DaoContribution> getContributions() {
        return new MappedList<DaoContribution>(contributions);
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.data.DaoCommentable#getCommentsFromQuery()
     */
    @Override
    public PageIterable<DaoComment> getComments() {
        return new MappedList<DaoComment>(comments);
    }

    @Override
    public DaoComment getLastComment() {
        return comments.get(comments.size() - 1);
    }

    public DaoOffer getSelectedOffer() {
        return selectedOffer;
    }

    public BigDecimal getContribution() {
        return contribution;
    }

    /**
     * @return the minimum value of the contribution on this demand.
     */
    public BigDecimal getContributionMin() {
        return (BigDecimal) SessionManager.createQuery("select min(f.amount) from DaoContribution as f where f.demand = :this")
                                          .setEntity("this", this)
                                          .uniqueResult();
    }

    /**
     * @return the maximum value of the contribution on this demand.
     */
    public BigDecimal getContributionMax() {
        return (BigDecimal) SessionManager.createQuery("select max(f.amount) from DaoContribution as f where f.demand = :this")
                                          .setEntity("this", this)
                                          .uniqueResult();
    }

    public Date getValidationDate() {
        return validationDate;
    }

    /**
     * @return the project
     */
    public DaoProject getProject() {
        return project;
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

    protected DaoDemand() {
        super();
    }

    // ======================================================================
    // equals hashcode.
    // ======================================================================

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
         int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((description == null) ? 0 : description.hashCode());
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
         DaoDemand other = (DaoDemand) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        return true;
    }

    public int countOpenBugs() {

         String q = "SELECT count(*) " + //
                "FROM com.bloatit.data.DaoOffer o " + //
                "JOIN o.batches as bs " + //
                "JOIN bs.bugs as b " + //
                "WHERE o = :selectedOffer " + //
                "AND b.state != :close ";//
         Query query = SessionManager.getSessionFactory().getCurrentSession().createQuery(q);
        query.setEntity("selectedOffer", selectedOffer);
        query.setParameter("close", DaoBug.BugState.RESOLVED);
        return ((Long) query.uniqueResult()).intValue();
    }

    public PageIterable<DaoBug> getOpenBugs() {

         String q = "SELECT b" + //
                " FROM com.bloatit.data.DaoOffer o " + //
                "JOIN o.batches as bs " + //
                "JOIN bs.bugs as b " + //
                "WHERE o = :selectedOffer " + //
                "AND b.state != :close ";//

         String qCount = "SELECT count(b)" + //
                " FROM com.bloatit.data.DaoOffer o " + //
                "JOIN o.batches as bs " + //
                "JOIN bs.bugs as b " + //
                "WHERE o = :selectedOffer " + //
                "AND b.state != :close ";//

         org.hibernate.classic.Session currentSession = SessionManager.getSessionFactory().getCurrentSession();
        return new QueryCollection<DaoBug>(currentSession.createQuery(q), currentSession.createQuery(qCount)).setEntity("selectedOffer",
                                                                                                                        selectedOffer)
                                                                                                             .setParameter("close",
                                                                                                                           DaoBug.BugState.RESOLVED);
    }

    public PageIterable<DaoBug> getClosedBugs() {
         String q = "SELECT b" + //
                " FROM com.bloatit.data.DaoOffer o " + //
                "JOIN o.batches as bs " + //
                "JOIN bs.bugs as b " + //
                "WHERE o = :selectedOffer " + //
                "AND o.demand = :this " + //
                "AND b.state = :close ";//

         String qCount = "SELECT count(b)" + //
                " FROM com.bloatit.data.DaoOffer o " + //
                "JOIN o.batches as bs " + //
                "JOIN bs.bugs as b " + //
                "WHERE o = :selectedOffer " + //
                "AND o.demand = :this " + //
                "AND b.state = :close ";//

         org.hibernate.classic.Session currentSession = SessionManager.getSessionFactory().getCurrentSession();
        return new QueryCollection<DaoBug>(currentSession.createQuery(q), currentSession.createQuery(qCount)).setEntity("selectedOffer",
                                                                                                                        selectedOffer)
                                                                                                             .setParameter("close",
                                                                                                                           DaoBug.BugState.RESOLVED)
                                                                                                             .setEntity("this", this);
    }

}
