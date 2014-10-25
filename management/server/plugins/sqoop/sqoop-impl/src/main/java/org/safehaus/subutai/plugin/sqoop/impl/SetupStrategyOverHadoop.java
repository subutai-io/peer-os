package org.safehaus.subutai.plugin.sqoop.impl;


import java.util.HashSet;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;


class SetupStrategyOverHadoop extends SqoopSetupStrategy
{

    public SetupStrategyOverHadoop( SqoopImpl manager, SqoopConfig config, TrackerOperation po )
    {
        super( manager, config, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        checkConfig();

        //check if node agents are connected
        for ( Agent a : config.getNodes() )
        {
            if ( manager.agentManager.getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( String.format( "Node %s is not connected", a.getHostname() ) );
            }
        }

        HadoopClusterConfig hc = manager.hadoopManager.getCluster( config.getHadoopClusterName() );
        if ( hc == null )
        {
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }

        if ( !hc.getAllNodes().containsAll( config.getNodes() ) )
        {
            throw new ClusterSetupException(
                    "Not all nodes belong to Hadoop cluster " + config.getHadoopClusterName() );
        }
        config.setHadoopNodes( new HashSet<>( hc.getAllNodes() ) );

        // check if already installed
        String s = CommandFactory.build( CommandType.LIST, null );
        Command cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ), config.getNodes() );
        manager.getCommandRunner().runCommand( cmd );

        if ( !cmd.hasCompleted() )
        {
            String m = "Failed to check installed packages";
            po.addLog( m );
            throw new ClusterSetupException( m + ": " + cmd.getAllErrors() );
        }

        String hadoop_pack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
        for ( Agent a : config.getNodes() )
        {
            AgentResult res = cmd.getResults().get( a.getUuid() );

            if ( res.getStdOut().contains( CommandFactory.PACKAGE_NAME ) )
            {
                throw new ClusterSetupException(
                        String.format( "Node %s already has Sqoop installed", a.getHostname() ) );
            }
            else if ( !res.getStdOut().contains( hadoop_pack ) )
            {
                throw new ClusterSetupException(
                        String.format( "Node %s has no Hadoop installation.", a.getHostname() ) );
            }
        }

        // installation
        s = CommandFactory.build( CommandType.INSTALL, null );
        cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ).withTimeout( 180 ), config.getNodes() );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Installation succeeded" );
            po.addLog( "Saving to db..." );
            manager.getPluginDao().saveInfo( SqoopConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster info successfully saved" );
        }
        else
        {
            throw new ClusterSetupException( "Installation failed: " + cmd.getAllErrors() );
        }

        return config;
    }
}
