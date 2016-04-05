package io.subutai.common.resource;


/**
 * Container resource types
 */
public enum ContainerResourceType
{
    RAM( "ram" ), CPU( "cpu" ), OPT( "opt" ),
    HOME( "home" ), VAR( "var" ),
    ROOTFS( "rootfs" );

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

        if ( key.equals( OPT.getKey() ) )
        {
            return OPT;
        }

        if ( key.equals( HOME.getKey() ) )
        {
            return HOME;
        }

        if ( key.equals( VAR.getKey() ) )
        {
            return VAR;
        }

        if ( key.equals( ROOTFS.getKey() ) )
        {
            return ROOTFS;
        }

        throw new IllegalArgumentException( "No such key for container resoutce type enum." );
    }
}
