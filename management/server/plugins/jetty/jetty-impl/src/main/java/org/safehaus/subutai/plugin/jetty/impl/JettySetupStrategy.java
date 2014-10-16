package org.safehaus.subutai.plugin.jetty.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;

import org.apache.cxf.common.util.CollectionUtils;


public class JettySetupStrategy implements ClusterSetupStrategy
{
    final JettyImpl manager;
    final JettyConfig config;
    final ProductOperation productOperation;


    public JettySetupStrategy( JettyImpl manager, JettyConfig config, ProductOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.productOperation = po;
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


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        checkConfig();


        Command checkCmd = manager.getCommands().getCheckInstalledCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( checkCmd );
        if ( !checkCmd.hasCompleted() )
        {
            throw new ClusterSetupException( "Failed to check installed packages. Installation aborted" );
        }
        for ( Agent node : config.getNodes() )
        {
            AgentResult result = checkCmd.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( Commands.PACKAGE_NAME ) )
            {
                throw new ClusterSetupException(
                        String.format( "Node %s already has Jetty installed. Installation aborted",
                                node.getHostname() ) );
            }
        }

        productOperation.addLog( "Updating db..." );
        try
        {
            manager.getPluginDAO().saveInfo( JettyConfig.PRODUCT_KEY, config.getClusterName(), config );
            productOperation.addLog( "Cluster info saved to DB." );
        }
        catch ( Exception ex )
        {
            throw new ClusterSetupException(
                    "Could not save cluster info to DB! Please see logs. Installation aborted" );
        }

        productOperation.addLog( "Installing Jetty..." );
        Command installCommand = manager.getCommands().getInstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            productOperation.addLog( "Installation succeeded." );
        }
        else
        {
            throw new ClusterSetupException( "Installation failed: " + installCommand.getAllErrors() );
        }

        return config;
    }
}
