package org.safehaus.subutai.wol.impl.manager;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.wol.api.PluginInfo;
import org.safehaus.subutai.wol.api.PluginManagerException;


/**
 * Created by ebru on 12.12.2014.
 */
public class ManagerHelper
{
    private final PeerManager peerManager;
    private CommandUtil commandUtil;

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

    protected String execute( RequestBuilder requestBuilder ) throws PluginManagerException
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

    protected List<PluginInfo> parsePluginNamesAndVersions ( String result)
    {
      List<PluginInfo> plugins = new ArrayList<>();
       for( String line : parseLines( result ))
       {
           if( line.contains( Commands.PACKAGE_POSTFIX_WITHOUT_DASH ))
           {
               for( String word : parseLineIntoWords( line ))
               {
                   String pluginName = null, version = null;
                   if(word.contains( Commands.PACKAGE_POSTFIX_WITHOUT_DASH ))
                   {
                       pluginName = parsePluginNameFromWord( word );
                   }
                   else if( word.contains( "." ))
                   {
                       version = word;
                   }
                   PluginInfo plugin = new PluginInfoImpl( pluginName, pluginName + Commands.PACKAGE_POSTFIX, version );
                   plugins.add( plugin );
               }
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

}
