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

import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.NullAgent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.container.api.ContainerState;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Local peer implementation
 */
public class LocalPeerImpl extends Peer implements LocalPeer, ResponseListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String SOURCE_MANAGEMENT = "MANAGEMENT_HOST";
    private static final String SOURCE_RESOURCE = "RESOURCE_HOST";
    private static final String SOURCE_CONTAINER = "CONTAINER_HOST";
    private static final int MAX_LXC_NAME = 15;
    private PeerManager peerManager;
    private ContainerManager containerManager;
    private TemplateRegistry templateRegistry;
    private CommunicationManager communicationManager;
    private PeerDAO peerDAO;
    private ConcurrentMap<String, AtomicInteger> sequences = new ConcurrentHashMap<>();
    private ManagementHost managementHost;


    public LocalPeerImpl( PeerManager peerManager, ContainerManager containerManager, TemplateRegistry templateRegistry,
                          PeerDAO peerDao, CommunicationManager communicationManager )
    {
        this.peerManager = peerManager;
        this.containerManager = containerManager;
        this.templateRegistry = templateRegistry;
        this.peerDAO = peerDao;
        this.communicationManager = communicationManager;
    }


    @Override
    public UUID getOwnerId()
    {
        return null;
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID creatorPeerId, final UUID environmentId,
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
                ContainerHost containerHost = new ContainerHost( agent );
                containerHost.setParentAgent( resourceHost.getAgent() );
                containerHost.setCreatorPeerId( creatorPeerId );
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
    public boolean startContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        try
        {
            if ( resourceHost.startContainerHost( containerHost ) )
            {
                containerHost.setState( ContainerState.RUNNING );
            }
            return true;
        }
        catch ( CommandException e )
        {
            containerHost.setState( ContainerState.UNKNOWN );
            throw new PeerException( String.format( "Could not start LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public boolean stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        try
        {
            if ( resourceHost.stopContainerHost( containerHost ) )
            {
                containerHost.setState( ContainerState.STOPPED );
            }
            return true;
        }
        catch ( CommandException e )
        {
            containerHost.setState( ContainerState.UNKNOWN );
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {

    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        Host result = findHostByName( host.getHostname() );
        if ( result == null )
        {
            throw new PeerException( "Parent Host not found." );
        }
        return result.isConnected();
    }


    @Override
    public ManagementHost getManagementHost() throws PeerException
    {
        return managementHost;
    }


    @Override
    public Set<ResourceHost> getResourceHosts() throws PeerException
    {
        return getManagementHost().getResourceHosts();
    }


    @Override
    public void onResponse( final Response response )
    {
        if ( response == null || response.getType() == null )
        {
            return;
        }

        if ( response.getType().equals( ResponseType.REGISTRATION_REQUEST ) || response.getType().equals(
                ResponseType.HEARTBEAT_RESPONSE ) )
        {
            if ( response.getHostname().equals( "management" ) )
            {
                if ( managementHost == null )
                {
                    managementHost = new ManagementHost( PeerUtils.buildAgent( response ) );
                    managementHost.setParentAgent( NullAgent.getInstance() );
                }
                managementHost.updateHeartbeat();
                peerDAO.saveInfo( SOURCE_MANAGEMENT, managementHost.getId().toString(), managementHost );
                return;
            }

            if ( managementHost == null )
            {
                return;
            }

            if ( response.getHostname().startsWith( "py" ) )
            {
                ResourceHost host = managementHost.getResourceHostByName( response.getHostname() );
                if ( host == null )
                {
                    host = new ResourceHost( PeerUtils.buildAgent( response ) );
                    host.setParentAgent( managementHost.getAgent() );
                    managementHost.addResourceHost( host );
                }
                host.updateHeartbeat();
                peerDAO.saveInfo( SOURCE_MANAGEMENT, managementHost.getId().toString(), managementHost );
                return;
            }

            ResourceHost resourceHost = managementHost.getResourceHostByName( response.getParentHostName() );
            if ( resourceHost == null )
            {
                return;
            }

            ContainerHost containerHost = resourceHost.getContainerHostByName( response.getHostname() );

            if ( containerHost == null )
            {
                containerHost = new ContainerHost( PeerUtils.buildAgent( response ) );
                containerHost.setParentAgent( resourceHost.getAgent() );
                resourceHost.addContainerHost( containerHost );
            }
            containerHost.updateHeartbeat();
            containerHost.setState( ContainerState.RUNNING );
            peerDAO.saveInfo( SOURCE_MANAGEMENT, managementHost.getId().toString(), managementHost );
        }
    }


    @Override
    public void init()
    {

        List<ManagementHost> result = peerDAO.getInfo( SOURCE_MANAGEMENT, ManagementHost.class );
        if ( result.size() > 0 )
        {
            managementHost = result.get( 0 );
        }
        communicationManager.addListener( this );
    }


    @Override
    public void shutdown()
    {
        communicationManager.removeListener( this );
    }
}
