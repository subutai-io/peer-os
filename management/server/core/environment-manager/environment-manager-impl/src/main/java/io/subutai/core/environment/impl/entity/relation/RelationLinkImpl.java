package io.subutai.core.environment.impl.entity.relation;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.common.security.relation.model.RelationLink;


/**
 * Created by talas on 12/8/15.
 */
@Entity
@Table( name = "relation_link" )
@Access( AccessType.FIELD )
public class RelationLinkImpl implements RelationLink
{
    @Id
    @Column( name = "item_id" )
    private String id;

    @Column( name = "unique_identifier" )
    private String uniqueIdentifier;

    @Column( name = "class_path" )
    private String classPath;


    public RelationLinkImpl()
    {
    }


    public RelationLinkImpl( final String uniqueIdentifier, final String classPath )
    {
        this.uniqueIdentifier = uniqueIdentifier;
        this.classPath = classPath;
        this.id = classPath + "|" + uniqueIdentifier;
    }


    @Override
    public String getId()
    {
        return id;
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


    public void setId( final String id )
    {
        this.id = id;
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

        final RelationLinkImpl trustItem = ( RelationLinkImpl ) o;

        if ( uniqueIdentifier != null ? !uniqueIdentifier.equals( trustItem.uniqueIdentifier ) :
             trustItem.uniqueIdentifier != null )
        {
            return false;
        }
        return !( classPath != null ? !classPath.equals( trustItem.classPath ) : trustItem.classPath != null );
    }


    @Override
    public int hashCode()
    {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + ( classPath != null ? classPath.hashCode() : 0 );
        return result;
    }


    @Override
    public String toString()
    {
        return "RelationLinkImpl{" +
                "id=" + id +
                ", uniqueIdentifier='" + uniqueIdentifier + '\'' +
                ", classPath='" + classPath + '\'' +
                '}';
    }
}
