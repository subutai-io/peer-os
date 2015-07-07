package io.subutai.pluginmanager.impl;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.StringUtil;
import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.pluginmanager.api.PluginInfo;
import io.subutai.pluginmanager.api.PluginManagerException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;


public class ManagerHelper
{
    private final PeerManager peerManager;
    private CommandUtil commandUtil;
    private static final String INFO_JSON = String.format(
            "[{\"type\":\"plugin\", \"pluginName\":\"lucene\", \"version\":\"2.0.5\", \"rating\":\"5\" }, " +
                    "{\"type\":\"plugin\", \"pluginName\":\"hipi\", \"version\":\"2.0.4\", \"rating\":\"6\" }," +
                    " {\"type\":\"plugin\", \"pluginName\":\"hadoop\", \"version\":\"2.0.4\", \"rating\":\"6\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"presto\", \"version\":\"2.1.1\", \"rating\":\"8\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"spark\", \"version\":\"2.0.4\", \"rating\":\"1\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"shark\", \"version\":\"2.0.4\", \"rating\":\"9\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"accumulo\", \"version\":\"2.0.4\", \"rating\":\"9\" },"+
                    "{\"type\":\"plugin\", \"pluginName\":\"flume\", \"version\":\"2.0.4\", \"rating\":\"9\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"pig\", \"version\":\"2.0.4\", \"rating\":\"9\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"nutch\", \"version\":\"2.0.4\", \"rating\":\"9\" },"+
                    "{\"type\":\"plugin\", \"pluginName\":\"elasticsearch\", \"version\":\"2.0.4\", \"rating\":\"9\" },"+
                    "{\"type\":\"plugin\", \"pluginName\":\"zookeeper\", \"version\":\"2.0.4\", \"rating\":\"9\" },"+
                    "{\"type\":\"plugin\", \"pluginName\":\"sqoop\", \"version\":\"2.0.4\", \"rating\":\"9\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"storm\", \"version\":\"2.0.4\", \"rating\":\"9\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"hbase\", \"version\":\"2.0.4\", \"rating\":\"9\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"hive\", \"version\":\"2.0.4\", \"rating\":\"9\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"lucene\", \"version\":\"2.0.4\", \"rating\":\"9\" }," +
                    "{\"type\":\"plugin\", \"pluginName\":\"mahout\", \"version\":\"2.0.4\", \"rating\":\"9\" }]" );


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
            if( result.hasSucceeded() )
            {
                return result.getStdOut();
            }
        }
        catch ( CommandException e )
        {
            throw new PluginManagerException( e );
        }
        return null;
    }


    private List<String> parseLines( String result )
    {
        String eol = System.getProperty( "line.separator" );

        return StringUtil.splitString( result, eol );
    }


    protected Set<String> parsePluginNames( String result )
    {
        Set<String> pluginNames = Sets.newHashSet();
        for( String line : parseLines( result ))
        {
            List<String> words = parseLineIntoWords( line );
            pluginNames.add( parsePluginNameFromWord( words.get( 0 ) ) );
        }
        return pluginNames;
    }

    protected Set<String> parseAvailablePluginsNames( String result)
    {
        Preconditions.checkNotNull( result );
        Set<String> pluginNames = Sets.newHashSet();
        for( String line : parseLines( result ))
        {
            pluginNames.add( line );
        }
        return pluginNames;
    }

    protected Set<PluginInfo> parsePluginNamesAndVersions( String result )
    {

                Set<PluginInfo> plugins = Sets.newHashSet();
                for ( String line : parseLines( result ) )
                {
                    if ( line.contains( Commands.PACKAGE_POSTFIX_WITHOUT_DASH ) && !( line.contains( "repo" )) )
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
        {}.getType() );
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


    protected String findVersion( Set<PluginInfo> availablePlugins, String pluginName )
    {
        String version = null;
        for ( PluginInfo plugin : availablePlugins )
        {
            if ( pluginName.equals( plugin.getPluginName() ) )
            {
                version = plugin.getVersion();
            }
        }
        return version;
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
