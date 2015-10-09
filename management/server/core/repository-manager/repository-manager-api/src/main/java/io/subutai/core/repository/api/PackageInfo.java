package io.subutai.core.repository.api;


import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;


/**
 * Contains package info
 */
public class PackageInfo
{
    private String name;
    private String description;


    public PackageInfo( final String name, final String description )
    {
        Preconditions.checkNotNull( name, "Invalid name" );
        Preconditions.checkNotNull( description, "Invalid description" );

        this.name = name;
        this.description = description;
    }


    public String getName()
    {
        return name;
    }


    public String getDescription()
    {
        return description;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "name", name ).add( "description", description ).toString();
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof PackageInfo ) )
        {
            return false;
        }

        final PackageInfo that = ( PackageInfo ) o;

        if ( !description.equals( that.description ) )
        {
            return false;
        }
        if ( !name.equals( that.name ) )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }
}
