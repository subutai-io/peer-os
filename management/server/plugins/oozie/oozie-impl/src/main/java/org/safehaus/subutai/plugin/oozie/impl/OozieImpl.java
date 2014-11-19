package org.safehaus.subutai.plugin.oozie.impl;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.SetupType;
import org.safehaus.subutai.plugin.oozie.impl.handler.AddNodeHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.CheckServerHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.StartServerHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.StopServerHandler;
import org.safehaus.subutai.plugin.oozie.impl.handler.UninstallHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class OozieImpl implements Oozie
{

    private static final Logger LOG = LoggerFactory.getLogger( OozieImpl.class.getName() );
    private PluginDAO pluginDAO;
    private Commands commands;
    private Tracker tracker;

    private EnvironmentManager environmentManager;
    private Hadoop hadoopManager;
    private ExecutorService executor;
    private DataSource dataSource;


    public OozieImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public void init()
    {
        try
        {
            this.pluginDAO = new PluginDAO( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        this.commands = new Commands();

        executor = Executors.newCachedThreadPool();
    }


    public Commands getCommands()
    {
        return commands;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( final PluginDAO pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public void destroy()
    {
        tracker = null;
        hadoopManager = null;
        executor.shutdown();
    }


    public UUID installCluster( final OozieClusterConfig config )
    {
        AbstractOperationHandler operationHandler = new InstallHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new UninstallHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public List<OozieClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( OozieClusterConfig.PRODUCT_KEY, OozieClusterConfig.class );
    }


    public OozieClusterConfig getCluster( String clusterName )
    {
        return pluginDAO.getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName, OozieClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new AddNodeHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID startServer( final OozieClusterConfig config )
    {
        AbstractOperationHandler operationHandler = new StartServerHandler( this, config.getClusterName() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID stopServer( final OozieClusterConfig config )
    {
        AbstractOperationHandler operationHandler = new StopServerHandler( this, config.getClusterName() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID checkServerStatus( final OozieClusterConfig config )
    {
        AbstractOperationHandler operationHandler = new CheckServerHandler( this, config.getClusterName() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final OozieClusterConfig config,
                                                         final TrackerOperation po )
    {

        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            return new OverHadoopSetupStrategy( this, po, config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            WithHadoopSetupStrategy s = new WithHadoopSetupStrategy( this, po, config );
            s.setEnvironment( environment );
            return s;
        }
        return null;

        //        return new OozieSetupStrategy( environment, config, po, this );
    }


    public EnvironmentBuildTask getDefaultEnvironmentBlueprint( final OozieClusterConfig config )
    {

        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", config.PRODUCT_KEY, UUIDUtil.generateTimeBasedUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        NodeGroup oozieGroup = new NodeGroup();
        oozieGroup.setTemplateName( config.getTemplateNameClient() );
        oozieGroup.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        int numberOfNodes = config.getClients().size();
        oozieGroup.setNumberOfNodes( numberOfNodes );

        NodeGroup oozieServer = new NodeGroup();
        oozieServer.setTemplateName( config.getTemplateNameServer() );
        oozieServer.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        oozieServer.setNumberOfNodes( 1 );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( oozieGroup ) );
        environmentBlueprint.setNodeGroups( Sets.newHashSet( oozieServer ) );

        environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );

        return environmentBuildTask;
    }


    @Override
    public UUID addNode( final String clustername, final String lxchostname, final String nodetype )
    {
        return null;
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxcHostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }
}
