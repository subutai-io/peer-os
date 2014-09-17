/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.agent.impl;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.agent.api.AgentListener;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;


/**
 * Implementation of Agent Manager Interface
 */
public class AgentManagerImpl implements ResponseListener, AgentManager {

    private static final Logger LOG = Logger.getLogger( AgentManagerImpl.class.getName() );
    /**
     * list of agent listeners
     */
    private final Queue<AgentListener> listeners = new ConcurrentLinkedQueue<>();
    /**
     * reference to communication manager
     */
    private final CommunicationManager communicationService;
    private final PeerManager peerManager;
    /**
     * executor for notifying agent listeners
     */
    private ExecutorService exec;
    /**
     * cache of currently connected agents with expiry ttl. Agents will expire unless they send heartbeat message
     * regularly
     */
    private Cache<UUID, Agent> agents;

    private volatile boolean notifyAgentListeners = true;


    public AgentManagerImpl( final CommunicationManager communicationService, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( communicationService, "Communication Manager is null" );
        Preconditions.checkNotNull( peerManager, "Peer Manager is null" );

        this.communicationService = communicationService;
        this.peerManager = peerManager;
    }


    public Collection<AgentListener> getListeners()
    {
        return Collections.unmodifiableCollection( listeners );
    }


    /**
     * Returns all agents currently connected to the mgmt server.
     *
     * @return set of all agents connected to the mgmt server.
     */
    public Set<Agent> getAgents()
    {
        return Sets.newHashSet( agents.asMap().values() );
    }


    /**
     * Returns all physical agents currently connected to the mgmt server.
     *
     * @return set of all physical agents currently connected to the mgmt server.
     */
    public Set<Agent> getPhysicalAgents()
    {
        Set<Agent> physicalAgents = new HashSet<>();
        for ( Agent agent : agents.asMap().values() )
        {
            if ( !agent.isLXC() )
            {
                physicalAgents.add( agent );
            }
        }
        return physicalAgents;
    }


    /**
     * Returns all lxc agents currently connected to the mgmt server.
     *
     * @return set of all lxc agents currently connected to the mgmt server.
     */
    public Set<Agent> getLxcAgents()
    {
        Set<Agent> lxcAgents = new HashSet<>();
        for ( Agent agent : agents.asMap().values() )
        {
            if ( agent.isLXC() )
            {
                lxcAgents.add( agent );
            }
        }
        return lxcAgents;
    }


    /**
     * Returns agent by its node's hostname or null if agent is not connected
     *
     * @param hostname - hostname of agent's node
     *
     * @return agent
     */
    public Agent getAgentByHostname( String hostname )
    {
        if ( !Strings.isNullOrEmpty( hostname ) )
        {
            for ( Agent agent : agents.asMap().values() )
            {
                if ( hostname.equalsIgnoreCase( agent.getHostname() ) )
                {
                    return agent;
                }
            }
        }
        return null;
    }


    /**
     * Returns agent by its UUID or null if agent is not connected
     *
     * @param uuid - UUID of agent
     *
     * @return agent
     */
    public Agent getAgentByUUID( UUID uuid )
    {
        return agents.getIfPresent( uuid );
    }


    /**
     * Returns agent by its physical parent node's hostname or null if agent is not connected
     *
     * @param parentHostname - hostname of agent's node physical parent node
     *
     * @return agent
     */
    public Set<Agent> getLxcAgentsByParentHostname( String parentHostname )
    {
        Set<Agent> lxcAgents = new HashSet<>();
        if ( !Strings.isNullOrEmpty( parentHostname ) )
        {
            for ( Agent agent : agents.asMap().values() )
            {
                if ( parentHostname.equalsIgnoreCase( agent.getParentHostName() ) )
                {
                    lxcAgents.add( agent );
                }
            }
        }
        return lxcAgents;
    }


    /**
     * Adds listener which wants to be notified when agents connect/disconnect
     *
     * @param listener - listener to add
     */
    @Override
    public void addListener( AgentListener listener )
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
            LOG.log( Level.SEVERE, "Error in addListener", ex );
        }
    }


    /**
     * Removes listener
     *
     * @param listener - - listener to remove
     */
    @Override
    public void removeListener( AgentListener listener )
    {
        try
        {
            listeners.remove( listener );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in removeListener", ex );
        }
    }


    @Override
    public Set<Agent> getAgentsByHostnames( final Set<String> hostnames )
    {
        Set<Agent> agentSet = new HashSet<>();
        if ( !CollectionUtil.isCollectionEmpty( hostnames ) )
        {
            for ( Agent agent : agents.asMap().values() )
            {
                if ( hostnames.contains( agent.getHostname() ) )
                {
                    agentSet.add( agent );
                }
            }
        }
        return agentSet;
    }


    @Override
    public Set<Agent> getAgentsByEnvironmentId( final UUID environmentId )
    {
        Set<Agent> agentSet = new HashSet<>();
        if ( environmentId != null )
        {
            for ( Agent agent : agents.asMap().values() )
            {
                if ( agent.getEnvironmentId() != null && environmentId.compareTo( agent.getEnvironmentId() ) == 0 )
                {
                    agentSet.add( agent );
                }
            }
        }
        return agentSet;
    }


    /**
     * Initialized agent manager
     */
    public void init()
    {
        try
        {

            Preconditions.checkNotNull( communicationService, "Communication service is null" );

            agents = CacheBuilder.newBuilder().
                    expireAfterWrite( Common.AGENT_FRESHNESS_MIN, TimeUnit.MINUTES ).
                                         build();

            communicationService.addListener( this );

            exec = Executors.newSingleThreadExecutor();
            exec.execute( new Runnable() {

                public void run()
                {
                    long lastNotify = System.currentTimeMillis();
                    while ( !Thread.interrupted() )
                    {
                        try
                        {
                            if ( notifyAgentListeners || System.currentTimeMillis() - lastNotify
                                    > Common.AGENT_FRESHNESS_MIN * 60 * 1000 / 2 )
                            {
                                lastNotify = System.currentTimeMillis();
                                notifyAgentListeners = false;
                                Set<Agent> freshAgents = Sets.newHashSet( agents.asMap().values() );
                                for ( Iterator<AgentListener> it = listeners.iterator(); it.hasNext(); )
                                {
                                    AgentListener listener = it.next();
                                    try
                                    {
                                        listener.onAgent( freshAgents );
                                    }
                                    catch ( Exception e )
                                    {
                                        it.remove();
                                        LOG.log( Level.SEVERE,
                                                "Error notifying agent listeners, removing faulting listener", e );
                                    }
                                }
                            }
                            Thread.sleep( 1000 );
                        }
                        catch ( InterruptedException ex )
                        {
                            break;
                        }
                    }
                }
            } );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in init", ex );
        }
    }


    /**
     * Disposes agent manager
     */
    public void destroy()
    {
        try
        {
            agents.invalidateAll();
            exec.shutdownNow();
            communicationService.removeListener( this );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in destroy", ex );
        }
    }


    /**
     * Communication manager event when response from agent arrives
     */
    public void onResponse( Response response )
    {
        if ( response != null && response.getType() != null )
        {
            switch ( response.getType() )
            {
                case REGISTRATION_REQUEST:
                {
                    addAgent( response );
                    break;
                }
                case HEARTBEAT_RESPONSE:
                {
                    addAgent( response );
                    break;
                }
                case AGENT_DISCONNECT:
                {
                    removeAgent( response );
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
    }


    /**
     * Adds agent to the cache of connected agents
     */
    private void addAgent( Response response )
    {
        try
        {
            if ( response != null && response.getUuid() != null )
            {
                Agent checkAgent = agents.getIfPresent( response.getUuid() );
                if ( checkAgent != null )
                {
                    //update timestamp of agent here & return
                    agents.put( response.getUuid(), checkAgent );
                    return;
                }
                //create agent from response
                Agent agent = new Agent( response.getUuid(),
                        Strings.isNullOrEmpty( response.getHostname() ) ? response.getUuid().toString() :
                        response.getHostname(), response.getParentHostName(), response.getMacAddress(),
                        response.getIps(), !Strings.isNullOrEmpty( response.getParentHostName() ),
                        response.getTransportId(), peerManager.getSiteId(), response.getEnvironmentId() );

                //send registration acknowledgement to agent
                sendAck( agent.getUuid() );
                //put agent to cache
                agents.put( response.getUuid(), agent );
                //notify listeners
                notifyAgentListeners = true;
            }
        }
        catch ( Exception e )
        {
            LOG.log( Level.SEVERE, "Error in addAgent", e );
        }
    }


    /**
     * Sends ack to agent when it is registered with the management server
     */
    private void sendAck( UUID agentUUID )
    {
        Request ack =
                new Request( "AGENT-MANAGER", RequestType.REGISTRATION_REQUEST_DONE, agentUUID, UUID.randomUUID(), null,
                        null, null, null, null, null, null, null, null, null, null, null );
        communicationService.sendRequest( ack );
    }


    /**
     * Removes agent from the cache of connected agents
     */
    private void removeAgent( Response response )
    {
        try
        {
            if ( response != null && response.getTransportId() != null )
            {
                for ( Agent agent : agents.asMap().values() )
                {
                    if ( agent.getTransportId().startsWith( response.getTransportId() ) )
                    {
                        agents.invalidate( agent.getUuid() );
                        notifyAgentListeners = true;
                        return;
                    }
                }
            }
        }
        catch ( Exception e )
        {
            LOG.log( Level.SEVERE, "Error in removeAgent", e );
        }
    }
}