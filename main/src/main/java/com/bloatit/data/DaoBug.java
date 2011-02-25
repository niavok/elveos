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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.bloatit.framework.exceptions.NonOptionalParameterException;
import com.bloatit.framework.utils.PageIterable;

/**
 * Represent a bug on a Released part of a batch. It is like a bug in a
 * bugtracker.
 */
@Entity
public final class DaoBug extends DaoUserContent implements DaoCommentable {

    /**
     * Criticality of the bug. A {@link Level#FATAL} error is a very important
     * error etc.
     */
    public enum Level {
        FATAL, MAJOR, MINOR
    }

    /**
     * It is the state of the bug ... The developer change it during the process
     * of fixing it.
     */
    public enum BugState {
        PENDING, DEVELOPING, RESOLVED
    }

    @Basic(optional = false)
    private String title;

    @Basic(optional = false)
    private String description;

    /**
     * This is the language in which the description is written.
     */
    @Basic(optional = false)
    private Locale locale;

    @Basic(optional = false)
    @Enumerated
    private Level errorLevel;

    @OneToMany
    @Cascade(value = { CascadeType.ALL })
    private final Set<DaoComment> comments = new HashSet<DaoComment>();

    @ManyToOne(optional = false)
    private DaoBatch batch;

    @Basic(optional = false)
    @Enumerated
    private BugState state;

    public DaoBug(final DaoMember member, final DaoBatch batch, final String title, final String description, final Locale locale, final Level level) {
        super(member);
        if (title == null || description == null || batch == null || locale == null || level == null || description.isEmpty()) {
            throw new NonOptionalParameterException();
        }
        this.batch = batch;
        this.title = title;
        this.description = description;
        this.locale = locale;
        this.errorLevel = level;
        this.state = BugState.PENDING;
    }

    public static DaoBug createAndPersist(final DaoMember member,
                                          final DaoBatch batch,
                                          final String title,
                                          final String description,
                                          final Locale locale,
                                          final Level level) {
        final Session session = SessionManager.getSessionFactory().getCurrentSession();
        final DaoBug bug = new DaoBug(member, batch, title, description, locale, level);
        try {
            session.save(bug);
        } catch (final HibernateException e) {
            session.getTransaction().rollback();
            SessionManager.getSessionFactory().getCurrentSession().beginTransaction();
            throw e;
        }
        return bug;
    }

    @Override
    public void addComment(final DaoComment comment) {
        comments.add(comment);
    }

    public void setErrorLevel(final Level level) {
        this.errorLevel = level;
    }

    /**
     * The person assigned to a bug is the developer (the member that has
     * created the offer). The person assigned to a bug is the developer (the
     * member that has created the offer).
     * 
     * @return the member assigned to this bug.
     */
    public DaoMember getAssignedTo() {
        return getBatch().getOffer().getAuthor();
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return the errorLevel
     */
    public Level getErrorLevel() {
        return errorLevel;
    }

    public DaoBatch getBatch() {
        return batch;
    }

    public BugState getState() {
        return state;
    }

    public void setResolved() {
        state = BugState.RESOLVED;
    }

    public void setDeveloping() {
        state = BugState.DEVELOPING;
    }

    /**
     * @return the comments
     */
    @Override
    public PageIterable<DaoComment> getComments() {
        return CommentManager.getComments(comments);
    }

    /**
     * @return the last comment
     */
    @Override
    public DaoComment getLastComment() {
        return CommentManager.getLastComment(comments);
    }

    // ======================================================================
    // Visitor.
    // ======================================================================

    @Override
    public <ReturnType> ReturnType accept(final DataClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

    // ======================================================================
    // Hibernate mapping
    // ======================================================================

    protected DaoBug() {
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
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
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
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DaoBug other = (DaoBug) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        return true;
    }
}
