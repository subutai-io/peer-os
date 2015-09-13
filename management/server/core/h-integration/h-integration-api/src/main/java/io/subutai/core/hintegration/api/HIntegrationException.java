package io.subutai.core.hintegration.api;


/**
 * Created by tzhamakeev on 9/7/15.
 */
public class HIntegrationException extends Exception
{
    public HIntegrationException( final String s, final Throwable cause )
    {
        super( s, cause );
    }


    public HIntegrationException( final String s )
    {
        super( s );
    }
}
