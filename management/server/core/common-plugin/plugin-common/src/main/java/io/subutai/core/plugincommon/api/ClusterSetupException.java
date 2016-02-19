package io.subutai.core.plugincommon.api;


public class ClusterSetupException extends Exception
{

    public ClusterSetupException( final String message )
    {
        super( message );
    }


    public ClusterSetupException( final Throwable cause )
    {
        super( cause );
    }
}
