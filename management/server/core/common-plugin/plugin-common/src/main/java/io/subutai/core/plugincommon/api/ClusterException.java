package io.subutai.core.plugincommon.api;


public class ClusterException extends Exception
{

    public ClusterException( final String message )
    {
        super( message );
    }


    public ClusterException( final Throwable cause )
    {
        super( cause );
    }


    public ClusterException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
