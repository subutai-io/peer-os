package org.safehaus.subutai.plugin.jetty.impl;


import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDao;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.jetty.api.Jetty;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.handler.CheckClusterHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.CheckServiceHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.StartClusterHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.StartServiceHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.StopClusterHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.StopServiceHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.UninstallOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class JettyImpl implements Jetty
{
    private static final Logger LOG = LoggerFactory.getLogger( JettyImpl.class.getName() );
    private Commands commands;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;
    private NetworkManager networkManager;
    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;
    private PluginDao pluginDAO;
    private DataSource dataSource;


    public JettyImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public LxcManager getLxcManager()
    {
        return lxcManager;
    }


    public void setLxcManager( final LxcManager lxcManager )
    {
        this.lxcManager = lxcManager;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public NetworkManager getNetworkManager()
    {
        return networkManager;
    }


    public void setNetworkManager( final NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public PluginDao getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( final PluginDao pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public void setCommands( Commands commands )
    {
        this.commands = commands;
    }


    public void init()
    {
        try
        {
            this.pluginDAO = new PluginDao( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        this.commands = new Commands( commandRunner );

        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public UUID installCluster( final JettyConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public List<JettyConfig> getClusters()
    {
        return pluginDAO.getInfo( JettyConfig.PRODUCT_KEY, JettyConfig.class );
    }


    @Override
    public JettyConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        return pluginDAO.getInfo( JettyConfig.PRODUCT_KEY, clusterName, JettyConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    @Override
    public UUID startCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new StartClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new CheckClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new StopClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startService( final String clusterName, final String lxchostname )
    {
        AbstractOperationHandler operationHandler = new StartServiceHandler( this, clusterName, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopService( final String clusterName, final String lxchostname )
    {
        AbstractOperationHandler operationHandler = new StopServiceHandler( this, clusterName, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusService( final String clusterName, final String lxchostname )
    {
        AbstractOperationHandler operationHandler = new CheckServiceHandler( this, clusterName, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public EnvironmentBuildTask getDefaultEnvironmentBlueprint( final JettyConfig config ) throws ClusterSetupException
    {
        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint
                .setName( String.format( "%s-%s", JettyConfig.PRODUCT_KEY, UUIDUtil.generateTimeBasedUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setExchangeSshKeys( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );


        Set<NodeGroup> nodeGroups = new HashSet<>();

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setName( NodeType.SLAVE_NODE.name() );
        nodeGroup.setNumberOfNodes( config.getNumberOfNodes() );
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );

        nodeGroups.add( nodeGroup );

        environmentBlueprint.setNodeGroups( nodeGroups );

        environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );
        environmentBuildTask.setPhysicalNodes( getSetOfAgentsHostName() );


        return environmentBuildTask;
    }


    private Set<String> getSetOfAgentsHostName() throws ClusterSetupException
    {
        Set<Agent> agents = agentManager.getPhysicalAgents();

        if ( agents != null && !agents.isEmpty() )
        {
            Set<String> hostNames = new HashSet<>( agents.size() );

            for ( Agent agent : agents )
            {
                hostNames.add( agent.getHostname() );
            }

            return hostNames;
        }
        else
        {
            throw new ClusterSetupException( "No physical machines available" );
        }
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment env, final JettyConfig config,
                                                         final TrackerOperation po )
    {
        Set<Agent> cassNodes = new HashSet<Agent>();
        for ( EnvironmentContainer environmentContainer : env.getContainers() )
        {
            cassNodes.add( environmentContainer.getAgent() );
        }
        config.setNodes( cassNodes );

        return new JettySetupStrategy( this, config, po );
    }
}
