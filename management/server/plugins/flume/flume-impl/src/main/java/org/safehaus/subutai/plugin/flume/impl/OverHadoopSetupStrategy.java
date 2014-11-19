package org.safehaus.subutai.plugin.flume.impl;


import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


class OverHadoopSetupStrategy extends FlumeSetupStrategy
{
    private static final Logger LOG = LoggerFactory.getLogger(OverHadoopSetupStrategy.class.getName());
    private Environment environment;

    public OverHadoopSetupStrategy( FlumeImpl manager, FlumeConfig config, TrackerOperation po, Environment environment )
    {
        super( manager, config, po );
        this.environment = environment;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        checkConfig();
        check();
        configure();
        return config;
    }

    private void configure() throws ClusterSetupException {
        po.addLog( "Updating db..." );
        //save to db
        config.setEnvironmentId( environment.getId() );
        manager.getPluginDao().saveInfo( FlumeConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster info saved to DB\nInstalling Flume..." );
        //install pig,
        String s = Commands.make( CommandType.INSTALL );
        //RequestBuilder installCommand = new RequestBuilder( s ).withTimeout( 1800 );
        for ( ContainerHost node : environment.getHostsByIds( config.getNodes() ) )
        {
            try
            {
                CommandResult result = node.execute(new RequestBuilder( s ).withTimeout( 600 ));
                processResult( node, result );

            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException(
                        String.format( "Error while installing Flume on container %s; %s", node.getHostname(),
                                e.getMessage() ) );
            }
        }

        po.addLog( "Configuring cluster..." );
    }

    private void check() throws ClusterSetupException {
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

        po.addLog("Checking prerequisites...");

        RequestBuilder checkInstalledCommand = new RequestBuilder( Commands.make( CommandType.STATUS ) );
        for( UUID uuid : config.getNodes())
        {
            ContainerHost node = environment.getContainerHostByUUID( uuid );
            try
            {
                CommandResult result = node.execute( checkInstalledCommand );
                if ( result.getStdOut().contains( Commands.PACKAGE_NAME ) )
                {
                    po.addLog(
                            String.format( "Node %s already has Flume installed. Omitting this node from installation",
                                    node.getHostname() ) );
                    config.getNodes().remove( node.getId() );
                }
                else if ( !result.getStdOut()
                        .contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME.toLowerCase() ) )
                {
                    po.addLog(
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
    public void processResult( ContainerHost host, CommandResult result ) throws ClusterSetupException
    {

        if ( !result.hasSucceeded() )
        {
            throw new ClusterSetupException( String.format( "Error on container %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
    }
}
