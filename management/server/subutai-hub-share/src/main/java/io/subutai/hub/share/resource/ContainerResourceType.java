package io.subutai.hub.share.resource;


/**
 * Container resource types
 */
public enum ContainerResourceType
{
    RAM( "ram" ), CPU( "cpu" ), OPT( "opt" ), HOME( "home" ), VAR( "var" ), ROOTFS( "rootfs" ), NET( "network" );

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
        if ( key.equals( NET.getKey() ) )
        {
            return NET;
        }

        throw new IllegalArgumentException( "No such container resource type key." );
    }
}
