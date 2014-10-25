package org.safehaus.subutai.core.peer.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;


/**
 * Local peer implementation
 */
public class LocalPeerImpl extends Peer implements LocalPeer
{
    private static final int MAX_LXC_NAME = 15;
    private PeerManager peerManager;
    private ContainerManager containerManager;
    private TemplateRegistry templateRegistry;
    private ConcurrentMap<String, AtomicInteger> sequences = new ConcurrentHashMap<>();


    public LocalPeerImpl( PeerManager peerManager, ContainerManager containerManager,
                          TemplateRegistry templateRegistry )
    {
        this.peerManager = peerManager;
        this.containerManager = containerManager;
        this.templateRegistry = templateRegistry;
    }

    //
    //    private PeerManager getPeerManager() throws PeerException
    //    {
    //        PeerManager peerManager = null;
    //        try
    //        {
    //            ServiceLocator.getServiceNoCache( PeerManager.class );
    //        }
    //        catch ( NamingException e )
    //        {
    //            throw new PeerException( "Could not locate PeerManager" );
    //        }
    //        return peerManager;
    //    }


    @Override
    public UUID getOwnerId()
    {
        return null;
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID ownerPeerId, final UUID environmentId,
                                                final List<Template> templates, final int quantity,
                                                final String strategyId, final List<Criteria> criteria )
            throws ContainerCreateException
    {
        Set<ContainerHost> result = new HashSet<>();
        try
        {
            for ( Template t : templates )
            {
                if ( t.isRemote() )
                {
                    tryToRegister( t );
                }
            }
            String templateName = templates.get( templates.size() - 1 ).getTemplateName();
            Set<Agent> agents = containerManager.clone( environmentId, templateName, quantity, strategyId, criteria );


            for ( Agent agent : agents )
            {
                ResourceHost resourceHost = getResourceHostByName( agent.getParentHostName() );
                ContainerHostImpl containerHost = new ContainerHostImpl( agent );
                containerHost.setParentAgent( resourceHost.getAgent() );
                containerHost.setOwnerPeerId( ownerPeerId );
                containerHost.setTemplateName( templateName );
                resourceHost.addContainerHost( containerHost );
                result.add( containerHost );
            }
        }
        catch ( PeerException | RegistryException e )
        {
            throw new ContainerCreateException( e.toString() );
        }
        return result;
    }


    private void tryToRegister( final Template template ) throws RegistryException
    {
        if ( templateRegistry.getTemplate( template.getTemplateName() ) == null )
        {
            templateRegistry.registerTemplate( template );
        }
    }


    private String nextHostName( String templateName ) throws PeerException
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

            if ( getContainerHostByName( name ) == null )
            {
                return name;
            }
        }
    }


    @Override
    public ContainerHost getContainerHostByName( String hostname ) throws PeerException
    {
        ContainerHost result = null;
        Iterator<ResourceHost> iterator = getResourceHosts().iterator();
        while ( result == null && iterator.hasNext() )
        {
            result = iterator.next().getContainerHostByName( hostname );
        }
        return result;
    }


    @Override
    public ResourceHost getResourceHostByName( String hostname ) throws PeerException
    {
        return getManagementHost().getResourceHostByName( hostname );
    }


    private Map<Agent, ServerMetric> getResourceHostsMetrics() throws PeerException, CommandException
    {
        Map<Agent, ServerMetric> result = new HashMap();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            ServerMetric metrics = resourceHost.getMetric();
            if ( metrics != null )
            {
                result.put( resourceHost.getAgent(), metrics );
            }
        }
        return result;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId ) throws PeerException
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByEnvironmentId( environmentId ) );
        }
        return result;
    }


    private Host findHostByName( String hostname ) throws PeerException
    {
        Host result = null;
        ManagementHost managementHost = getManagementHost();
        if ( managementHost.getHostname().equals( hostname ) )
        {
            result = managementHost;
        }
        else
        {
            Iterator<ResourceHost> iterator = managementHost.getResourceHosts().iterator();
            while ( result == null && iterator.hasNext() )
            {
                ResourceHost rh = iterator.next();
                if ( rh.getHostname().equals( hostname ) )
                {
                    result = rh;
                }
                else
                {
                    result = rh.getContainerHostByName( hostname );
                }
            }
        }
        return result;
    }


    @Override
    public void startContainer( final ContainerHost containerHost ) throws PeerException, CommandException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        resourceHost.startContainerHost( containerHost );
    }


    @Override
    public void stopContainer( final ContainerHost containerHost ) throws PeerException, CommandException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        resourceHost.stopContainerHost( containerHost );
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {

    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        Host result = findHostByName( host.getParentHostname() );
        if ( result == null )
        {
            throw new PeerException( "Parent Host not found." );
        }
        return result.isConnected( host );
    }


    @Override
    public ManagementHost getManagementHost() throws PeerException
    {
        return peerManager.getManagementHost();
    }


    @Override
    public Set<ResourceHost> getResourceHosts() throws PeerException
    {
        return getManagementHost().getResourceHosts();
    }
}
