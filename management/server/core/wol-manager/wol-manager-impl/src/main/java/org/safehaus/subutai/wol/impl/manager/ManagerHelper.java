package org.safehaus.subutai.wol.impl.manager;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.wol.api.PluginInfo;
import org.safehaus.subutai.wol.api.PluginManagerException;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;


/**
 * Created by ebru on 12.12.2014.
 */
public class ManagerHelper
{
    private final PeerManager peerManager;
    private CommandUtil commandUtil;

    private static final String INFO_JSON = String.format(
            "[{\"type\":\"plugin\", \"pluginName\":\"lucene\", \"version\":\"2.0.4\", \"rating\":\"5\" }, " +
                    "{\"type\":\"plugin\", \"pluginName\":\"hipi\", \"version\":\"2.0.4\", \"rating\":\"6\" } ]" );


    public ManagerHelper( PeerManager peerManager )
    {
        this.peerManager = peerManager;
        this.commandUtil = new CommandUtil();
    }


    protected ManagementHost getManagementHost() throws PluginManagerException
    {
        try
        {
            return peerManager.getLocalPeer().getManagementHost();
        }
        catch ( HostNotFoundException e )
        {
            throw new PluginManagerException( e );
        }
    }


    public String execute( RequestBuilder requestBuilder ) throws PluginManagerException
    {
        try
        {
            CommandResult result = commandUtil.execute( requestBuilder, getManagementHost() );
            return result.getStdOut();
        }
        catch ( CommandException e )
        {
            throw new PluginManagerException( e );
        }
    }


    private List<String> parseLines( String result )
    {
        String eol = System.getProperty( "line.separator" );
        List<String> lines = StringUtil.splitString( result, eol );

        return lines;
    }


    protected Set<PluginInfo> parsePluginNamesAndVersions( String result )
    {
        Set<PluginInfo> plugins = Sets.newHashSet();
        for ( String line : parseLines( result ) )
        {
            if ( line.contains( Commands.PACKAGE_POSTFIX_WITHOUT_DASH ) )
            {
                PluginInfo plugin = new PluginInfoImpl();
                for ( String word : parseLineIntoWords( line ) )
                {
                    if ( word.contains( Commands.PACKAGE_POSTFIX_WITHOUT_DASH ) )
                    {
                        String pluginName = parsePluginNameFromWord( word );
                        plugin.setPluginName( pluginName );
                        plugin.setType( "plugin" );
                        plugin.setRating( findRating( parseJson(), pluginName ) );
                    }
                    else if ( word.contains( "." ) && !word.contains( "application" ) )
                    {
                        String version = word;
                        plugin.setVersion( version );
                    }
                }
                plugins.add( plugin );
            }
        }
        return plugins;
    }


    private List<String> parseLineIntoWords( String line )
    {
        List<String> words = StringUtil.splitString( line, " \t" );
        return words;
    }


    private String parsePluginNameFromWord( String word )
    {
        List<String> pieces = StringUtil.splitString( word, "-" );
        String pluginName = pieces.get( 0 );
        return pluginName;
    }


    protected Set<PluginInfo> parseJson()
    {
        Set<PluginInfo> plugins = JsonUtil.fromJson( INFO_JSON, new TypeToken<Set<PluginInfoImpl>>()
        {
        }.getType() );
        return plugins;
    }


    protected String findRating( Set<PluginInfo> availablePlugins, String pluginName )
    {
        String rating = null;
        for ( PluginInfo plugin : availablePlugins )
        {
            if ( pluginName.equals( plugin.getPluginName() ) )
            {
                rating = plugin.getRating();
            }
        }
        return rating;
    }


    protected Set<PluginInfo> getDifferenceBetweenPlugins( Set<PluginInfo> installedPlugins,
                                                           Set<PluginInfo> availablePlugins )
    {
        for ( PluginInfo plugin : availablePlugins )
        {
            if ( installedPlugins.contains( plugin ) )
            {
                availablePlugins.remove( plugin );
            }
        }
        return availablePlugins;
    }
}
