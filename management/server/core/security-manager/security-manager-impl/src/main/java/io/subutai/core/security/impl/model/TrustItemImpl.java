package io.subutai.core.security.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.security.api.model.TrustItem;


/**
 * Created by talas on 12/8/15.
 */
@Entity
@Table( name = "trust_item" )
@Access( AccessType.FIELD )
public class TrustItemImpl implements TrustItem
{
    @Id
    @Column( name = "item_id" )
    private String id;

    @Column( name = "unique_identifier" )
    private String uniqueIdentifier;

    @Column( name = "class_path" )
    private String classPath;


    public TrustItemImpl()
    {
    }


    public TrustItemImpl( final String uniqueIdentifier, final String classPath )
    {
        this.uniqueIdentifier = uniqueIdentifier;
        this.classPath = classPath;
        this.id = classPath + "|" + uniqueIdentifier;
    }


    public String getId()
    {
        return id;
    }


    public String getUniqueIdentifier()
    {
        return uniqueIdentifier;
    }


    public String getClassPath()
    {
        return classPath;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TrustItemImpl ) )
        {
            return false;
        }

        final TrustItemImpl trustItem = ( TrustItemImpl ) o;

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
        return "TrustItemImpl{" +
                "id=" + id +
                ", uniqueIdentifier='" + uniqueIdentifier + '\'' +
                ", classPath='" + classPath + '\'' +
                '}';
    }
}
