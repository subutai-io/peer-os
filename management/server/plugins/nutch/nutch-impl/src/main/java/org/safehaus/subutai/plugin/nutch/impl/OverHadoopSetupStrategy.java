package org.safehaus.subutai.plugin.nutch.impl;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.nutch.api.NutchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


class OverHadoopSetupStrategy extends NutchSetupStrategy
{

    private static final Logger LOG = LoggerFactory.getLogger( OverHadoopSetupStrategy.class.getName() );
    private Environment environment;

    public OverHadoopSetupStrategy( NutchImpl manager, NutchConfig config, TrackerOperation po, Environment environment )
    {
        super( manager, config, po );
        this.environment = environment;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        check();
        configure();
        return config;

    }


    private void check() throws ClusterSetupException
    {
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
        //check nodes are connected
        //        Set<ContainerHost> nodes = environment.getContainerHostsByIds( config.getNodes() );
        //        for ( ContainerHost host : nodes )
        //        {
        //            if ( !host.isConnected() )
        //            {
        //                throw new ClusterSetupException(
        //                        String.format( "Container %s is not connected", host.getHostname() ) );
        //            }
        //        }
        //check hadoopcluster
        HadoopClusterConfig hc = manager.getHadoopManager().getCluster( config.getHadoopClusterName() );
        if ( hc == null )
        {
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }
        if ( !hc.getAllNodes().containsAll( config.getNodes() ) )
        {
            throw new ClusterSetupException(
                    "Not all nodes belong to Hadoop cluster " + config.getHadoopClusterName() );
        }

        trackerOperation.addLog( "Checking prerequisites..." );

        RequestBuilder checkInstalledCommand = new RequestBuilder( Constants.CHECK );
        for( UUID uuid : config.getNodes())
        {
            ContainerHost node = environment.getContainerHostById( uuid );
            try
            {
                CommandResult result = node.execute( checkInstalledCommand );
                if ( result.getStdOut().contains( Constants.PACKAGE_NAME ) )
                {
                    trackerOperation.addLog(
                            String.format( "Node %s already has Nutch installed. Omitting this node from installation",
                                    node.getHostname() ) );
                    config.getNodes().remove( node.getId() );
                }
                else if ( !result.getStdOut()
                                 .contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME.toLowerCase() ) )
                {
                    trackerOperation.addLog(
                            String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                                    node.getHostname() ) );
                    config.getNodes().remove( node.getId() );
                }
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException( "Failed to check presence of installed subutai packages" );
            }
        }
        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

    }
    private void configure() throws ClusterSetupException
    {
        trackerOperation.addLog( "Updating db..." );
        //save to db
        config.setEnvironmentId( environment.getId() );
        manager.getPluginDao().saveInfo( NutchConfig.PRODUCT_KEY, config.getClusterName(), config );
        trackerOperation.addLog( "Cluster info saved to DB\nInstalling Pig..." );
        //install nutch,
        //RequestBuilder installCommand = new RequestBuilder( Commands.INSTALL );
        for ( ContainerHost node : environment.getContainerHostsByIds( config.getNodes() ) )
        {
            try
            {
                node.execute(new RequestBuilder( Constants.INSTALL ).withTimeout( 600 ));
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException(
                        String.format( "Error while installing Pig on container %s; %s", node.getHostname(),
                                e.getMessage() ) );
            }
        }

        trackerOperation.addLog( "Configuring cluster..." );
    }
    public void processResult( ContainerHost host, CommandResult result ) throws ClusterSetupException
    {

        if ( !result.hasSucceeded() )
        {
            throw new ClusterSetupException( String.format( "Error on container %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
    }
}
