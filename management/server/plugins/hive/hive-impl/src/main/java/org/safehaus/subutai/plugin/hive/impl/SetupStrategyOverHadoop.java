package org.safehaus.subutai.plugin.hive.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;


class SetupStrategyOverHadoop extends HiveSetupStrategy
{

    public SetupStrategyOverHadoop( Environment environment, HiveImpl manager, HiveConfig config, TrackerOperation po )
    {
        super( environment, manager, config, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        checkConfig();

        //check if nodes are connected
        ContainerHost server = environment.getContainerHostByUUID( config.getEnvironmentId() );
        if ( ! server.isConnected() ){
            throw new ClusterSetupException( "Server node is not connected " );
        }
        for ( UUID uuid : config.getClients() ){
            ContainerHost host = environment.getContainerHostByUUID( uuid );
            if ( ! host.isConnected() ){
                throw new ClusterSetupException( String.format( "Node %s is not connected", host.getHostname() ) );
            }
        }

        HadoopClusterConfig hadoopConfig = ( HadoopClusterConfig ) hiveManager.getHadoopManager();
        if ( hadoopConfig == null ){
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }


        Set<UUID> allNodes = new HashSet<>( config.getClients() );
        allNodes.add( config.getServer() );

        if ( !hadoopConfig.getAllNodes().containsAll( allNodes ) )
        {
            throw new ClusterSetupException(
                    "Not all nodes belong to Hadoop cluster " + config.getHadoopClusterName() );
        }
        config.setHadoopNodes( new HashSet<>( hadoopConfig.getAllNodes() ) );

        // check if already installed
        for ( UUID uuid : config.getAllNodes() ){
            ContainerHost host = environment.getContainerHostByUUID( uuid );
            CommandResult result = null;
            try
            {
                result = host.execute( new RequestBuilder( Commands.checkIfInstalled ) );
                if ( result.getStdOut().contains( Product.HIVE.getPackageName() ) )
                {
                    throw new ClusterSetupException(
                            String.format( "Node %s already has Hive installed", host.getHostname() ) );
                }
                else if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) )
                {
                    throw new ClusterSetupException(
                            String.format( "Node %s has no Hadoop installation.", host.getHostname() ) );
                }
                else if ( host.getAgent().getUuid().equals( config.getServer() ) )
                {
                    if ( result.getStdOut().contains( Product.DERBY.getPackageName() ) )
                    {
                        throw new ClusterSetupException( "Server node already has Derby installed" );
                    }
                }
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }
        }

        // installation of server
        trackerOperation.addLog( "Installing server..." );
        for ( Product p : new Product[] { Product.HIVE, Product.DERBY } )
        {
            try
            {
                server.execute( new RequestBuilder( Commands.installCommand + Product.HIVE ) );
                server.execute( new RequestBuilder( Commands.installCommand + Product.DERBY ) );
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException( String.format( "Failed to install %s on server node", p.toString() ) );
            }

        }
        trackerOperation.addLog( "Server installation completed" );


        trackerOperation.addLog( "Installing clients..." );
        for ( UUID uuid : config.getClients() ){
            ContainerHost client = environment.getContainerHostByUUID( uuid );
            try
            {
                client.execute( new RequestBuilder( Commands.installCommand + Product.HIVE ) );
                trackerOperation.addLog( Product.HIVE.name() + " is installed on " + client.getHostname() );
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException( String.format( "Failed to install %s on server node", Product.HIVE.toString() ) );
            }
        }

        try
        {
            new ClusterConfiguration( hiveManager, trackerOperation ).configureCluster( config, environment );
        }
        catch ( ClusterConfigurationException e )
        {
            throw new ClusterSetupException( e.getMessage() );
        }


        return config;
    }
}
