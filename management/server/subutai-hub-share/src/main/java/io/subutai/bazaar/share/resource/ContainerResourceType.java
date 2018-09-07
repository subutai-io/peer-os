package io.subutai.bazaar.share.resource;


/**
 * Container resource types
 */
public enum ContainerResourceType
{
    RAM( "ram" ), CPU( "cpu" ), DISK( "disk" ), NET( "network" ), CPUSET( "cpuset" );

    private String key;


    ContainerResourceType( String key )
    {
        this.key = key;
    }


    public String getKey()
    {
        return key;
    }


    public static ContainerResourceType parse( String key )
    {
        if ( key.equals( RAM.getKey() ) )
        {
            return RAM;
        }

        if ( key.equals( CPU.getKey() ) )
        {
            return CPU;
        }

        if ( key.equals( DISK.getKey() ) )
        {
            return DISK;
        }

        if ( key.equals( NET.getKey() ) )
        {
            return NET;
        }

        if ( key.equals( CPUSET.getKey() ) )
        {
            return CPUSET;
        }

        throw new IllegalArgumentException( "No such container resource type key." );
    }
}
