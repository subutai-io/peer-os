package org.safehaus.subutai.plugin.shark.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class SetupStrategyOverSpark extends SetupStartegyBase implements ClusterSetupStrategy
{

    public SetupStrategyOverSpark( SharkImpl manager, SharkClusterConfig config, ProductOperation po )
    {
        super( manager, config, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        checkConfig();

        SparkClusterConfig sparkConfig = checkAndGetSparkConfig();

        config.setNodes( sparkConfig.getAllNodes() );
        checkConnected();

        po.addLog( "Checking installed packages..." );

        Command checkCmd = Commands.getCheckInstalledCommand( config.getNodes() );
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
                        String.format( "Node %s already has Shark installed. Installation aborted",
                                node.getHostname() ) );
            }
        }

        po.addLog( "Updating db..." );
        try
        {
            manager.getPluginDao().saveInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster info saved to DB." );
        }
        catch ( Exception ex )
        {
            throw new ClusterSetupException(
                    "Could not save cluster info to DB! Please see logs. Installation aborted" );
        }

        po.addLog( "Installing Shark..." );
        Command installCommand = Commands.getInstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            po.addLog( "Installation succeeded." );
            setupMasterIp( sparkConfig.getMasterNode() );
        }
        else
        {
            throw new ClusterSetupException( "Installation failed: " + installCommand.getAllErrors() );
        }

        return config;
    }
}

