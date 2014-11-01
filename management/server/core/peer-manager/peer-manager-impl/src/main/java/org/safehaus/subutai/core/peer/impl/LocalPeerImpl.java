package org.safehaus.subutai.core.peer.impl;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.CommandStatus;
import org.safehaus.subutai.common.protocol.NullAgent;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.container.api.ContainerState;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.Criteria;

import com.google.common.collect.Sets;


/**
 * Local peer implementation
 */
public class LocalPeerImpl implements LocalPeer, ResponseListener
{
    private static final String SOURCE_MANAGEMENT = "MANAGEMENT_HOST";
    private static final long HOST_INACTIVE_TIME = 5 * 1000 * 60; // 5 min
    private PeerManager peerManager;
    private ContainerManager containerManager;
    private TemplateRegistry templateRegistry;
    private CommunicationManager communicationManager;
    private PeerDAO peerDAO;
    private ManagementHost managementHost;
    private CommandRunner commandRunner;
    private QuotaManager quotaManager;


    public LocalPeerImpl( PeerManager peerManager, ContainerManager containerManager, TemplateRegistry templateRegistry,
                          PeerDAO peerDao, CommunicationManager communicationManager, CommandRunner commandRunner,
                          QuotaManager quotaManager )
    {

        this.peerManager = peerManager;
        this.containerManager = containerManager;
        this.templateRegistry = templateRegistry;
        this.peerDAO = peerDao;
        this.communicationManager = communicationManager;
        this.commandRunner = commandRunner;
        this.quotaManager = quotaManager;
    }


    @Override
    public void init()
    {
        List<ManagementHost> result = peerDAO.getInfo( SOURCE_MANAGEMENT, ManagementHost.class );
        if ( result.size() > 0 )
        {
            managementHost = result.get( 0 );
            managementHost.resetHeartbeat();
            for ( ResourceHost resourceHost : managementHost.getResourceHosts() )
            {
                resourceHost.resetHeartbeat();
            }
        }
        communicationManager.addListener( this );
    }


    @Override
    public void shutdown()
    {
        communicationManager.removeListener( this );
    }


    @Override
    public UUID getId()
    {

        return peerManager.getLocalPeerInfo().getId();
    }


    @Override
    public String getName()
    {
        return peerManager.getLocalPeerInfo().getName();
    }


    @Override
    public UUID getOwnerId()
    {
        return peerManager.getLocalPeerInfo().getOwnerId();
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerManager.getLocalPeerInfo();
    }


    @Override
    public ContainerHost createContainer( final String hostName, final String templateName, final String cloneName,
                                          final UUID envId ) throws ContainerCreateException
    {
        try
        {
            Set<Agent> agents = containerManager
                    .clone( envId, getResourceHostByName( hostName ).getAgent(), templateName,
                            Sets.newHashSet( cloneName ) );

            if ( agents.size() == 1 )
            {
                Agent agent = agents.iterator().next();
                ResourceHost resourceHost = getResourceHostByName( agent.getParentHostName() );
                ContainerHost containerHost = new ContainerHost( agent, getId(), envId );
                containerHost.setParentAgent( resourceHost.getAgent() );
                containerHost.setCreatorPeerId( getId() );
                containerHost.setTemplateName( templateName );
                containerHost.updateHeartbeat();
                resourceHost.addContainerHost( containerHost );
                peerDAO.saveInfo( SOURCE_MANAGEMENT, managementHost.getId().toString(), managementHost );
                return containerHost;
            }
            else
            {
                throw new ContainerCreateException( "There are more than one created containers." );
            }
        }
        catch ( PeerException e )
        {
            throw new ContainerCreateException( e.toString() );
        }
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
                ContainerHost containerHost = new ContainerHost( agent, getId(), environmentId );
                containerHost.setParentAgent( resourceHost.getAgent() );
                containerHost.setCreatorPeerId( creatorPeerId );
                containerHost.setTemplateName( templateName );
                containerHost.updateHeartbeat();
                resourceHost.addContainerHost( containerHost );
                result.add( containerHost );
                peerDAO.saveInfo( SOURCE_MANAGEMENT, managementHost.getId().toString(), managementHost );
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


    private Host findHostById( UUID id ) throws PeerException
    {
        Host result = null;
        ManagementHost managementHost = getManagementHost();
        if ( managementHost.getId().equals( id ) )
        {
            result = managementHost;
        }
        else
        {
            Iterator<ResourceHost> iterator = managementHost.getResourceHosts().iterator();
            while ( result == null && iterator.hasNext() )
            {
                ResourceHost rh = iterator.next();
                if ( rh.getId().equals( id ) )
                {
                    result = rh;
                }
                else
                {
                    result = rh.getContainerHostById( id );
                }
            }
        }
        return result;
    }


    @Override
    public void startContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        try
        {
            if ( resourceHost.startContainerHost( containerHost ) )
            {
                containerHost.setState( ContainerState.RUNNING );
            }
        }
        catch ( CommandException e )
        {
            containerHost.setState( ContainerState.UNKNOWN );
            throw new PeerException( String.format( "Could not start LXC container [%s]", e.toString() ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public void stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        try
        {
            if ( resourceHost.stopContainerHost( containerHost ) )
            {
                containerHost.setState( ContainerState.STOPPED );
            }
        }
        catch ( CommandException e )
        {
            containerHost.setState( ContainerState.UNKNOWN );
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        Host result = getContainerHostByName( containerHost.getHostname() );
        if ( result == null )
        {
            throw new PeerException( "Container Host not found." );
        }

        try
        {
            containerManager.destroy( containerHost.getAgent().getParentHostName(), containerHost.getHostname() );
            ResourceHost resourceHost = getResourceHostByName( containerHost.getAgent().getParentHostName() );
            resourceHost.removeContainerHost( result );
            peerDAO.saveInfo( SOURCE_MANAGEMENT, managementHost.getId().toString(), managementHost );
        }
        catch ( ContainerDestroyException e )
        {
            throw new PeerException( e.toString() );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        Host result = findHostById( host.getId() );
        if ( result == null )
        {
            throw new PeerException( "Parent Host not found." );
        }
        if ( result instanceof ContainerHost )
        {
            return ContainerState.RUNNING.equals( ( ( ContainerHost ) result ).getState() ) && checkHeartbeat(
                    result.getLastHeartbeat() );
        }
        else
        {
            return checkHeartbeat( result.getLastHeartbeat() );
        }
    }


    @Override
    public String getQuota( ContainerHost host, final QuotaEnum quota ) throws PeerException
    {
        try
        {
            return quotaManager.getQuota( host.getHostname(), quota, host.getParentAgent() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e.toString() );
        }
    }


    @Override
    public void setQuota( ContainerHost host, final QuotaEnum quota, final String value ) throws PeerException
    {
        try
        {
            quotaManager.setQuota( host.getHostname(), quota, value, host.getParentAgent() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e.toString() );
        }
    }


    private boolean checkHeartbeat( long lastHeartbeat )
    {
        return ( System.currentTimeMillis() - lastHeartbeat ) < HOST_INACTIVE_TIME;
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
    public List<String> getTemplates()
    {
        List<Template> templates = templateRegistry.getAllTemplates();

        List<String> result = new ArrayList<>();
        for ( Template template : templates )
        {
            result.add( template.getTemplateName() );
        }
        return result;
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
                    managementHost = new ManagementHost( PeerUtils.buildAgent( response ), getId() );
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
                    host = new ResourceHost( PeerUtils.buildAgent( response ), getId() );
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

            if ( containerHost != null )
            {
                containerHost.updateHeartbeat();
                peerDAO.saveInfo( SOURCE_MANAGEMENT, managementHost.getId().toString(), managementHost );
            }
        }
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        return execute( requestBuilder, host, null );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {
        if ( !host.isConnected() )
        {
            throw new CommandException( "Host disconnected." );
        }
        Agent agent = host.getAgent();
        Command command = commandRunner.createCommand( requestBuilder, Sets.newHashSet( agent ) );
        command.execute( new org.safehaus.subutai.core.command.api.command.CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final AgentResult agentResult, final Command command )
            {
                if ( callback != null )
                {
                    callback.onResponse( response,
                            new CommandResult( agentResult.getExitCode(), agentResult.getStdOut(),
                                    agentResult.getStdErr(), command.getCommandStatus() ) );
                }
            }
        } );

        AgentResult agentResult = command.getResults().get( agent.getUuid() );

        if ( agentResult != null )
        {
            return new CommandResult( agentResult.getExitCode(), agentResult.getStdOut(), agentResult.getStdErr(),
                    command.getCommandStatus() );
        }
        else
        {
            return new CommandResult( null, null, null, CommandStatus.TIMEOUT );
        }
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {
        if ( !host.isConnected() )
        {
            throw new CommandException( "Host disconnected." );
        }
        final Agent agent = host.getAgent();
        Command command = commandRunner.createCommand( requestBuilder, Sets.newHashSet( agent ) );
        command.executeAsync( new org.safehaus.subutai.core.command.api.command.CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final AgentResult agentResult, final Command command )
            {
                if ( callback != null )
                {
                    callback.onResponse( response,
                            new CommandResult( agentResult.getExitCode(), agentResult.getStdOut(),
                                    agentResult.getStdErr(), command.getCommandStatus() ) );
                }
            }
        } );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        executeAsync( requestBuilder, host, null );
    }


    @Override
    public boolean isLocal()
    {
        return true;
    }


    @Override
    public void clean()
    {
        if ( managementHost == null || managementHost.getId() == null )
        {
            return;
        }
        UUID id = managementHost.getId();
        peerDAO.deleteInfo( SOURCE_MANAGEMENT, id.toString() );
        managementHost = null;
        peerDAO.deleteInfo( SOURCE_MANAGEMENT, id.toString() );
    }
}

