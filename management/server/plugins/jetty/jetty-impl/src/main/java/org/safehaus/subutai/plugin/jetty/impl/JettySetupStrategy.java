package org.safehaus.subutai.plugin.jetty.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;

import org.apache.cxf.common.util.CollectionUtils;


public class JettySetupStrategy implements ClusterSetupStrategy
{
    final JettyImpl manager;
    final JettyConfig config;
    final TrackerOperation trackerOperation;


    public JettySetupStrategy( JettyImpl manager, JettyConfig config, TrackerOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.trackerOperation = po;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        checkConfig();

        trackerOperation.addLog( "Updating db..." );
        try
        {
            manager.getPluginDAO().saveInfo( JettyConfig.PRODUCT_KEY, config.getClusterName(), config );
            trackerOperation.addLog( "Cluster info saved to DB." );
        }
        catch ( Exception ex )
        {
            throw new ClusterSetupException(
                    "Could not save cluster info to DB! Please see logs. Installation aborted" );
        }

        for ( Agent agent : config.getNodes() )
        {
            trackerOperation.addLog( String.format( "Creating base directory '%s' for %s", config.getBaseDirectory(),
                    agent.getHostname() ) );
            Command mkdirCommand = manager.getCommands().getMakeDirectoryCommand( config.getBaseDirectory(), agent );
            manager.getCommandRunner().runCommand( mkdirCommand );

            if ( !mkdirCommand.hasSucceeded() )
            {
                trackerOperation.addLog( "Creating base directory failed" );
            }
            else
            {
                trackerOperation.addLog( "Succesfully created base directory" );
            }

            trackerOperation.addLog( String.format( "Preparing base directory '%s' for %s", config.getBaseDirectory(),
                    agent.getHostname() ) );
            Command prepareBaseCommand = manager.getCommands().getPrepareJettyBaseCommand( config, agent );
            manager.getCommandRunner().runCommand( prepareBaseCommand );

            if ( !prepareBaseCommand.hasSucceeded() )
            {
                trackerOperation.addLog( "Base directory preparation failed" );
            }
            else
            {
                trackerOperation.addLog( "Succesfully prepared base directory" );
            }

            trackerOperation.addLog( "Setting JETTY_BASE variable" );
            Command setBaseVarCommand = manager.getCommands().getSetJettyBaseVariableCommand( config, agent );
            manager.getCommandRunner().runCommand( setBaseVarCommand );
            if ( !setBaseVarCommand.hasSucceeded() )
            {
                trackerOperation.addLog( "Setting JETTY_BASE variable failed" );
            }
            else
            {
                trackerOperation.addLog(
                        String.format( "Succesfully JETTY_BASE variable set to '%s'", config.getBaseDirectory() ) );
            }

            trackerOperation.addLog( "Setting jetty.port" );
            Command setPortVarCommand = manager.getCommands().getSetJettyPortVariableCommand( config, agent );
            manager.getCommandRunner().runCommand( setPortVarCommand );

            if ( !setPortVarCommand.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Setting jetty.port to %d failed", config.getPort() ) );
            }
            else
            {
                trackerOperation
                        .addLog( String.format( "jetty.port succesfully have been set to %d", config.getPort() ) );
            }
        }

        return config;
    }


    void checkConfig() throws ClusterSetupException
    {
        String m = "Invalid configuration: ";

        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            throw new ClusterSetupException( m + "Cluster name not specified" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    m + String.format( "Cluster '%s' already exists", config.getClusterName() ) );
        }

        if ( CollectionUtils.isEmpty( config.getNodes() ) )
        {
            throw new ClusterSetupException( String.format( "No node is specified to install jetty on" ) );
        }
    }
}
