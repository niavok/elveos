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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.bloatit.framework.exceptions.NonOptionalParameterException;

@Entity
public  class DaoFileMetadata extends DaoUserContent {

    public enum FileType {
        TEXT, HTML, TEX, PDF, ODT, DOC, BMP, JPG, PNG, SVG, UNKNOWN
    }

    @Basic(optional = false)
    private String filename;

    @Basic(optional = false)
    private String url;

    @Basic(optional = false)
    private int size;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Basic(optional = false)
    @Enumerated
    private FileType type;

    @OneToOne(optional = true, mappedBy = "file")
    private DaoImage image;

    @ManyToOne(optional = true)
    private DaoUserContent relatedContent;

    public static DaoFileMetadata createAndPersist( DaoMember member,
                                                    DaoUserContent relatedContent,
                                                    String filename,
                                                    String url,
                                                    FileType type,
                                                    int size) {
         Session session = SessionManager.getSessionFactory().getCurrentSession();
         DaoFileMetadata file = new DaoFileMetadata(member, relatedContent, filename, url, type, size);
        try {
            session.save(file);
        } catch ( HibernateException e) {
            session.getTransaction().rollback();
            SessionManager.getSessionFactory().getCurrentSession().beginTransaction();
            throw e;
        }
        return file;
    }

    /**
     * @param member is the author (the one who uploaded the file)
     * @param relatedContent can be null. It is the content with which this file
     *            has been uploaded.
     * @param filename is the name of the file (with its extension, but without
     *            its whole folder path)
     * @param directory is the path of the directory where the file is.
     * @param type is the type of the file (found using its extension or
     *            mimetype)
     * @param size is the size of the file.
     */
    private DaoFileMetadata( DaoMember member,
                             DaoUserContent relatedContent,
                             String filename,
                             String url,
                             FileType type,
                             int size) {
        super(member);
        if (filename == null || url == null || type == null || filename.isEmpty() || url.isEmpty()) {
            throw new NonOptionalParameterException();
        }
        this.size = size;
        this.filename = filename;
        this.url = url;
        this.type = type;
        this.shortDescription = null;
        this.relatedContent = relatedContent;
        if (relatedContent != null) {
            relatedContent.addFile(this);
        }
        // At the end to make sure the assignment are done.
        // It works only if equal is  !!
        if (equals(relatedContent)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param shortDescription the shortDescription to set
     */
    public  void setShortDescription( String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Tells that the current File is an image. Used in DaoImage constructor.
     * 
     * @param image the image to set.
     */
    void setImage( DaoImage image) {
        this.image = image;
    }

    /**
     * If the file is an image, it should be associated with a DaoImage object.
     * 
     * @return the image object associated with this file. It can be null.
     */
    public DaoImage getImage() {
        return image;
    }

    /**
     * @return the url.
     */
    public  String getUrl() {
        return url;
    }

    /**
     * @return the shortDescription
     */
    public  String getShortDescription() {
        return shortDescription;
    }

    /**
     * @return the filename
     */
    public  String getFilename() {
        return filename;
    }

    /**
     * @return the size
     */
    public  int getSize() {
        return size;
    }

    public FileType getType() {
        return type;
    }
    
    public DaoUserContent getRelatedContent() {
        return relatedContent;
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

    protected DaoFileMetadata() {
        // for hibernate.
    }

    // ======================================================================
    // equals hashcode.
    // ======================================================================

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public  int hashCode() {
         int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public  boolean equals( Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
         DaoFileMetadata other = (DaoFileMetadata) obj;
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        if (filename == null) {
            if (other.filename != null) {
                return false;
            }
        } else if (!filename.equals(other.filename)) {
            return false;
        }
        return true;
    }

}
