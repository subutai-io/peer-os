package io.subutai.core.pluginmanager.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import io.subutai.core.pluginmanager.api.PluginInfo;
import io.subutai.core.pluginmanager.api.PluginManager;


/**
 * Created by talas on 12/7/15.
 */
@Command( scope = "plugin", name = "info", description = "Get plugin info" )
public class GetPluginInfo extends OsgiCommandSupport
{
    private static final Logger logger = LoggerFactory.getLogger( GetPluginInfo.class );

    private PluginManager pluginManager;

    @Argument( index = 0, name = "plugin name", required = true, multiValued = false,
            description = "name of a plugin" )
    private String pluginName;

    @Argument( index = 1, name = "plugin version", required = true, multiValued = false,
            description = "plugin version" )
    private String pluginVersion;


    public GetPluginInfo( final PluginManager pluginManager )
    {
        this.pluginManager = pluginManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        PluginInfo pluginInfo = pluginManager.getPluginInfo( pluginName, pluginVersion );
        if ( pluginInfo != null )
        {
            System.out.print( pluginInfo.getPluginName() + " " );
            System.out.print( pluginInfo.getRating() + " " );
            System.out.print( pluginInfo.getType() + " " );
            System.out.println( pluginInfo.getVersion() );
        }
        else
        {
            System.out.println( "Not found." );
        }
        return null;
    }
}
