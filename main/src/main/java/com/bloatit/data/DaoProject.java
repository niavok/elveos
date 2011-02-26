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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.bloatit.data.queries.QueryCollection;
import com.bloatit.framework.exceptions.NonOptionalParameterException;
import com.bloatit.framework.utils.PageIterable;

@Entity
public  class DaoProject extends DaoIdentifiable {

    @Column(nullable = false, unique = true, updatable = false)
    private String name;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private DaoDescription description;

    @ManyToOne(optional = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("id")
    private DaoFileMetadata image;

    @OneToMany(mappedBy = "project")
    private  List<DaoDemand> demands = new ArrayList<DaoDemand>();

    // ======================================================================
    // Static HQL requests
    // ======================================================================

    public static DaoProject getByName( String name) {
         Query query = SessionManager.createQuery("from DaoProject where name = :name").setString("name", name);
        return (DaoProject) query.uniqueResult();
    }

    // ======================================================================
    // Construction
    // ======================================================================

    public static DaoProject createAndPersist( String name,  DaoDescription description) {
         Session session = SessionManager.getSessionFactory().getCurrentSession();
         DaoProject project = new DaoProject(name, description);
        try {
            session.save(project);
        } catch ( HibernateException e) {
            session.getTransaction().rollback();
            SessionManager.getSessionFactory().getCurrentSession().beginTransaction();
            throw e;
        }
        return project;
    }

    private DaoProject( String name,  DaoDescription description) {
        super();
        if (name == null || name.isEmpty() || description == null) {
            throw new NonOptionalParameterException();
        }
        this.name = name;
        this.description = description;
    }

    protected void addDemand( DaoDemand demand) {
        demands.add(demand);
    }

    // ======================================================================
    // Getters
    // ======================================================================

    /**
     * @return the description
     */
    public DaoDescription getDescription() {
        return description;
    }

    /**
     * @return the image
     */
    public DaoFileMetadata getImage() {
        return image;
    }

    public PageIterable<DaoDemand> getDemands() {
        return new MappedList<DaoDemand>(demands);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    // ======================================================================
    // Visitor.
    // ======================================================================

    @Override
    public <ReturnType> ReturnType accept( DataClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

    // ======================================================================
    // For Hibernate mapping.
    // ======================================================================

    protected DaoProject() {
        super();
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
        int result = 1;
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
         DaoProject other = (DaoProject) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }

        return true;
    }

    public void setImage( DaoFileMetadata image) {
        this.image = image;
    }

}
