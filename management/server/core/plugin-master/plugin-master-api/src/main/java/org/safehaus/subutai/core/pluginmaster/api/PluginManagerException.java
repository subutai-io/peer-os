package org.safehaus.subutai.core.pluginmaster.api;


/**
 * Created by ebru on 08.12.2014.
 */
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
