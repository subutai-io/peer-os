package io.subutai.core.pluginmanager.cli;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import io.subutai.core.pluginmanager.api.PluginInfo;
import io.subutai.core.pluginmanager.api.PluginManager;


/**
 * Created by talas on 12/7/15.
 */
@Command( scope = "plugin", name = "list", description = "Lists available plugins" )
public class GetAvailablePluginList extends OsgiCommandSupport
{
    private static final Logger logger = LoggerFactory.getLogger( GetAvailablePluginList.class );

    private PluginManager pluginManager;


    public GetAvailablePluginList( final PluginManager pluginManager )
    {
        this.pluginManager = pluginManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<PluginInfo> pluginInfoList = pluginManager.getAvailablePlugins();
        for ( final PluginInfo pluginInfo : pluginInfoList )
        {
            String out = String.format( "%s %s %s %s \n###", pluginInfo.getPluginName(), pluginInfo.getRating(),
                    pluginInfo.getType(), pluginInfo.getVersion() );
            System.out.println( out );
        }
        return null;
    }
}
