package org.safehaus.subutai.plugin.sqoop.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;


class SetupStrategyOverHadoop extends SqoopSetupStrategy
{

    public SetupStrategyOverHadoop( SqoopImpl manager, SqoopConfig config, Environment env, TrackerOperation po )
    {
        super( manager, config, env, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        checkConfig();

        //check if nodes are connected
        Set<ContainerHost> nodes = environment.getContainerHostsByIds( config.getNodes() );
        if ( nodes.size() < config.getNodes().size() )
        {
            throw new ClusterSetupException( "Fewer nodes found in the encironment than expected" );
        }
        for ( ContainerHost node : nodes )
        {
            if ( !node.isConnected() )
            {
                throw new ClusterSetupException( String.format( "Node %s is not connected", node.getHostname() ) );
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
        String s = CommandFactory.build( NodeOperationType.STATUS, null );
        String hadoop_pack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME.toLowerCase();
        Iterator<ContainerHost> it = nodes.iterator();
        while ( it.hasNext() )
        {
            ContainerHost node = it.next();
            try
            {
                CommandResult res = node.execute( new RequestBuilder( s ) );
                if ( res.hasSucceeded() )
                {
                    if ( res.getStdOut().contains( CommandFactory.PACKAGE_NAME ) )
                    {
                        to.addLog( String.format( "Node %s has already Sqoop installed.", node.getHostname() ) );
                        it.remove();
                    }
                    else if ( res.getStdOut().contains( hadoop_pack ) )
                    {
                        throw new ClusterSetupException( "Hadoop not installed on node " + node.getHostname() );
                    }
                }
                else
                {
                    throw new ClusterSetupException( "Failed to check installed packges on " + node.getHostname() );
                }
            }
            catch ( CommandException ex )
            {
                throw new ClusterSetupException( ex );
            }
        }
        if ( nodes.isEmpty() )
        {
            throw new ClusterSetupException( "No nodes to install Sqoop" );
        }

        // installation
        s = CommandFactory.build( NodeOperationType.INSTALL, null );
        it = nodes.iterator();
        while ( it.hasNext() )
        {
            ContainerHost node = it.next();
            try
            {
                CommandResult res = node.execute( new RequestBuilder( s ) );
                if ( res.hasSucceeded() )
                {
                    to.addLog( "Sqoop installed on " + node.getHostname() );
                }
                else
                {
                    throw new ClusterSetupException( "Failed to install Sqoop on " + node.getHostname() );
                }
            }
            catch ( CommandException ex )
            {
                throw new ClusterSetupException( ex );
            }
        }

        to.addLog( "Saving to db..." );
        boolean saved = manager.getPluginDao().saveInfo( SqoopConfig.PRODUCT_KEY, config.getClusterName(), config );
        if ( saved )
        {
            to.addLog( "Installation info successfully saved" );
            configure();
        }
        else
        {
            throw new ClusterSetupException( "Failed to save installation info" );
        }

        return config;
    }
}

