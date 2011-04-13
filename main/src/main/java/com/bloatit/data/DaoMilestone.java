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
import java.util.EnumSet;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Query;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.OrderBy;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;

import com.bloatit.data.DaoBug.BugState;
import com.bloatit.data.DaoBug.Level;
import com.bloatit.data.queries.QueryCollection;
import com.bloatit.framework.exceptions.highlevel.BadProgrammerException;
import com.bloatit.framework.exceptions.lowlevel.NonOptionalParameterException;
import com.bloatit.framework.utils.PageIterable;

/**
 * A DaoMilestone is a part of a DaoOffer.
 * 
 * @author Thomas Guyard
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//@formatter:off
@NamedQueries(value = { @NamedQuery(
                           name = "milestone.getBugs.byNonStateLevel",
                           query = "FROM DaoBug WHERE milestone = :this AND state != :state AND level = :level"),
                        @NamedQuery(
                           name = "milestone.getBugs.byNonState.size",
                           query = "SELECT count(*) FROM DaoBug WHERE milestone = :this AND state != :state"),
                        @NamedQuery(
                           name = "milestone.getBugs.byState",
                           query = "FROM DaoBug WHERE milestone = :this AND state = :state"),
                        @NamedQuery(
                            name = "milestone.getBugs.byState.size",
                            query = "SELECT count(*) FROM DaoBug WHERE milestone = :this AND state != :state"),
                        @NamedQuery(
                            name = "milestone.getBugs.byLevel",
                            query = "FROM DaoBug WHERE milestone = :this AND level = :level"),
                        @NamedQuery(
                            name = "milestone.getBugs.byLevel.size",
                            query = "SELECT count (*) FROM DaoBug WHERE milestone = :this AND level = :level"),
                        @NamedQuery(
                            name = "milestone.getBugs.byStateLevel",
                            query = "FROM DaoBug WHERE milestone = :this AND state = :state AND level = :level"),
                        @NamedQuery(
                            name = "milestone.getBugs.byStateLevel.size",
                            query = "SELECT count(*) FROM DaoBug WHERE milestone = :this AND state = :state AND level = :level"),
                     }
             )
// @formatter:on
public class DaoMilestone extends DaoIdentifiable {

    public enum MilestoneState {
        PENDING, DEVELOPING, UAT, VALIDATED, CANCELED
    }

    /**
     * After this date, the Milestone should be done.
     */
    @Basic(optional = false)
    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    @DateBridge(resolution = Resolution.DAY)
    @Column(updatable = false)
    private Date expirationDate;

    @Basic(optional = false)
    @Column(updatable = false)
    private int secondBeforeValidation;

    @Basic(optional = false)
    @Column(updatable = false)
    private int fatalBugsPercent;

    @Basic(optional = false)
    @Column(updatable = false)
    private int majorBugsPercent;

    /**
     * The amount represents the money the member want to have to make his
     * offer.
     */
    @Basic(optional = false)
    @Column(updatable = false)
    // @Cache(usage=CacheConcurrencyStrategy.READ_ONLY)
    private BigDecimal amount;

    // nullable.
    @Enumerated
    private Level levelToValidate;

    @Basic(optional = false)
    private MilestoneState milestoneState;

    /**
     * Remember a description is a title with some content. (Translatable)
     */
    @ManyToOne
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private DaoDescription description;

    @OneToMany(mappedBy = "milestone")
    @Cascade(value = { CascadeType.ALL })
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private final List<DaoBug> bugs = new ArrayList<DaoBug>();

    @OneToMany(mappedBy = "milestone")
    @Cascade(value = { CascadeType.ALL })
    @OrderBy(clause = "id DESC")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private final List<DaoRelease> releases = new ArrayList<DaoRelease>();

    @ManyToOne(optional = false)
    private DaoOffer offer;

    // ======================================================================
    // Construction.
    // ======================================================================

    /**
     * Create a DaoMilestone.
     * 
     * @param amount is the amount of the offer. Must be non null, and > 0.
     * @param dateExpire is the date when this offer should be finish. Must be
     *            non null, and in the future.
     * @param secondBeforeValidation number of seconds to wait until we watch
     *            for bugs and validate the milestone.
     * @throws NonOptionalParameterException if a parameter is null.
     * @throws BadProgrammerException if the amount is < 0 or if the Date is in
     *             the future.
     */
    public DaoMilestone(final Date dateExpire,
                        final BigDecimal amount,
                        final DaoDescription description,
                        final DaoOffer offer,
                        final int secondBeforeValidation) {
        super();
        if (dateExpire == null || amount == null || description == null || offer == null) {
            throw new NonOptionalParameterException();
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadProgrammerException("Amount must be > 0");
        }
        if (dateExpire.before(new Date())) {
            throw new BadProgrammerException("Make sure the date is in the future.");
        }
        this.expirationDate = (Date) dateExpire.clone();
        this.amount = amount;
        this.description = description;
        this.offer = offer;
        this.secondBeforeValidation = secondBeforeValidation;
        this.levelToValidate = Level.FATAL;
        this.fatalBugsPercent = 100;
        this.majorBugsPercent = 0;
        this.milestoneState = MilestoneState.PENDING;
    }

    /**
     * Set the percent of money the developer will received when all bugs of one
     * level are closed. This method take parameters for the Fatal and Major
     * level. The Minor level is calculated from it (see
     * {@link #getMinorBugsPercent()}).
     * 
     * @param fatalPercent is the percent of the money the developer will get
     *            when all the {@link Level#FATAL} bugs are closed. It must be
     *            >= 0 and <= 100.
     * @param majorPercent is the percent of the money the developer will get
     *            when all the {@link Level#MAJOR} bugs are closed. It must be
     *            >= 0 and <= 100.
     */
    public void updateMajorFatalPercent(final int fatalPercent, final int majorPercent) {
        if (fatalPercent < 0 || majorPercent < 0) {
            throw new BadProgrammerException("The parameters must be percents !");
        }
        if ((fatalPercent + majorPercent) > 100) {
            throw new BadProgrammerException("The sum of the two percent parameters is > 100 !");
        }
        this.fatalBugsPercent = fatalPercent;
        this.majorBugsPercent = majorPercent;
    }

    public void setDeveloping() {
        this.milestoneState = MilestoneState.DEVELOPING;
    }

    public void addRelease(final DaoRelease release) {
        this.releases.add(release);
        if (this.milestoneState == MilestoneState.DEVELOPING) {
            this.milestoneState = MilestoneState.UAT;
        }
        getOffer().milestoneHasARelease(this);
    }

    public void addBug(final DaoBug bug) {
        this.bugs.add(bug);
    }

    /**
     * Tells that the Income state of this milestone is finished, and everything
     * is OK. The validation can be partial (when some major or minor bugs are
     * open). The validate method may also validate nothing if some FATAL bugs
     * are open, or if the validation period is not open. You can change this
     * behavior using the <code>force</code> parameter. The force parameter
     * allows to validate the milestone without taking into account these
     * previous restrictions.
     * 
     * @param force force the validation of this milestone. Do not take care of
     *            the bugs and the timeOuts.
     * @return true if all parts of this milestone is validated.
     */
    public boolean validate(final boolean force) {
        //
        // Calculate the real percent (= percent of this milestone * percent of
        // this
        // level).
        final int milestonePercent = this.offer.getMilestonePercent(this);
        final int fatalPercent = (milestonePercent * this.fatalBugsPercent) / 100;
        final int majorPercent = (milestonePercent * this.majorBugsPercent) / 100;
        final int minorPercent = milestonePercent - majorPercent - fatalPercent;

        //
        // Do the validation
        //
        if (this.levelToValidate == Level.FATAL && (force || shouldValidatePart(Level.FATAL))) {
            this.levelToValidate = Level.MAJOR;
            this.offer.getFeature().validateContributions(fatalPercent);
        }
        // if fatalBugPercent == 100, there is nothing left to validate so it is
        // automatically validated.
        if (this.levelToValidate == Level.MAJOR && (force || shouldValidatePart(Level.MAJOR) || this.fatalBugsPercent == 100)) {
            this.levelToValidate = Level.MINOR;
            this.offer.getFeature().validateContributions(majorPercent);
        }
        // when minorBugPercent == 0, there is nothing left to validate so it is
        // automatically validated.
        if (this.levelToValidate == Level.MINOR && (force || shouldValidatePart(Level.MINOR) || getMinorBugsPercent() == 0)) {
            this.levelToValidate = null;
            this.offer.getFeature().validateContributions(minorPercent);
        }
        if (this.levelToValidate == null) {
            this.milestoneState = MilestoneState.VALIDATED;
            this.offer.passToNextMilestone();
            return true;
        }
        return false;
    }

    /**
     * You can validate a milestone after its release and when the bugs
     * requirement are done.
     * 
     * @return true if an admin should validate this Milestone part. False
     *         otherwise.
     */
    public boolean shouldValidatePart(final Level level) {
        if (validationPeriodFinished() && getNonResolvedBugs(level).size() == 0) {
            return true;
        }
        return false;
    }

    public boolean partIsValidated(final Level level) {
        return this.levelToValidate == null || !EnumSet.range(this.levelToValidate, Level.MINOR).contains(level);
    }

    private boolean validationPeriodFinished() {
        final Date releasedDate = getReleasedDate();
        if (releasedDate == null) {
            return false;
        }
        return new Date(releasedDate.getTime() + ((long) this.secondBeforeValidation) * 1000).before(new Date());
    }

    public void cancelMilestone() {
        this.milestoneState = MilestoneState.CANCELED;
    }

    // ======================================================================
    // Getters.
    // ======================================================================

    public PageIterable<DaoBug> getNonResolvedBugs(final Level level) {
        return new QueryCollection<DaoBug>("milestone.getBugs.byNonStateLevel").setEntity("this", this)
                                                                               .setParameter("level", level)
                                                                               .setParameter("state", BugState.RESOLVED);
    }

    public PageIterable<DaoBug> getBugs(final Level level) {
        return new QueryCollection<DaoBug>("milestone.getBugs.byLevel").setEntity("this", this).setParameter("level", level);
    }

    public PageIterable<DaoBug> getBugs(final BugState state) {
        return new QueryCollection<DaoBug>("milestone.getBugs.byState").setEntity("this", this).setParameter("state", state);
    }

    public PageIterable<DaoBug> getBugs(final Level level, final BugState state) {
        return new QueryCollection<DaoBug>("milestone.getBugs.byStateLevel").setEntity("this", this)
                                                                            .setParameter("level", level)
                                                                            .setParameter("state", state);
    }

    public PageIterable<DaoRelease> getReleases() {
        return new MappedList<DaoRelease>(this.releases);
    }

    public Date getExpirationDate() {
        return (Date) this.expirationDate.clone();
    }

    public MilestoneState getMilestoneState() {
        return this.milestoneState;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public DaoDescription getDescription() {
        return this.description;
    }

    public DaoOffer getOffer() {
        return this.offer;
    }

    /**
     * @return the releaseDate
     */
    public Date getReleasedDate() {
        final Query query = SessionManager.createFilter(this.releases, "select max(creationDate)");
        return (Date) query.uniqueResult();
    }

    /**
     * @return the fatalBugsPercent
     */
    public int getFatalBugsPercent() {
        return this.fatalBugsPercent;
    }

    /**
     * @return the majorBugsPercent
     */
    public int getMajorBugsPercent() {
        return this.majorBugsPercent;
    }

    /**
     * @return the getMinorBugsPercent (= 100 - (majorBugsPercent +
     *         fatalBugsPercent)).
     */
    public int getMinorBugsPercent() {
        return 100 - (this.majorBugsPercent + this.fatalBugsPercent);
    }

    // ======================================================================
    // Visitor.
    // ======================================================================

    @Override
    public <ReturnType> ReturnType accept(final DataClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    protected DaoMilestone() {
        super();
    }

    // ======================================================================
    // equals and hashCode.
    // ======================================================================

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.amount == null) ? 0 : this.amount.hashCode());
        result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
        result = prime * result + ((this.expirationDate == null) ? 0 : this.expirationDate.hashCode());
        result = prime * result + this.fatalBugsPercent;
        result = prime * result + this.majorBugsPercent;
        result = prime * result + ((this.offer == null) ? 0 : this.offer.hashCode());
        result = prime * result + this.secondBeforeValidation;
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DaoMilestone other = (DaoMilestone) obj;
        if (this.amount == null) {
            if (other.amount != null) {
                return false;
            }
        } else if (!this.amount.equals(other.amount)) {
            return false;
        }
        if (this.description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!this.description.equals(other.description)) {
            return false;
        }
        if (this.expirationDate == null) {
            if (other.expirationDate != null) {
                return false;
            }
        } else if (!this.expirationDate.equals(other.expirationDate)) {
            return false;
        }
        if (this.fatalBugsPercent != other.fatalBugsPercent) {
            return false;
        }
        if (this.majorBugsPercent != other.majorBugsPercent) {
            return false;
        }
        if (this.offer == null) {
            if (other.offer != null) {
                return false;
            }
        } else if (!this.offer.equals(other.offer)) {
            return false;
        }
        if (this.secondBeforeValidation != other.secondBeforeValidation) {
            return false;
        }
        return true;
    }

}
