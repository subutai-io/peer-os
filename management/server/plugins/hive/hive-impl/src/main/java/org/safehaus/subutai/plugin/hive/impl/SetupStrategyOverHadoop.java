package org.safehaus.subutai.plugin.hive.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;


public class SetupStrategyOverHadoop extends HiveSetupStrategy
{

    public SetupStrategyOverHadoop( Environment environment, HiveImpl manager, HiveConfig config,
                                    HadoopClusterConfig hadoopClusterConfig, TrackerOperation po )
    {
        super( environment, manager, config, hadoopClusterConfig, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        checkConfig();

        //check if nodes are connected
        ContainerHost server = environment.getContainerHostByUUID( config.getServer() );
        if ( !server.isConnected() )
        {
            throw new ClusterSetupException( "Server node is not connected " );
        }
        for ( UUID uuid : config.getClients() )
        {
            ContainerHost host = environment.getContainerHostByUUID( uuid );
            if ( !host.isConnected() )
            {
                throw new ClusterSetupException( String.format( "Node %s is not connected", host.getHostname() ) );
            }
        }


        if ( hadoopClusterConfig == null )
        {
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }


        Set<UUID> allNodes = new HashSet<>( config.getClients() );
        allNodes.add( config.getServer() );

        if ( !hadoopClusterConfig.getAllNodes().containsAll( allNodes ) )
        {
            throw new ClusterSetupException(
                    "Not all nodes belong to Hadoop cluster " + config.getHadoopClusterName() );
        }
        config.setHadoopNodes( new HashSet<>( hadoopClusterConfig.getAllNodes() ) );


        // installation of server
        trackerOperation.addLog( "Installing server..." );
        for ( Product p : new Product[] { Product.HIVE, Product.DERBY } )
        {
            try
            {
                if ( !checkIfProductIsInstalled( server, HiveConfig.PRODUCT_KEY.toLowerCase() ) )
                {
                    server.execute( new RequestBuilder(
                            Commands.installCommand + Common.PACKAGE_PREFIX + Product.HIVE.toString().toLowerCase() ) );
                }
                if ( !checkIfProductIsInstalled( server, Product.DERBY.toString().toLowerCase() ) )
                {
                    server.execute( new RequestBuilder(
                            Commands.installCommand + Common.PACKAGE_PREFIX + Product.DERBY.toString()
                                                                                           .toLowerCase() ) );
                }
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException( String.format( "Failed to install %s on server node", p.toString() ) );
            }
        }
        trackerOperation.addLog( "Server installation completed" );


        // installation of clients
        trackerOperation.addLog( "Installing clients..." );
        for ( UUID uuid : config.getClients() )
        {
            ContainerHost client = environment.getContainerHostByUUID( uuid );
            try
            {
                if ( !checkIfProductIsInstalled( client, HiveConfig.PRODUCT_KEY.toLowerCase() ) )
                {
                    client.execute( new RequestBuilder(
                            Commands.installCommand + Common.PACKAGE_PREFIX + Product.HIVE.toString().toLowerCase() ) );
                    trackerOperation.addLog( Product.HIVE.name() + " is installed on " + client.getHostname() );
                }
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException(
                        String.format( "Failed to install %s on server node", Product.HIVE.toString() ) );
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


    private boolean checkIfProductIsInstalled( ContainerHost containerHost, String productName )
    {
        boolean isHiveInstalled = false;
        try
        {
            CommandResult result = containerHost.execute( new RequestBuilder( Commands.checkIfInstalled ) );
            if ( result.getStdOut().contains( productName ) )
            {
                isHiveInstalled = true;
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return isHiveInstalled;
    }
}
