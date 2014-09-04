package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.plugin.cassandra.impl.handler.CheckClusterHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.CheckNodeHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.CheckServiceHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.InstallClusterHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StartClusterHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StartServiceHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StopClusterHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StopServiceHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.UninstallClusterHandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class CassandraImpl extends CassandraBase implements Cassandra {


    public CassandraImpl() {

    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public void setExecutor( final ExecutorService executor ) {
        this.executor = executor;
    }


    public void init() {
        Commands.init( commandRunner );
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public UUID installCluster( final CassandraConfig config ) {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new InstallClusterHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new UninstallClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new StartClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new StopClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startService( final String clusterName, final String agentUUID ) {
        AbstractOperationHandler operationHandler = new StartServiceHandler( this, clusterName, agentUUID );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopService( final String clusterName, final String agentUUID ) {
        AbstractOperationHandler operationHandler = new StopServiceHandler( this, clusterName, agentUUID );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusService( final String clusterName, final String agentUUID ) {
        AbstractOperationHandler operationHandler = new CheckServiceHandler( this, clusterName, agentUUID );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final CassandraConfig config,
                                                         final ProductOperation po ) {
        return new CassandraSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final CassandraConfig config ) {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", config.PRODUCT_KEY, UUID.randomUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        nodeGroup.setNumberOfNodes( config.getNumberOfNodes() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );
        return environmentBlueprint;
    }


    @Override
    public UUID checkCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new CheckClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public List<CassandraConfig> getClusters() {
        return dbManager.getInfo( CassandraConfig.PRODUCT_KEY, CassandraConfig.class );
    }


    @Override
    public CassandraConfig getCluster( String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        try {
            return pluginDAO.getInfo( CassandraConfig.PRODUCT_KEY, clusterName, CassandraConfig.class );
        }
        catch ( DBException e ) {
            return null;
        }
    }


    @Override
    public UUID addNode( final String clusterName, final String lxchostname, final String nodetype ) {
        // TODO
        return null;
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxchostname, final String nodetype ) {
        // TODO
        return null;
    }


    @Override
    public UUID checkNode( final String clustername, final String lxchostname ) {
        AbstractOperationHandler operationHandler = new CheckNodeHandler( this, clustername, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }
}
