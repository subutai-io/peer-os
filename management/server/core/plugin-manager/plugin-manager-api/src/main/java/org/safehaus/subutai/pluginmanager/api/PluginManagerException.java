package org.safehaus.subutai.pluginmanager.api;


public class PluginManagerException extends Exception
{
    public PluginManagerException( final Throwable cause )
    {
        super( cause );
    }


    public PluginManagerException( final String message )
    {
        super( message );
    }
}
