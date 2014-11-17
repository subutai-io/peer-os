package org.safehaus.subutai.plugin.hipi.impl;


import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hipi.api.HipiConfig;

import com.google.common.base.Strings;


class OverHadoopSetupStrategy extends HipiSetupStrategy
{

    public OverHadoopSetupStrategy( HipiImpl manager, HipiConfig config, Environment environment, TrackerOperation po )
    {
        super( manager, config, environment, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        checkConfig();

        Set<ContainerHost> nodes = environment.getHostsByIds( config.getNodes() );

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

        if ( Strings.isNullOrEmpty( config.getHadoopClusterName() ) || CollectionUtil
                .isCollectionEmpty( config.getNodes() ) )
        {
            throw new ClusterSetupException( "Malformed configuration\nInstallation aborted" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ) );
        }

        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        trackerOperation.addLog( "Checking prerequisites..." );

        // Check installed packages
        trackerOperation.addLog( "Installing Hipi..." );

        String statusCommand = CommandFactory.build( NodeOperationType.CHECK_INSTALLATION );
        for ( Iterator<ContainerHost> it = nodes.iterator(); it.hasNext(); )
        {
            ContainerHost node = it.next();
            try
            {
                CommandResult result = node.execute( new RequestBuilder( statusCommand ) );
                if ( result.hasSucceeded() && result.getStdOut().contains( CommandFactory.PACKAGE_NAME ) )
                {
                    trackerOperation
                            .addLog( String.format( "Node %s has already Hipi installed.", node.getHostname() ) );
                    it.remove();
                }
                else
                {
                    throw new ClusterSetupException( "Failed to check installed packages on " + node.getHostname() );
                }
            }
            catch ( CommandException ex )
            {
                throw new ClusterSetupException( ex );
            }
        }

        if ( nodes.isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }


        String installCommand = CommandFactory.build( NodeOperationType.INSTALL );
        for ( Iterator<ContainerHost> it = nodes.iterator(); it.hasNext(); )
        {
            ContainerHost node = it.next();
            try
            {
                CommandResult result = node.execute( new RequestBuilder( installCommand ) );

                if ( result.hasSucceeded() )
                {
                    trackerOperation.addLog( "Hipi installed on " + node.getHostname() );
                }
                else
                {
                    throw new ClusterSetupException( "Failed to install Hipi on " + node.getHostname() );
                }
            }
            catch ( CommandException ex )
            {
                throw new ClusterSetupException( ex );
            }
        }

        trackerOperation.addLog( "Saving to db..." );
        boolean saved = manager.getPluginDao().saveInfo( HipiConfig.PRODUCT_KEY, config.getClusterName(), config );

        if ( saved )
        {
            trackerOperation.addLog( "Installation info successfully saved" );
        }
        else
        {
            throw new ClusterSetupException( "Failed to save installation info" );
        }

        return config;
    }
}
