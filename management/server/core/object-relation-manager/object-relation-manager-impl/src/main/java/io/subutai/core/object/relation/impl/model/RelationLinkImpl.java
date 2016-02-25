package io.subutai.core.object.relation.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Preconditions;

import io.subutai.common.security.relation.RelationLink;


/**
 * Created by talas on 12/8/15.
 */
@Entity
@Table( name = "relation_link" )
@Access( AccessType.FIELD )
public class RelationLinkImpl implements RelationLink
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column(name = "rl_id")
    private long id;

    @Column( name = "link_id" )
    private String linkId;

    @Column( name = "unique_identifier" )
    private String uniqueIdentifier;

    @Column( name = "class_path" )
    private String classPath;

    @Column( name = "context" )
    private String context = "";


    public RelationLinkImpl()
    {
    }


    public RelationLinkImpl( final String uniqueIdentifier, final String classPath, final String context )
    {
        this.uniqueIdentifier = uniqueIdentifier;
        this.classPath = classPath;
        this.linkId = classPath + "|" + uniqueIdentifier;
        this.context = context;
    }


    public RelationLinkImpl( RelationLink relationLink )
    {
        Preconditions.checkNotNull( relationLink, "Error relationLink is null." );
        this.uniqueIdentifier = relationLink.getUniqueIdentifier();
        this.classPath = relationLink.getClassPath();
        this.linkId = relationLink.getLinkId();
        this.context = relationLink.getContext();
    }


    @Override
    public String getLinkId()
    {
        return linkId;
    }


    @Override
    public String getUniqueIdentifier()
    {
        return uniqueIdentifier;
    }


    @Override
    public String getClassPath()
    {
        return classPath;
    }


    @Override
    public String getContext()
    {
        return context;
    }


    public void setContext( final String context )
    {
        this.context = context;
    }


    public void setLinkId( final String id )
    {
        this.linkId = id;
    }


    public void setUniqueIdentifier( final String uniqueIdentifier )
    {
        this.uniqueIdentifier = uniqueIdentifier;
    }


    public void setClassPath( final String classPath )
    {
        this.classPath = classPath;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof RelationLinkImpl ) )
        {
            return false;
        }

        final RelationLinkImpl that = ( RelationLinkImpl ) o;

        return linkId != null ? linkId.equals( that.linkId ) : that.linkId == null;
    }


    @Override
    public int hashCode()
    {
        return linkId != null ? linkId.hashCode() : 0;
    }


    @Override
    public String toString()
    {
        return "RelationLinkImpl{" +
                "id=" + id +
                ", linkId='" + linkId + '\'' +
                ", uniqueIdentifier='" + uniqueIdentifier + '\'' +
                ", classPath='" + classPath + '\'' +
                ", context='" + context + '\'' +
                '}';
    }
}
