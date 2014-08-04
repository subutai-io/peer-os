package org.safehaus.subutai.impl.container;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.lxcmanager.LxcState;
import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.api.manager.helper.PlacementStrategy;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.impl.strategy.PlacementStrategyFactory;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.settings.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ContainerManagerImpl extends ContainerManagerBase {

    private static final Logger logger = LoggerFactory.getLogger( ContainerManager.class );
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    // number sequences for template names used for new clone name generation
    private ConcurrentMap<String, AtomicInteger> sequences;
    private ExecutorService executor;


    public void init() {
        sequences = new ConcurrentHashMap<>();
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        sequences.clear();
    }


    @Override
    public Set<Agent> clone( UUID envId, String templateName, int nodesCount, Collection<Agent> hosts,
                             PlacementStrategy... strategy ) throws LxcCreateException {

        // restrict metrics to provided hosts only
        Map<Agent, ServerMetric> metrics = lxcManager.getPhysicalServerMetrics();
        if ( hosts != null && !hosts.isEmpty() ) {
            Iterator<Agent> it = metrics.keySet().iterator();
            while ( it.hasNext() ) {
                if ( !hosts.contains( it.next() ) ) {
                    it.remove();
                }
            }
        }

        LxcPlacementStrategy st = PlacementStrategyFactory.create( nodesCount, strategy );
        st.calculatePlacement( metrics );
        Map<Agent, Integer> slots = st.getPlacementDistribution();

        int totalSlots = 0;

        for ( int slotCount : slots.values() ) {
            totalSlots += slotCount;
        }

        if ( totalSlots == 0 ) {
            throw new LxcCreateException( "Lxc placement strategy returned empty set" );
        }

        if ( totalSlots < nodesCount ) {
            throw new LxcCreateException( String.format( "Only %d containers can be created", totalSlots ) );
        }


        // clone specified number of instances and store their names
        List<String> cloneNames = new ArrayList<>();
        for ( Map.Entry<Agent, Integer> e : slots.entrySet() ) {
            Set<String> existingNames = getContainerNames( e.getKey().getHostname() );
            for ( int i = 0; i < e.getValue(); i++ ) {
                String name = nextHostName( templateName, existingNames );
                boolean b = templateManager.clone( e.getKey().getHostname(), templateName, name );
                if ( b ) {
                    cloneNames.add( name );
                }
            }
        }

        if ( cloneNames.size() < nodesCount ) {
            throw new LxcCreateException( "Not all containers created successfully. Use LXC module to cleanup" );
        }

        boolean result = true;
        long waitStart = System.currentTimeMillis();
        Set<Agent> clones = new HashSet<>();
        while ( !Thread.interrupted() ) {
            result = true;
            for ( String cloneName : cloneNames ) {
                Agent lxcAgent = agentManager.getAgentByHostname( cloneName );
                if ( lxcAgent == null ) {
                    result = false;
                    break;
                }
                else {
                    clones.add( lxcAgent );
                }
            }
            if ( result ) {
                break;
            }
            else {
                if ( System.currentTimeMillis() - waitStart > Common.LXC_AGENT_WAIT_TIMEOUT_SEC * 1000 ) {
                    break;
                }
                else {
                    try {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException ex ) {
                        break;
                    }
                }
            }
        }

        if ( !result ) {
            throw new LxcCreateException( "Waiting interval for lxc agents timed out. Use LXC module to cleanup" );
        }

        try {
            if ( envId != null ) {
                saveNodeGroup( envId, templateName, clones, strategy );
            }
        }
        catch ( Exception ex ) {
            logger.error( "Failed to save nodes info", ex );
        }

        return clones;
    }


    @Override
    public Set<Agent> clone( String templateName, int nodesCount, Collection<Agent> hosts,
                             PlacementStrategy... strategy ) throws LxcCreateException {
        return clone( null, templateName, nodesCount, hosts, strategy );
    }


    @Override
    public boolean attachAndExecute( Agent physicalHost, String cloneName, String cmd ) {
        return attachAndExecute( physicalHost, cloneName, cmd, 30, TimeUnit.SECONDS );
    }


    @Override
    public boolean attachAndExecute( Agent physicalHost, String cloneName, String cmd, long t, TimeUnit unit ) {
        if ( cmd == null || cmd.isEmpty() ) {
            return false;
        }
        // synopsis:
        // lxc-attach {-n name} [-a arch] [-e] [-s namespaces] [-R] [--keep-env] [--clear-env] [-- command]
        StringBuilder sb = new StringBuilder( "lxc-attach -n " );
        sb.append( cloneName ).append( " -- " ).append( cmd );

        int timeout = ( int ) unit.toSeconds( t );
        Command comm = commandRunner.createCommand( new RequestBuilder( sb.toString() ).withTimeout( timeout ),
                new HashSet<>( Arrays.asList( physicalHost ) ) );
        commandRunner.runCommand( comm );
        return comm.hasSucceeded();
    }


    @Override
    public void cloneDestroy( final String hostName, final String cloneName ) throws LxcDestroyException {
        boolean result = templateManager.cloneDestroy( hostName, cloneName );
        if ( !result ) {
            throw new LxcDestroyException( String.format( "Error destroying container %s", cloneName ) );
        }
    }


    @Override
    public void cloneDestroyByHostname( final Set<String> cloneNames ) throws LxcDestroyException {
        if ( cloneNames == null || cloneNames.isEmpty() ) {
            throw new LxcDestroyException( "Clone names is empty or null" );
        }

        Set<Agent> lxcAgents = new HashSet<>();
        for ( String lxcHostname : cloneNames ) {
            if ( lxcHostname != null ) {
                Agent lxcAgent = agentManager.getAgentByHostname( lxcHostname );
                if ( lxcAgent == null ) {
                    throw new LxcDestroyException( String.format( "Lxc %s is not connected", lxcHostname ) );
                }
                lxcAgents.add( lxcAgent );
            }
        }

        cloneDestroy( lxcAgents );
    }


    public void cloneDestroy( Set<Agent> lxcAgents ) throws LxcDestroyException {
        if ( lxcAgents == null || lxcAgents.isEmpty() ) {
            throw new LxcDestroyException( "LxcAgents is null or empty" );
        }

        Map<Agent, Set<Agent>> families = new HashMap<>();
        for ( Agent lxcAgent : lxcAgents ) {
            if ( lxcAgent != null ) {
                Agent parentAgent = agentManager.getAgentByHostname( lxcAgent.getParentHostName() );
                if ( parentAgent == null ) {
                    throw new LxcDestroyException(
                            String.format( "Physical parent of %s is not connected", lxcAgent.getHostname() ) );
                }
                Set<Agent> lxcChildren = families.get( parentAgent );
                if ( lxcChildren == null ) {
                    lxcChildren = new HashSet<>();
                    families.put( parentAgent, lxcChildren );
                }
                lxcChildren.add( lxcAgent );
            }
        }

        cloneDestroy( families );
    }


    public void cloneDestroy( Map<Agent, Set<Agent>> agentFamilies ) throws LxcDestroyException {
        Map<Agent, Set<String>> families = new HashMap<>();

        for ( Map.Entry<Agent, Set<Agent>> entry : agentFamilies.entrySet() ) {
            Agent physicalAgent = entry.getKey();
            if ( physicalAgent != null ) {
                Set<Agent> lxcChildren = entry.getValue();
                Set<String> lxcHostnames = families.get( physicalAgent );
                if ( lxcHostnames == null ) {
                    lxcHostnames = new HashSet<>();
                    families.put( physicalAgent, lxcHostnames );
                }

                for ( Agent lxcAgent : lxcChildren ) {
                    if ( lxcAgent != null ) {
                        lxcHostnames.add( lxcAgent.getHostname() );
                    }
                }
            }
        }

        cloneDestroyByHostname( families );
    }


    //@todo use command chaining on the same physical server for parallel node destruction instead of multithreaded
    // approach
    //
    //use parallel thread only for multiple physical servers
    public void cloneDestroyByHostname( Map<Agent, Set<String>> agentFamilies ) throws LxcDestroyException {
        if ( agentFamilies == null || agentFamilies.isEmpty() ) {
            throw new LxcDestroyException( "AgentFamilies is null or empty" );
        }

        List<ContainerInfo> lxcInfos = new ArrayList<>();
        for ( Map.Entry<Agent, Set<String>> family : agentFamilies.entrySet() ) {
            Agent physicalAgent = family.getKey();
            if ( physicalAgent != null ) {
                Set<String> children = family.getValue();

                for ( String lxcAgentHostname : children ) {
                    if ( lxcAgentHostname != null ) {
                        ContainerInfo lxcInfo = new ContainerInfo( physicalAgent, lxcAgentHostname );
                        lxcInfos.add( lxcInfo );
                    }
                }
            }
        }

        if ( !lxcInfos.isEmpty() ) {

            CompletionService<ContainerInfo> completer = new ExecutorCompletionService<>( executor );
            //launch destroy commands
            for ( ContainerInfo lxcInfo : lxcInfos ) {
                completer.submit( new ContainerActor( lxcInfo, this, ContainerAction.DESTROY ) );
            }

            //wait for completion
            try {
                for ( ContainerInfo ignored : lxcInfos ) {
                    Future<ContainerInfo> future = completer.take();
                    future.get();
                }
            }
            catch ( InterruptedException | ExecutionException e ) {
            }

            boolean result = true;
            for ( ContainerInfo lxcInfo : lxcInfos ) {
                result &= lxcInfo.isResult();
            }

            if ( !result ) {
                throw new LxcDestroyException( "Not all lxcs destroyed. Use LXC module to cleanup" );
            }
        }
        else {
            throw new LxcDestroyException( "Empty child lxcs provided" );
        }
    }


    private String nextHostName( String templateName, Set<String> existingNames ) {
        AtomicInteger i = sequences.putIfAbsent( templateName, new AtomicInteger() );
        if ( i == null ) {
            i = sequences.get( templateName );
        }
        while ( true ) {
            String name = templateName + i.incrementAndGet();
            if ( !existingNames.contains( name ) ) {
                return name;
            }
        }
    }


    private Set<String> getContainerNames( String hostname ) {
        Map<String, EnumMap<LxcState, List<String>>> map = lxcManager.getLxcOnPhysicalServers();
        EnumMap<LxcState, List<String>> lxcs = map.get( hostname );
        if ( lxcs == null ) {
            return Collections.emptySet();
        }

        Set<String> res = new HashSet<>();
        for ( List<String> ls : lxcs.values() ) {
            res.addAll( ls );
        }
        return res;
    }


    private void saveNodeGroup( UUID envId, String templateName, Set<Agent> agents, PlacementStrategy... strategy ) {

        String cql = "INSERT INTO nodes(uuid, env_id, info) VALUES(?, ?, ?)";

        NodeInfo group = new NodeInfo();
        group.setEnvId( envId );
        group.setTemplateName( templateName );
        if ( strategy == null || strategy.length == 0 ) {
            strategy = new PlacementStrategy[] {
                    PlacementStrategyFactory.getDefaultStrategyType()
            };
        }
        group.setStrategy( EnumSet.of( strategy[0], strategy ) );
        Template template = templateRegistry.getTemplate( templateName );
        group.setProducts( template.getProducts() );
        for ( Agent a : agents ) {
            group.setInstanceId( a.getUuid() );
            dbManager.executeUpdate( cql, a.getUuid().toString(), envId.toString(), gson.toJson( group ) );
        }
    }
}
