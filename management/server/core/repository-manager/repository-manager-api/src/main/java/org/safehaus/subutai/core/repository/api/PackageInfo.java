package org.safehaus.subutai.core.repository.api;


/**
 * Contains package info
 */
public class PackageInfo
{
    private String status;
    private String name;
    private String description;


    public PackageInfo( final String status, final String name, final String description )
    {
        this.status = status;
        this.name = name;
        this.description = description;
    }


    public String getStatus()
    {
        return status;
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
        return "PackageInfo{" +
                "status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
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

        if ( description != null ? !description.equals( that.description ) : that.description != null )
        {
            return false;
        }
        if ( name != null ? !name.equals( that.name ) : that.name != null )
        {
            return false;
        }
        if ( status != null ? !status.equals( that.status ) : that.status != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + ( name != null ? name.hashCode() : 0 );
        result = 31 * result + ( description != null ? description.hashCode() : 0 );
        return result;
    }
}
