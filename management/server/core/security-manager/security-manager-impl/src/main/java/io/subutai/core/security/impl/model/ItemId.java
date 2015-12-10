package io.subutai.core.security.impl.model;


import javax.persistence.Embeddable;


/**
 * Created by talas on 12/10/15.
 */
@Embeddable
public class ItemId
{
    private String uniqueIdentifier;
    private String classPath;


    public ItemId()
    {
    }


    public ItemId( final String uniqueIdentifier, final String classPath )
    {
        this.uniqueIdentifier = uniqueIdentifier;
        this.classPath = classPath;
    }


    public String getUniqueIdentifier()
    {
        return uniqueIdentifier;
    }


    public void setUniqueIdentifier( final String uniqueIdentifier )
    {
        this.uniqueIdentifier = uniqueIdentifier;
    }


    public String getClassPath()
    {
        return classPath;
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
        if ( !( o instanceof ItemId ) )
        {
            return false;
        }

        final ItemId itemId = ( ItemId ) o;

        if ( uniqueIdentifier != null ? !uniqueIdentifier.equals( itemId.uniqueIdentifier ) :
             itemId.uniqueIdentifier != null )
        {
            return false;
        }
        return !( classPath != null ? !classPath.equals( itemId.classPath ) : itemId.classPath != null );
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
        return "ItemId{" +
                "uniqueIdentifier='" + uniqueIdentifier + '\'' +
                ", classPath='" + classPath + '\'' +
                '}';
    }
}
