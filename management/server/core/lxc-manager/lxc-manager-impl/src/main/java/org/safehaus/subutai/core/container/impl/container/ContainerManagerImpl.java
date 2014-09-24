package org.safehaus.subutai.core.container.impl.container;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcState;
import org.safehaus.subutai.core.container.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.core.container.impl.strategy.PlacementStrategyFactory;
import org.safehaus.subutai.core.registry.api.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ContainerManagerImpl extends ContainerManagerBase
{

    private static final Logger LOGGER = LoggerFactory.getLogger( ContainerManager.class );
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    // number sequences for template names used for new clone name generation
    private ConcurrentMap<String, AtomicInteger> sequences;
    private ExecutorService executor;
    private Map<String, Set<String>> cloneNames;


    public void init()
    {
        sequences = new ConcurrentHashMap<>();
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        sequences.clear();
    }


    @Override
    public Set<Agent> clone( UUID envId, String templateName, int nodesCount, Collection<Agent> hosts,
                             PlacementStrategy... strategy ) throws LxcCreateException
    {
        clonesCreate( hosts, templateName, nodesCount, strategy );

        Set<Agent> clones = processClones( cloneNames );

        try
        {
            if ( envId != null )
            {
                saveNodeGroup( envId, templateName, clones, strategy );
            }
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Failed to save nodes info", ex );
        }

        return clones;
    }


    private void clonesCreate( final Collection<Agent> hosts, final String templateName, final int nodesCount,
                               final PlacementStrategy[] strategy ) throws LxcCreateException
    {
        // restrict metrics to provided hosts only
        Map<Agent, ServerMetric> metrics = lxcManager.getPhysicalServerMetrics();
        if ( hosts != null && !hosts.isEmpty() )
        {
            Iterator<Agent> it = metrics.keySet().iterator();
            while ( it.hasNext() )
            {
                if ( !hosts.contains( it.next() ) )
                {
                    it.remove();
                }
            }
        }

        LxcPlacementStrategy st = PlacementStrategyFactory.create( nodesCount, strategy );
        st.calculatePlacement( metrics );

        Map<Agent, Integer> slots = st.getPlacementDistribution();

        int totalSlots = 0;

        for ( int slotCount : slots.values() )
        {
            totalSlots += slotCount;
        }

        if ( totalSlots == 0 )
        {
            throw new LxcCreateException( "Lxc placement strategy returned empty set" );
        }

        if ( totalSlots < nodesCount )
        {
            throw new LxcCreateException( String.format( "Only %d containers can be created", totalSlots ) );
        }

        // clone specified number of instances and store their names
        cloneNames = new HashMap<>();
        Set<String> existingContainerNames = getContainerNames( hosts );
        List<ContainerInfo> lxcInfos = new ArrayList<>();
        for ( Map.Entry<Agent, Integer> e : slots.entrySet() )
        {
            Set<String> hostCloneNames = new HashSet<>();
            for ( int i = 0; i < e.getValue(); i++ )
            {
                String newContainerName = nextHostName( templateName, existingContainerNames );
                hostCloneNames.add( newContainerName );
            }
            cloneNames.put( e.getKey().getHostname(), hostCloneNames );
            ContainerInfo lxcInfo = new ContainerInfo( e.getKey(), hostCloneNames );
            lxcInfos.add( lxcInfo );
        }

        if ( !lxcInfos.isEmpty() )
        {

            CompletionService<ContainerInfo> completer = new ExecutorCompletionService<>( executor );
            //launch create commands
            for ( ContainerInfo lxcInfo : lxcInfos )
            {
                completer.submit( new ContainerActor( lxcInfo, this, ContainerAction.CREATE, templateName ) );
            }

            //wait for completion
            try
            {
                for ( ContainerInfo ignored : lxcInfos )
                {
                    Future<ContainerInfo> future = completer.take();
                    future.get();
                }
            }
            catch ( InterruptedException | ExecutionException ignore )
            {
                LOGGER.info( "ContainerManagerImpl@clone: " + ignore.getMessage(), ignore );
            }

            boolean result = true;
            for ( ContainerInfo lxcInfo : lxcInfos )
            {
                result &= lxcInfo.isResult();
            }

            if ( !result )
            {
                throw new LxcCreateException(
                        String.format( "Not all lxcs created. Use LXC module to cleanup %s", cloneNames ) );
            }
        }
        else
        {
            throw new LxcCreateException( "Empty container infos provided" );
        }
    }


    private Set<Agent> processClones( Map<String, Set<String>> cloneNames ) throws LxcCreateException
    {
        boolean result = true;
        long waitStart = System.currentTimeMillis();
        Set<Agent> clones = new HashSet<>();
        while ( !Thread.interrupted() )
        {
            result = true;
            outerloop:
            for ( Set<String> names : cloneNames.values() )
            {
                for ( String cloneName : names )
                {
                    Agent lxcAgent = agentManager.getAgentByHostname( cloneName );
                    if ( lxcAgent == null )
                    {
                        result = false;
                        break outerloop;
                    }
                    else
                    {
                        clones.add( lxcAgent );
                    }
                }
            }
            if ( result )
            {
                break;
            }
            else
            {
                if ( System.currentTimeMillis() - waitStart > Common.LXC_AGENT_WAIT_TIMEOUT_SEC * 1000 )
                {
                    break;
                }
                else
                {
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException ex )
                    {
                        break;
                    }
                }
            }
        }

        if ( !result )
        {
            //destroy clones
            Set<String> names = new HashSet<>();

            for ( Map.Entry<String, Set<String>> entry : cloneNames.entrySet() )
            {
                names.addAll( entry.getValue() );
            }
            try
            {
                clonesDestroyByHostname( names );
            }
            catch ( LxcDestroyException ignore )
            {
                LOGGER.warn( "ContainerManagerImpl@" );
            }

            throw new LxcCreateException(
                    String.format( "Waiting interval for lxc agents timed out. Use LXC module to cleanup nodes %s",
                            cloneNames ) );
        }
        return clones;
    }


    @Override
    public Set<Agent> clone( String templateName, int nodesCount, Collection<Agent> hosts,
                             PlacementStrategy... strategy ) throws LxcCreateException
    {
        return clone( null, templateName, nodesCount, hosts, strategy );
    }


    @Override
    public boolean attachAndExecute( Agent physicalHost, String cloneName, String cmd )
    {
        return attachAndExecute( physicalHost, cloneName, cmd, 30, TimeUnit.SECONDS );
    }


    @Override
    public boolean attachAndExecute( Agent physicalHost, String cloneName, String cmd, long t, TimeUnit unit )
    {
        if ( cmd == null || cmd.isEmpty() )
        {
            return false;
        }
        // synopsis:
        // lxc-attach {-n name} [-a arch] [-e] [-s namespaces] [-R] [--keep-env] [--clear-env] [-- command]

        int timeout = ( int ) unit.toSeconds( t );
        Command comm = commandRunner.createCommand(
                new RequestBuilder( "lxc-attach -n " + cloneName + " -- " + cmd ).withTimeout( timeout ),
                new HashSet<>( Arrays.asList( physicalHost ) ) );
        commandRunner.runCommand( comm );
        return comm.hasSucceeded();
    }


    @Override
    public void cloneDestroy( final String hostName, final String cloneName ) throws LxcDestroyException
    {
        boolean result = templateManager.cloneDestroy( hostName, cloneName );
        if ( !result )
        {
            throw new LxcDestroyException( String.format( "Error destroying container %s", cloneName ) );
        }
    }


    @Override
    public void clonesDestroy( final String hostName, final Set<String> cloneNames ) throws LxcDestroyException
    {
        boolean result = templateManager.cloneDestroy( hostName, cloneNames );
        if ( !result )
        {
            throw new LxcDestroyException(
                    String.format( "Not all containers from %s are destroyed. Use LXC module to cleanup",
                            cloneNames ) );
        }
    }


    @Override
    public void clonesCreate( final String hostName, final String templateName, final Set<String> cloneNames )
            throws LxcCreateException
    {
        //TODO: check envId
        boolean result = templateManager.clone( hostName, templateName, cloneNames, "test" );
        if ( !result )
        {
            throw new LxcCreateException(
                    String.format( "Not all containers from %s : %s are created. Use LXC module to cleanup", hostName,
                            cloneNames ) );
        }
    }


    @Override
    public void clonesDestroyByHostname( final Set<String> cloneNames ) throws LxcDestroyException
    {
        if ( cloneNames == null || cloneNames.isEmpty() )
        {
            throw new LxcDestroyException( "Clone names is empty or null" );
        }

        Set<Agent> lxcAgents = new HashSet<>();
        for ( String lxcHostname : cloneNames )
        {
            if ( lxcHostname != null )
            {
                Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                if ( lxcAgent == null )
                {
                    throw new LxcDestroyException( String.format( "Lxc %s is not connected", lxcHostname ) );
                }
                lxcAgents.add( lxcAgent );
            }
        }

        clonesDestroy( lxcAgents );
    }


    @Override
    public void clonesDestroy( Set<Agent> lxcAgents ) throws LxcDestroyException
    {
        if ( lxcAgents == null || lxcAgents.isEmpty() )
        {
            throw new LxcDestroyException( "LxcAgents is null or empty" );
        }

        Map<Agent, Set<Agent>> families = new HashMap<>();
        for ( Agent lxcAgent : lxcAgents )
        {
            if ( lxcAgent != null )
            {
                Agent parentAgent = agentManager.getAgentByHostname( lxcAgent.getParentHostName() );
                if ( parentAgent == null )
                {
                    throw new LxcDestroyException(
                            String.format( "Physical parent of %s is not connected", lxcAgent.getHostname() ) );
                }
                Set<Agent> lxcChildren = families.get( parentAgent );
                if ( lxcChildren == null )
                {
                    lxcChildren = new HashSet<>();
                    families.put( parentAgent, lxcChildren );
                }
                lxcChildren.add( lxcAgent );
            }
        }

        cloneDestroy( families );
    }


    private void cloneDestroy( Map<Agent, Set<Agent>> agentFamilies ) throws LxcDestroyException
    {
        Map<Agent, Set<String>> families = new HashMap<>();

        for ( Map.Entry<Agent, Set<Agent>> entry : agentFamilies.entrySet() )
        {
            Agent physicalAgent = entry.getKey();
            if ( physicalAgent != null )
            {
                Set<Agent> lxcChildren = entry.getValue();
                Set<String> lxcHostnames = families.get( physicalAgent );
                if ( lxcHostnames == null )
                {
                    lxcHostnames = new HashSet<>();
                    families.put( physicalAgent, lxcHostnames );
                }

                for ( Agent lxcAgent : lxcChildren )
                {
                    if ( lxcAgent != null )
                    {
                        lxcHostnames.add( lxcAgent.getHostname() );
                    }
                }
            }
        }

        clonesDestroy( families );
    }


    private void clonesDestroy( Map<Agent, Set<String>> agentFamilies ) throws LxcDestroyException
    {
        if ( agentFamilies == null || agentFamilies.isEmpty() )
        {
            throw new LxcDestroyException( "Agent Families is null or empty" );
        }

        List<ContainerInfo> lxcInfos = new ArrayList<>();
        for ( Map.Entry<Agent, Set<String>> family : agentFamilies.entrySet() )
        {
            Agent physicalAgent = family.getKey();
            if ( physicalAgent != null )
            {

                ContainerInfo lxcInfo = new ContainerInfo( physicalAgent, family.getValue() );
                lxcInfos.add( lxcInfo );
            }
        }

        if ( !lxcInfos.isEmpty() )
        {

            CompletionService<ContainerInfo> completer = new ExecutorCompletionService<>( executor );
            //launch destroy commands
            for ( ContainerInfo lxcInfo : lxcInfos )
            {
                completer.submit( new ContainerActor( lxcInfo, this, ContainerAction.DESTROY ) );
            }

            //wait for completion
            try
            {
                for ( ContainerInfo ignored : lxcInfos )
                {
                    Future<ContainerInfo> future = completer.take();
                    future.get();
                }
            }
            catch ( InterruptedException | ExecutionException ignore )
            {
                LOGGER.info( "ContainerManagerImpl@clonesDestroy: " + ignore.getMessage(), ignore );
            }

            boolean result = true;
            for ( ContainerInfo lxcInfo : lxcInfos )
            {
                result &= lxcInfo.isResult();
            }

            if ( !result )
            {
                throw new LxcDestroyException( "Not all lxcs destroyed. Use LXC module to cleanup" );
            }
        }
        else
        {
            throw new LxcDestroyException( "Empty child lxcs provided" );
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
            String name = templateName + i.incrementAndGet();
            if ( !existingNames.contains( name ) )
            {
                return name;
            }
        }
    }


    private Set<String> getContainerNames( Collection<Agent> hostsToCheck )
    {
        Map<String, EnumMap<LxcState, List<String>>> map = lxcManager.getLxcOnPhysicalServers();

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
        for ( Map<LxcState, List<String>> lxcsOnOneHost : map.values() )
        {
            for ( List<String> hosts : lxcsOnOneHost.values() )
            {
                lxcHostNames.addAll( hosts );
            }
        }
        return lxcHostNames;
    }


    private void saveNodeGroup( UUID envId, String templateName, Set<Agent> agents, PlacementStrategy... strategy )
    {

        String cql = "INSERT INTO nodes(uuid, env_id, info) VALUES(?, ?, ?)";

        NodeInfo group = new NodeInfo();
        group.setEnvId( envId );
        group.setTemplateName( templateName );
        if ( strategy == null || strategy.length == 0 )
        {
            strategy = new PlacementStrategy[] {
                    PlacementStrategyFactory.getDefaultStrategyType()
            };
        }
        group.setStrategy( EnumSet.of( strategy[0], strategy ) );
        Template template = templateRegistry.getTemplate( templateName );
        group.setProducts( template.getProducts() );
        for ( Agent a : agents )
        {
            group.setInstanceId( a.getUuid() );
            dbManager.executeUpdate( cql, a.getUuid().toString(), envId.toString(), GSON.toJson( group ) );
        }
    }
}
