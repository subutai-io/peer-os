package io.subutai.core.hubmanager.api;


public class HubPluginException extends Exception
{
    public HubPluginException( final String s, final Throwable cause )
    {
        super( s, cause );
    }


    public HubPluginException( final String s )
    {
        super( s );
    }
}
