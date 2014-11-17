package org.safehaus.subutai.core.container.impl;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.Container;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerEvent;
import org.safehaus.subutai.core.container.api.ContainerEventListener;
import org.safehaus.subutai.core.container.api.ContainerEventType;
import org.safehaus.subutai.core.container.api.ContainerException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.container.api.ContainerState;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.monitor.api.Monitoring;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.template.api.TemplateException;
import org.safehaus.subutai.core.template.api.TemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class ContainerManagerImpl extends ContainerManagerBase
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerManagerImpl.class );
    private static final long WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS = 10000;
    private static final int MAX_LXC_NAME = 15;
    private final Pattern loadAveragePattern = Pattern.compile( "load average: (.*)" );
    private final Queue<ContainerEventListener> listeners = new ConcurrentLinkedQueue<>();
    private ConcurrentMap<String, AtomicInteger> sequences;
    private ExecutorService executor;
    private final Commands commands;


    public ContainerManagerImpl( AgentManager agentManager, CommandRunner commandRunner, Monitoring monitoring,
                                 TemplateManager templateManager, TemplateRegistry templateRegistry,
                                 StrategyManager strategyManager )
    {
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.monitoring = monitoring;
        this.templateManager = templateManager;
        this.templateRegistry = templateRegistry;

        this.strategyManager = strategyManager;
        this.commands = new Commands( commandRunner );
    }


    public void init()
    {
        Preconditions.checkNotNull( agentManager, "Agent manager is null" );
        Preconditions.checkNotNull( commandRunner, "Command runner is null" );
        Preconditions.checkNotNull( monitoring, "Monitor is null" );

        sequences = new ConcurrentHashMap<>();
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        sequences.clear();
        executor.shutdown();
    }


    @Override
    public Container getContainerByUuid( final UUID uuid )
    {
        Agent agent = agentManager.getAgentByUUID( uuid );
        if ( agent != null )
        {
            return new ContainerImpl( agent, this, agentManager, commandRunner );
        }
        else
        {
            return null;
        }
    }


    @Override
    public Set<Agent> clone( final UUID envId, final Agent agent, final String templateName,
                             final Set<String> cloneNames ) throws ContainerCreateException
    {
        Set<Agent> successfullyClonedContainers = new HashSet<>();
        ContainerEventListener listener = new ContainerEventListenerImpl( successfullyClonedContainers );

        try
        {
            addListener( listener );
            ExecutorService executor = Executors.newFixedThreadPool( 1 );
            execute( agent, envId, templateName, cloneNames, executor, ContainerAction.CREATE );
            executor.shutdown();
        }
        catch ( ContainerException e )
        {
            // roll back on container exception: destroying successfully cloned containers
            for ( Agent a : successfullyClonedContainers )
            {
                try
                {
                    destroy( a.getParentHostName(), a.getHostname() );
                }
                catch ( ContainerDestroyException cde )
                {
                    LOG.error( String.format( "Destroying container %s on %s failed.", a.getHostname(),
                            a.getParentHostName() ) );
                }
            }
            successfullyClonedContainers.clear();
        }
        finally
        {
            removeListener( listener );
        }
        return successfullyClonedContainers;
    }


    @Override
    public Set<Agent> clone( final UUID envId, final String templateName, final int numOfContainers,
                             final String strategyId, final List<Criteria> criteria ) throws ContainerCreateException
    {

        Map<Agent, Integer> slots = new HashMap<>();
        try
        {
            Map<ServerMetric, Integer> distribution = strategyManager
                    .getPlacementDistribution( getPhysicalServerMetrics(), numOfContainers, strategyId, criteria );
            for ( Map.Entry<ServerMetric, Integer> e : distribution.entrySet() )
            {
                Agent agent = agentManager.getAgentByHostname( e.getKey().getHostname() );
                if ( agent == null )
                {
                    throw new ContainerCreateException(
                            String.format( "Resource host %s unavailable.", e.getKey().getHostname() ) );
                }
                slots.put( agent, e.getValue() );
            }
        }
        catch ( StrategyException e )
        {
            throw new ContainerCreateException( e.getMessage() );
        }

        // clone specified number of instances and store their names
        Map<Agent, Set<String>> cloneNames = new HashMap<>();

        Set<String> existingContainerNames = getContainerNames( slots.keySet() );

        for ( Map.Entry<Agent, Integer> e : slots.entrySet() )
        {
            Set<String> hostCloneNames = new HashSet<>();
            for ( int i = 0; i < e.getValue(); i++ )
            {
                String newContainerName = nextHostName( templateName, existingContainerNames );
                hostCloneNames.add( newContainerName );
            }
            cloneNames.put( e.getKey(), hostCloneNames );
        }

        CompletionService completionService = new ExecutorCompletionService<>( executor );
        final ContainerManager self = this;
        for ( final Map.Entry<Agent, Set<String>> e : cloneNames.entrySet() )
        {
            completionService.submit( new Callable<Set<Agent>>()
            {
                @Override
                public Set<Agent> call() throws Exception
                {
                    return self.clone( envId, e.getKey(), templateName, e.getValue() );
                }
            } );
        }

        Set<Agent> result = new HashSet<Agent>();

        for ( Agent ignore : cloneNames.keySet() )
        {
            try
            {
                Future<Set<Agent>> future = completionService.take();
                Set<Agent> agents = future.get();
                result.addAll( agents );
            }
            catch ( InterruptedException | ExecutionException e )
            {
                throw new ContainerCreateException( "Not all containers created successfully." );
            }
        }
        return result;
    }


    /**
     * Clones container by given environment ID, physical host and template name.
     *
     * @param envId environment UUID
     * @param hostName name of physical host
     * @param templateName used template name on cloning
     * @param cloneName host name of cloned container
     */
    @Override
    public Agent clone( UUID envId, String hostName, String templateName, String cloneName )
            throws ContainerCreateException
    {
        fireEvent( new ContainerEvent( ContainerEventType.CLONING_STARTED, hostName, cloneName ) );
        try
        {
            templateManager.clone( hostName, templateName, cloneName, envId.toString() );
        }
        catch ( TemplateException te )
        {
            fireEvent( new ContainerEvent( ContainerEventType.CLONING_FAILED, hostName, cloneName ) );
            throw new ContainerCreateException(
                    String.format( "Couldn't create container %s : %s. %s", hostName, cloneName, te.toString() ) );
        }
        Agent agent = agentManager.waitForRegistration( cloneName, Common.LXC_AGENT_WAIT_TIMEOUT_SEC * 1000 );
        if ( agent == null )
        {
            fireEvent( new ContainerEvent( ContainerEventType.CLONING_FAILED, hostName, cloneName ) );
            throw new ContainerCreateException(
                    String.format( "Couldn't create container %s : %s. Could not get response from container.",
                            hostName, cloneName ) );
        }
        else
        {
            fireEvent( new ContainerEvent( ContainerEventType.CLONING_SUCCEED, hostName, cloneName ) );
            return agent;
        }
    }


    @Override
    public void destroy( String hostName, String cloneName ) throws ContainerDestroyException
    {
        fireEvent( new ContainerEvent( ContainerEventType.DESTROYING_STARTED, hostName, cloneName ) );
        if ( !templateManager.cloneDestroy( hostName, cloneName ) )
        {
            fireEvent( new ContainerEvent( ContainerEventType.DESTROYING_FAILED, hostName, cloneName ) );
            throw new ContainerDestroyException(
                    String.format( "Could not destroy container %s : %s.", hostName, cloneName ) );
        }
        else
        {
            fireEvent( new ContainerEvent( ContainerEventType.DESTROYING_SUCCEED, hostName, cloneName ) );
        }
    }


    private void fireEvent( final ContainerEvent containerEvent )
    {
        for ( Iterator<ContainerEventListener> it = listeners.iterator(); it.hasNext(); )
        {
            ContainerEventListener listener = it.next();
            try
            {
                listener.onContainerEvent( containerEvent );
            }
            catch ( Exception e )
            {
                it.remove();
                LOG.error( "Error notifying container event listeners, removing faulting listener", e );
            }
        }
    }


    /**
     * Returns information about what lxc containers each physical servers has at present
     *
     * @return map where key is a hostname of physical server and value is a map where key is state of lxc and value is
     * a list of lxc hostnames
     */
    public Map<String, EnumMap<ContainerState, List<String>>> getContainersOnPhysicalServers()
    {
        final Map<String, EnumMap<ContainerState, List<String>>> agentFamilies = new HashMap<>();
        Set<Agent> pAgents = agentManager.getPhysicalAgents();
        for ( Iterator<Agent> it = pAgents.iterator(); it.hasNext(); )
        {
            Agent agent = it.next();
            if ( !agent.getHostname().matches( "^py.*" ) )
            {
                it.remove();
            }
        }
        if ( !pAgents.isEmpty() )
        {

            Command templateListCommand = commands.getTemplateListCommand( pAgents );

            commandRunner.runCommand( templateListCommand );

            Map<UUID, Set<String>> agentTemplates = new HashMap<>();
            if ( templateListCommand.hasCompleted() )
            {

                for ( AgentResult result : templateListCommand.getResults().values() )
                {
                    Set<String> templates = new HashSet<>();
                    UUID agentUuid = result.getAgentUUID();
                    String stdOut = result.getStdOut();
                    String[] outLines = result.getStdOut().split( "\\n" );

                    for ( int i = 2; i < outLines.length; i++ )
                    {
                        templates.add( outLines[i] );
                    }
                    agentTemplates.put( agentUuid, templates );
                }
            }
            Command getLxcListCommand = commands.getLxcListCommand( pAgents );
            commandRunner.runCommand( getLxcListCommand );

            if ( getLxcListCommand.hasCompleted() )
            {
                for ( AgentResult result : getLxcListCommand.getResults().values() )
                {
                    Agent agent = agentManager.getAgentByUUID( result.getAgentUUID() );

                    String parentHostname =
                            agent == null ? String.format( "Offline[%s]", result.getAgentUUID() ) : agent.getHostname();
                    EnumMap<ContainerState, List<String>> lxcs = new EnumMap<>( ContainerState.class );
                    String[] lxcStrs = result.getStdOut().split( "\\n" );

                    for ( int i = 2; i < lxcStrs.length; i++ )
                    {
                        String[] lxcProperties = lxcStrs[i].split( "\\s+" );
                        if ( lxcProperties.length > 1 )
                        {
                            String lxcHostname = lxcProperties[0];
                            if ( !agentTemplates.get( result.getAgentUUID() ).contains( lxcHostname ) )
                            //                            if ( !( Common.BASE_CONTAINER_NAME.equalsIgnoreCase(
                            // lxcHostname )
                            //                                    || Common.MASTER_TEMPLATE_NAME.equalsIgnoreCase(
                            // lxcHostname ) ) )
                            {
                                String lxcStatus = lxcProperties[1];

                                ContainerState state = ContainerState.parseState( lxcStatus );

                                if ( lxcs.get( state ) == null )
                                {
                                    lxcs.put( state, new ArrayList<String>() );
                                }
                                lxcs.get( state ).add( lxcHostname );
                            }
                        }
                    }
                    agentFamilies.put( parentHostname, lxcs );
                }
            }
        }

        return agentFamilies;
    }


    /**
     * Returns metrics of all physical servers connected to the management server
     *
     * @return map of metrics where key is a physical agent and value is a metric
     */
    @Override
    public List<ServerMetric> getPhysicalServerMetrics()
    {
        final List<ServerMetric> serverMetrics = new ArrayList<>();
        Set<Agent> agents = agentManager.getPhysicalAgents();
        //omit management server
        for ( Iterator<Agent> it = agents.iterator(); it.hasNext(); )
        {
            Agent agent = it.next();
            if ( !agent.getHostname().matches( "^py.*" ) )
            {
                it.remove();
            }
        }

        if ( agents.isEmpty() )
        {
            return serverMetrics;
        }

        Command getMetricsCommand = commands.getMetricsCommand( agents );
        commandRunner.runCommand( getMetricsCommand );

        if ( getMetricsCommand.hasCompleted() )
        {
            for ( AgentResult result : getMetricsCommand.getResults().values() )
            {
                String[] metrics = result.getStdOut().split( "\n" );
                ServerMetric serverMetric = gatherMetrics( "", metrics );
                if ( serverMetric != null )
                {
                    Agent agent = agentManager.getAgentByUUID( result.getAgentUUID() );
                    Map<MetricType, Double> averageMetrics = gatherAvgMetrics( agent );
                    serverMetric.setHostname( agent.getHostname() );
                    serverMetric.setAverageMetrics( averageMetrics );
                    serverMetrics.add( serverMetric );
                }
            }
        }

        if ( !serverMetrics.isEmpty() )
        {
            //get number of lxcs currently present on servers
            Map<String, EnumMap<ContainerState, List<String>>> lxcInfo = getContainersOnPhysicalServers();
            for ( Iterator<ServerMetric> it = serverMetrics.iterator(); it.hasNext(); )
            {
                //                Map.Entry<Agent, ServerMetric> entry = it.next();
                ServerMetric serverMetric = it.next();
                EnumMap<ContainerState, List<String>> info = lxcInfo.get( serverMetric.getHostname() );
                if ( info != null )
                {
                    int numOfExistingLxcs =
                            ( info.get( ContainerState.RUNNING ) != null ? info.get( ContainerState.RUNNING ).size() :
                              0 ) + ( info.get( ContainerState.STOPPED ) != null ?
                                      info.get( ContainerState.STOPPED ).size() : 0 ) + (
                                    info.get( ContainerState.FROZEN ) != null ?
                                    info.get( ContainerState.FROZEN ).size() : 0 );
                    serverMetric.setNumOfLxcs( numOfExistingLxcs );
                }
                else
                {
                    it.remove();
                }
            }
        }
        return serverMetrics;
    }


    /**
     * Starts lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return true if all went ok, false otherwise
     */
    public boolean startLxcOnHost( Agent physicalAgent, String lxcHostname )
    {
        if ( physicalAgent != null && !Strings.isNullOrEmpty( lxcHostname ) )
        {
            Command startLxcCommand = commands.getLxcStartCommand( physicalAgent, lxcHostname );
            commandRunner.runCommand( startLxcCommand );
            try
            {
                Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
            }
            catch ( InterruptedException ignore )
            {
            }
            Command lxcInfoCommand = commands.getLxcInfoCommand( physicalAgent, lxcHostname );
            commandRunner.runCommand( lxcInfoCommand );

            ContainerState state = ContainerState.UNKNOWN;
            if ( lxcInfoCommand.hasCompleted() )
            {
                AgentResult result = lxcInfoCommand.getResults().entrySet().iterator().next().getValue();
                if ( result.getStdOut().contains( "RUNNING" ) )
                {
                    state = ContainerState.RUNNING;
                }
            }

            return ContainerState.RUNNING.equals( state );
        }
        return false;
    }


    /**
     * Stops lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return true if all went ok, false otherwise
     */
    public boolean stopLxcOnHost( Agent physicalAgent, String lxcHostname )
    {
        if ( physicalAgent != null && !Strings.isNullOrEmpty( lxcHostname ) )
        {
            Command stopLxcCommand = commands.getLxcStopCommand( physicalAgent, lxcHostname );
            commandRunner.runCommand( stopLxcCommand );

            long thresholdTime = System.currentTimeMillis() + WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS;
            ContainerState state = ContainerState.UNKNOWN;
            do
            {
                try
                {
                    Thread.sleep( 2000 );
                }
                catch ( InterruptedException ignore )
                {
                }
                Command lxcInfoCommand = commands.getLxcInfoCommand( physicalAgent, lxcHostname );
                commandRunner.runCommand( lxcInfoCommand );
                if ( lxcInfoCommand.hasCompleted() )
                {
                    AgentResult result = lxcInfoCommand.getResults().entrySet().iterator().next().getValue();
                    if ( result.getStdOut().contains( "STOPPED" ) )
                    {
                        state = ContainerState.STOPPED;
                    }
                }
            }
            while ( thresholdTime > System.currentTimeMillis() && ContainerState.UNKNOWN.equals( state ) );

            return ContainerState.STOPPED.equals( state );
        }
        return false;
    }


    /**
     * Adds listener which wants to be notified when agents connect/disconnect
     *
     * @param listener - listener to add
     */
    @Override
    public void addListener( ContainerEventListener listener )
    {
        try
        {
            if ( !listeners.contains( listener ) )
            {
                listeners.add( listener );
            }
        }
        catch ( Exception ex )
        {
            //LOG.error( "Error on adding container event listener", ex );
        }
    }


    /**
     * Removes listener
     *
     * @param listener - - listener to remove
     */
    @Override
    public void removeListener( ContainerEventListener listener )
    {
        try
        {
            listeners.remove( listener );
        }
        catch ( Exception ex )
        {
            //LOG.error( "Error on removing container event listener", ex );
        }
    }


    /**
     * Gather metrics from elastic search for a one week period
     */
    private Map<MetricType, Double> gatherAvgMetrics( Agent agent )
    {

        if ( agent == null )
        {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.DATE, -7 );
        Date startDate = cal.getTime();
        Date endDate = Calendar.getInstance().getTime();
        Map<MetricType, Double> averageMetrics = new EnumMap<>( MetricType.class );
        return averageMetrics;
    }


    /**
     * Gather metrics from linux commands outputs.
     */
    private ServerMetric gatherMetrics( String hostname, String[] metrics )
    {
        int freeRamMb = 0;
        int freeHddMb = 0;
        int numOfProc = 0;
        double loadAvg = 0;
        double cpuLoadPercent = 100;
        // parsing only 4 metrics
        if ( metrics.length != 4 )
        {
            return null;
        }
        boolean parseOk = true;
        for ( int line = 0; parseOk && line < metrics.length; line++ )
        {
            String metric = metrics[line];
            switch ( line )
            {
                case 0:
                    //-/+ buffers/cache:       1829       5810
                    String[] ramMetric = metric.split( "\\s+" );
                    String freeRamMbStr = ramMetric[ramMetric.length - 1];
                    try
                    {
                        freeRamMb = Integer.parseInt( freeRamMbStr );
                    }
                    catch ( Exception e )
                    {
                        parseOk = false;
                    }
                    break;
                case 1:
                    //lxc-data       143264768 608768 142656000   1% /lxc-data
                    String[] hddMetric = metric.split( "\\s+" );
                    if ( hddMetric.length == 6 )
                    {
                        String hddMetricKbStr = hddMetric[3];
                        try
                        {
                            freeHddMb = Integer.parseInt( hddMetricKbStr ) / 1024;
                        }
                        catch ( Exception e )
                        {
                            parseOk = false;
                        }
                    }
                    else
                    {
                        parseOk = false;
                    }
                    break;
                case 2:
                    // 09:17:38 up 4 days, 23:06,  0 users,  load average: 2.18, 3.06, 2.12
                    Matcher m = loadAveragePattern.matcher( metric );
                    if ( m.find() )
                    {
                        String[] loads = m.group( 1 ).split( "," );
                        try
                        {
                            loadAvg = ( Double.parseDouble( loads[0] ) + Double.parseDouble( loads[1] ) + Double
                                    .parseDouble( loads[2] ) ) / 3;
                        }
                        catch ( Exception e )
                        {
                            parseOk = false;
                        }
                    }
                    else
                    {
                        parseOk = false;
                    }
                    break;
                case 3:
                    try
                    {
                        numOfProc = Integer.parseInt( metric );
                        if ( numOfProc > 0 )
                        {
                            cpuLoadPercent = ( loadAvg / numOfProc ) * 100;
                        }
                        else
                        {
                            break;
                        }
                    }
                    catch ( Exception e )
                    {
                        parseOk = false;
                    }
                    break;
            }
        }
        if ( parseOk )
        {
            ServerMetric serverMetric =
                    new ServerMetric( hostname, freeHddMb, freeRamMb, ( int ) cpuLoadPercent, numOfProc, null );
            return serverMetric;
        }
        else
        {
            return null;
        }
    }


    private String nextHostName( String templateName, Set<String> existingNames )
    {
        AtomicInteger i = sequences.putIfAbsent( templateName, new AtomicInteger() );
        if ( i == null )
        {
            i = sequences.get( templateName );
        }
        while ( true )
        {
            String suffix = String.valueOf( i.incrementAndGet() );
            int prefixLen = MAX_LXC_NAME - suffix.length();
            String name = ( templateName.length() > prefixLen ? templateName.substring( 0, prefixLen ) : templateName )
                    + suffix;
            if ( !existingNames.contains( name ) )
            {
                return name;
            }
        }
    }


    private Set<String> getContainerNames( Collection<Agent> hostsToCheck )
    {
        Map<String, EnumMap<ContainerState, List<String>>> map = getContainersOnPhysicalServers();

        if ( hostsToCheck != null && !hostsToCheck.isEmpty() )
        {
            Iterator<String> it = map.keySet().iterator();
            while ( it.hasNext() )
            {
                String hostname = it.next();
                boolean hostIncluded = false;
                for ( Agent agent : hostsToCheck )
                {
                    if ( agent.getHostname().equalsIgnoreCase( hostname ) )
                    {
                        hostIncluded = true;
                        break;
                    }
                }
                if ( !hostIncluded )
                {
                    it.remove();
                }
            }
        }

        Set<String> lxcHostNames = new HashSet<>();
        for ( EnumMap<ContainerState, List<String>> lxcsOnOneHost : map.values() )
        {
            for ( List<String> hosts : lxcsOnOneHost.values() )
            {
                lxcHostNames.addAll( hosts );
            }
        }
        return lxcHostNames;
    }


    private void execute( Agent agent, UUID envId, String templateName, Set<String> cloneNames,
                          ExecutorService executor, ContainerAction containerAction ) throws ContainerException
    {

        //prepare ContainerInfo list for execution
        List<ContainerInfo> containerInfoList = new ArrayList<>( cloneNames.size() );
        for ( String cloneName : cloneNames )
        {
            ContainerInfo containerInfo = new ContainerInfo( agent, envId, templateName, cloneName, containerAction );
            containerInfoList.add( containerInfo );
        }

        CompletionService<ContainerInfo> completer = new ExecutorCompletionService<>( executor );

        //launch destroy commands
        for ( ContainerInfo containerInfo : containerInfoList )
        {
            completer.submit( new ContainerActor( containerInfo, this ) );
        }

        //wait for completion
        try
        {
            for ( ContainerInfo ignored : containerInfoList )
            {
                Future<ContainerInfo> future = completer.take();
                future.get();
            }
        }
        catch ( InterruptedException | ExecutionException ignore )
        {
        }

        boolean result = true;
        for ( ContainerInfo containerInfo : containerInfoList )
        {
            result &= containerInfo.isOk();
        }

        if ( !result )
        {
            throw new ContainerException( "Not all actions performed successfully" );
        }
    }


    @Override
    public void destroy( final String hostName, final Set<String> cloneNames ) throws ContainerDestroyException
    {
        boolean result = templateManager.cloneDestroy( hostName, cloneNames );
        if ( !result )
        {
            throw new ContainerDestroyException(
                    String.format( "Not all containers from %s are destroyed. Use LXC module to cleanup",
                            cloneNames ) );
        }
    }


    private class ContainerEventListenerImpl implements ContainerEventListener
    {
        private Set<Agent> successfullyClonedContainers;


        ContainerEventListenerImpl( Set<Agent> successfullyClonedContainers )
        {
            this.successfullyClonedContainers = successfullyClonedContainers;
        }


        public void onContainerEvent( ContainerEvent event )
        {
            if ( event.getEventType() == ContainerEventType.CLONING_SUCCEED )
            {
                Agent agent = agentManager.getAgentByHostname( event.getHostname() );
                if ( agent != null )
                {
                    successfullyClonedContainers.add( agent );
                }
            }
        }
    }
}
