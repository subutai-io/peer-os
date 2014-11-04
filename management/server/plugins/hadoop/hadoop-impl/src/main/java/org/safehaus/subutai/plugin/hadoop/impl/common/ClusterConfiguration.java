package org.safehaus.subutai.plugin.hadoop.impl.common;


import java.util.logging.Logger;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class ClusterConfiguration
{

    private static final Logger LOG = Logger.getLogger( ClusterConfiguration.class.getName() );
    private TrackerOperation po;
    private HadoopImpl hadoopManager;



    public ClusterConfiguration( final TrackerOperation operation, final HadoopImpl cassandraManager)
    {
        this.po = operation;
        this.hadoopManager = cassandraManager;
    }


    public void configureCluster( HadoopClusterConfig config, Environment environment )
            throws ClusterConfigurationException, CommandException
    {
        Commands commands = new Commands( config );
        po.addLog( String.format( "Configuring cluster: %s", config.getClusterName() ) );

        // Clear configuration files
        for ( ContainerHost containerHost : environment.getContainers() ){
            containerHost.execute( new RequestBuilder( commands.getClearMastersCommand() ) );
            containerHost.execute( new RequestBuilder( commands.getClearSlavesCommand() ) );
        }

        // Configure NameNode
        for ( ContainerHost containerHost : environment.getContainers() ){
            containerHost.execute( new RequestBuilder( commands.getSetMastersCommand() ) );
        }

        // Configure JobTracker
        config.getJobTracker().execute( new RequestBuilder( commands.getConfigureJobTrackerCommand() ) );


        // Configure Secondary NameNode
        config.getNameNode().execute( new RequestBuilder( commands.getConfigureSecondaryNameNodeCommand() ) );


        // Configure DataNodes
        for ( ContainerHost containerHost : config.getDataNodes() ){
            config.getNameNode().execute(
                    new RequestBuilder( commands.getConfigureDataNodesCommand( containerHost.getHostname() ) ) );
        }

        // Configure TaskTrackers
        for ( ContainerHost containerHost : config.getTaskTrackers() ){
            config.getJobTracker().execute(
                    new RequestBuilder( commands.getConfigureTaskTrackersCcommand( containerHost.getHostname() ) ) );
        }

        // Format NameNode
        config.getNameNode().execute( new RequestBuilder( commands.getFormatNameNodeCommand() ) );


        // Start Hadoop cluster
        config.getNameNode().execute( new RequestBuilder( commands.getStartNameNodeCommand() ) );
        config.getJobTracker().execute( new RequestBuilder( commands.getStartJobTrackerCommand() ) );


        po.addLog( "Configuration is finished !" );

        config.setEnvironmentId( environment.getId() );
        hadoopManager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLogDone( "Hadoop cluster data saved into database" );
    }
}
