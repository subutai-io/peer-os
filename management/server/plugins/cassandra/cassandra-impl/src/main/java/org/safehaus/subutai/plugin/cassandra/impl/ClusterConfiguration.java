package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;


public class ClusterConfiguration
{

    private static final Logger LOG = Logger.getLogger( ClusterConfiguration.class.getName() );
    private TrackerOperation po;
    private CassandraImpl cassandraManager;


    public ClusterConfiguration( final TrackerOperation operation, final CassandraImpl cassandraManager )
    {
        this.po = operation;
        this.cassandraManager = cassandraManager;
    }


    //TODO use host.getInterfaces instead of Agents
    public void configureCluster( CassandraClusterConfig config, Environment environment )
            throws ClusterConfigurationException
    {

        po.addLog( String.format( "Configuring cluster: %s", config.getClusterName() ) );
        String script = ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s";
        String permissionParam = "sudo chmod 750 $CASSANDRA_HOME";
        String clusterNameParam = "cluster_name " + config.getClusterName();
        String dataDirParam = "data_dir " + config.getDataDirectory();
        String commitLogDirParam = "commitlog_dir " + config.getCommitLogDirectory();
        String savedCacheDirParam = "saved_cache_dir " + config.getSavedCachesDirectory();


        StringBuilder sb = new StringBuilder();
        for ( UUID uuid : config.getSeedNodes() )
        {
            ContainerHost containerHost = environment.getContainerHostById( uuid );
            sb.append( containerHost.getIpByMask( Common.IP_MASK ) ).append( "," );
        }
        if ( ! sb.toString().isEmpty() ){
            sb.replace( sb.toString().length() - 1, sb.toString().length(), "" );
        }
        String seedsParam = "seeds " + sb.toString();


        for ( UUID uuid : config.getNodes() )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostById( uuid );
                po.addLog( "Configuring node: " + containerHost.getHostname() );

                // Setting permission
                CommandResult commandResult = containerHost.execute( new RequestBuilder( permissionParam ) );
                po.addLog( commandResult.getStdOut() );

                // Setting cluster name
                commandResult =
                        containerHost.execute( new RequestBuilder( String.format( script, clusterNameParam ) ) );
                po.addLog( commandResult.getStdOut() );

                // Create directories
                commandResult = containerHost
                        .execute( new RequestBuilder( String.format( "mkdir -p %s", config.getDataDirectory() ) ) );
                po.addLog( commandResult.getStdOut() );
                commandResult = containerHost.execute(
                        new RequestBuilder( String.format( "mkdir -p %s", config.getCommitLogDirectory() ) ) );
                po.addLog( commandResult.getStdOut() );
                commandResult = containerHost.execute(
                        new RequestBuilder( String.format( "mkdir -p %s", config.getSavedCachesDirectory() ) ) );
                po.addLog( commandResult.getStdOut() );

                // Configure directories
                commandResult = containerHost.execute( new RequestBuilder( String.format( script, dataDirParam ) ) );
                po.addLog( commandResult.getStdOut() );

                commandResult =
                        containerHost.execute( new RequestBuilder( String.format( script, commitLogDirParam ) ) );
                po.addLog( commandResult.getStdOut() );

                commandResult =
                        containerHost.execute( new RequestBuilder( String.format( script, savedCacheDirParam ) ) );
                po.addLog( commandResult.getStdOut() );

                // Set RPC address
                String rpcAddress =
                        String.format( ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s %s", "rpc_address",
                                containerHost.getIpByMask( Common.IP_MASK ) );
                commandResult = containerHost.execute( new RequestBuilder( rpcAddress ) );
                po.addLog( commandResult.getStdOut() );

                // Set listen address
                String listenAddress = String.format( ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s %s",
                        "listen_address", containerHost.getIpByMask( Common.IP_MASK ) );
                commandResult = containerHost.execute( new RequestBuilder( listenAddress ) );
                po.addLog( commandResult.getStdOut() );

                // Configure seeds
                commandResult = containerHost.execute( new RequestBuilder( String.format( script, seedsParam ) ) );
                po.addLog( commandResult.getStdOut() );
            }
            catch ( CommandException e )
            {
                po.addLogFailed( String.format( "Installation failed" ) );
                throw new ClusterConfigurationException( e.getMessage() );
            }
        }

        config.setEnvironmentId( environment.getId() );
        cassandraManager.getPluginDAO().saveInfo( CassandraClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLogDone( "Cassandra cluster data saved into database" );
    }
}
