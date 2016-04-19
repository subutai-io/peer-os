package io.subutai.core.localpeer.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.PermitAll;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.CloneContainerTask;
import io.subutai.common.environment.Containers;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.exception.DaoException;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.ReverseProxyConfig;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.resource.PeerResources;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.HostUtil;
import io.subutai.common.util.P2PUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.localpeer.impl.command.CommandRequestListener;
import io.subutai.core.localpeer.impl.container.CreateEnvironmentContainersRequestListener;
import io.subutai.core.localpeer.impl.container.ImportTemplateTask;
import io.subutai.core.localpeer.impl.container.PrepareTemplateRequestListener;
import io.subutai.core.localpeer.impl.container.SetQuotaTask;
import io.subutai.core.localpeer.impl.dao.NetworkResourceDaoImpl;
import io.subutai.core.localpeer.impl.dao.ResourceHostDataService;
import io.subutai.core.localpeer.impl.entity.AbstractSubutaiHost;
import io.subutai.core.localpeer.impl.entity.ContainerHostEntity;
import io.subutai.core.localpeer.impl.entity.NetworkResourceEntity;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;
import io.subutai.core.localpeer.impl.tasks.CleanupEnvironmentTask;
import io.subutai.core.localpeer.impl.tasks.JoinP2PSwarmTask;
import io.subutai.core.localpeer.impl.tasks.ResetP2PSwarmSecretTask;
import io.subutai.core.localpeer.impl.tasks.TunnelsTask;
import io.subutai.core.localpeer.impl.tasks.UsedHostNetResourcesTask;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.model.Relation;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationMeta;
import io.subutai.core.object.relation.api.model.RelationStatus;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * Local peer implementation
 */
@PermitAll
public class LocalPeerImpl implements LocalPeer, HostListener, Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );


    private DaoManager daoManager;
    private TemplateManager templateRegistry;
    protected Set<ResourceHost> resourceHosts = Sets.newConcurrentHashSet();
    private CommandExecutor commandExecutor;
    private QuotaManager quotaManager;
    private Monitor monitor;
    protected ResourceHostDataService resourceHostDataService;
    private HostRegistry hostRegistry;
    protected CommandUtil commandUtil = new CommandUtil();
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Set<RequestListener> requestListeners = Sets.newHashSet();
    protected PeerInfo peerInfo;
    private SecurityManager securityManager;
    protected ServiceLocator serviceLocator = new ServiceLocator();
    private IdentityManager identityManager;
    private RelationManager relationManager;

    protected volatile boolean initialized = false;
    private NetworkResourceDaoImpl networkResourceDao;
    LocalPeerCommands localPeerCommands = new LocalPeerCommands();
    HostUtil hostUtil = new HostUtil();


    public LocalPeerImpl( DaoManager daoManager, TemplateManager templateRegistry, QuotaManager quotaManager,
                          CommandExecutor commandExecutor, HostRegistry hostRegistry, Monitor monitor,
                          SecurityManager securityManager )
    {
        this.daoManager = daoManager;
        this.templateRegistry = templateRegistry;
        this.quotaManager = quotaManager;
        this.monitor = monitor;
        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
        this.securityManager = securityManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setRelationManager( final RelationManager relationManager )
    {
        this.relationManager = relationManager;
    }


    public void init()
    {
        LOG.debug( "********************************************** Initializing peer "
                + "******************************************" );
        try
        {
            initPeerInfo();

            //add command request listener
            addRequestListener( new CommandRequestListener() );
            //add command response listener

            //add create container requests listener
            addRequestListener( new CreateEnvironmentContainersRequestListener( this ) );

            //add prepare templates listener
            addRequestListener( new PrepareTemplateRequestListener( this ) );

            resourceHostDataService = createResourceHostDataService();

            resourceHosts.clear();

            for ( ResourceHost resourceHost : resourceHostDataService.getAll() )
            {
                resourceHosts.add( resourceHost );
            }

            setResourceHostTransientFields( getResourceHosts() );

            this.networkResourceDao = new NetworkResourceDaoImpl( daoManager.getEntityManagerFactory() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new LocalPeerInitializationError( "Failed to init Local Peer", e );
        }

        initialized = true;
    }


    protected void initPeerInfo()
    {
        peerInfo = new PeerInfo();
        peerInfo.setId( securityManager.getKeyManager().getPeerId() );
        peerInfo.setOwnerId( securityManager.getKeyManager().getPeerOwnerId() );
        peerInfo.setPublicUrl( SystemSettings.getPublicUrl() );
        peerInfo.setPublicSecurePort( SystemSettings.getPublicSecurePort() );
        peerInfo.setName( String.format( "Peer %s on %s", peerInfo.getId(), SystemSettings.getPublicUrl() ) );
    }


    @Override
    public void setPeerInfo( final PeerInfo peerInfo )
    {
        this.peerInfo.setId( peerInfo.getId() );
        this.peerInfo.setName( peerInfo.getName() );
        this.peerInfo.setOwnerId( peerInfo.getOwnerId() );
        this.peerInfo.setPublicUrl( peerInfo.getPublicUrl() );
        this.peerInfo.setPublicSecurePort( peerInfo.getPublicSecurePort() );
    }


    protected ResourceHostDataService createResourceHostDataService()
    {
        return new ResourceHostDataService( daoManager.getEntityManagerFactory() );
    }


    @Override
    public void dispose()
    {
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            ( ( Disposable ) resourceHost ).dispose();
        }

        hostUtil.dispose();
    }


    private void setResourceHostTransientFields( Set<ResourceHost> resourceHosts )
    {
        for ( ResourceHost resourceHost : resourceHosts )
        {
            ( ( AbstractSubutaiHost ) resourceHost ).setPeer( this );
        }
    }


    @Override
    public String getId()
    {
        return peerInfo.getId();
    }


    @Override
    public String getName()
    {
        return peerInfo.getName();
    }


    @Override
    public String getOwnerId()
    {
        return peerInfo.getOwnerId();
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    @Override
    public ContainerHostState getContainerState( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );

        try
        {
            ContainerHostInfo containerHostInfo =
                    ( ContainerHostInfo ) hostRegistry.getHostInfoById( containerId.getId() );
            return containerHostInfo.getState();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException( String.format( "Error getting container state: %s", e.getMessage() ), e );
        }
    }


    @Override
    public Containers getEnvironmentContainers( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        Containers result = new Containers();
        try
        {
            Set<ContainerHost> containers = findContainersByEnvironmentId( environmentId.getId() );

            for ( ContainerHost c : containers )
            {
                ContainerHostInfo info;
                try
                {
                    info = hostRegistry.getContainerHostInfoById( c.getId() );
                }
                catch ( HostDisconnectedException e )
                {
                    info = new ContainerHostInfoModel( c );
                }
                result.addContainer( info );
            }
            return result;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException( String.format( "Error getting environment containers: %s", e.getMessage() ), e );
        }
    }


    @Override
    public void configureHostsInEnvironment( final EnvironmentId environmentId, final HostAddresses hostAddresses )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkNotNull( hostAddresses, "Invalid HostAdresses" );
        Preconditions.checkArgument( !hostAddresses.isEmpty(), "No host addresses" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        CommandUtil.HostCommandResults results = commandUtil
                .executeParallel( localPeerCommands.getAddIpHostToEtcHostsCommand( hostAddresses.getHostAddresses() ),
                        hosts );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "Host registration failed on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( results.hasFailures() )
        {
            throw new PeerException( "Failed to register all hosts" );
        }
    }


    @Override
    public SshPublicKeys generateSshKeyForEnvironment( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );

        SshPublicKeys sshPublicKeys = new SshPublicKeys();

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return sshPublicKeys;
        }

        CommandUtil.HostCommandResults readResults =
                commandUtil.executeParallel( localPeerCommands.getReadOrCreateSSHCommand(), hosts );

        Set<Host> succeededHosts = Sets.newHashSet();
        Set<Host> failedHosts = Sets.newHashSet( hosts );

        for ( CommandUtil.HostCommandResult result : readResults.getCommandResults() )
        {
            if ( result.hasSucceeded() && !Strings.isNullOrEmpty( result.getCommandResult().getStdOut() ) )
            {
                sshPublicKeys.addSshPublicKey( result.getCommandResult().getStdOut() );

                succeededHosts.add( result.getHost() );
            }
            else
            {
                LOG.error( "Failed to generate ssh key on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        failedHosts.removeAll( succeededHosts );

        if ( !failedHosts.isEmpty() )
        {
            throw new PeerException( "Failed to generate ssh keys on all hosts" );
        }

        return sshPublicKeys;
    }


    @Override
    public void configureSshInEnvironment( final EnvironmentId environmentId, final SshPublicKeys sshPublicKeys )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkNotNull( sshPublicKeys, "SshPublicKey is null" );
        Preconditions.checkArgument( !sshPublicKeys.isEmpty(), "No ssh keys" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        //add keys in portions, since all can not fit into one command, it fails
        int portionSize = 100;
        int i = 0;
        StringBuilder keysString = new StringBuilder();
        Set<String> keys = sshPublicKeys.getSshPublicKeys();

        for ( String key : keys )
        {
            keysString.append( key );

            i++;

            //send next portion of keys
            if ( i % portionSize == 0 || i == keys.size() )
            {
                CommandUtil.HostCommandResults appendResults = commandUtil
                        .executeParallel( localPeerCommands.getAppendSshKeysCommand( keysString.toString() ), hosts );

                keysString.setLength( 0 );

                for ( CommandUtil.HostCommandResult result : appendResults.getCommandResults() )
                {
                    if ( !result.hasSucceeded() )
                    {
                        LOG.error( "Failed to add ssh keys on host {}: {}", result.getHost().getHostname(),
                                result.getFailureReason() );
                    }
                }

                if ( appendResults.hasFailures() )
                {
                    throw new PeerException( "Failed to add ssh keys on all hosts" );
                }
            }
        }

        //config ssh
        CommandUtil.HostCommandResults configResults =
                commandUtil.executeParallel( localPeerCommands.getConfigSSHCommand(), hosts );

        for ( CommandUtil.HostCommandResult result : configResults.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "Failed to configure ssh on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( configResults.hasFailures() )
        {
            throw new PeerException( "Failed to configure ssh on all hosts" );
        }
    }


    @Override
    public void addSshKey( final EnvironmentId environmentId, final String sshPublicKey ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPublicKey ), "Invalid ssh key" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        CommandUtil.HostCommandResults results =
                commandUtil.executeParallel( localPeerCommands.getAppendSshKeyCommand( sshPublicKey ), hosts );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "SSH key addition failed on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( results.hasFailures() )
        {
            throw new PeerException( "Failed to add SSH key on all hosts" );
        }
    }


    @Override
    public void removeSshKey( final EnvironmentId environmentId, final String sshPublicKey ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPublicKey ), "Invalid ssh key" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        CommandUtil.HostCommandResults results =
                commandUtil.executeParallel( localPeerCommands.getRemoveSshKeyCommand( sshPublicKey ), hosts );


        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "SSH key removal failed on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( results.hasFailures() )
        {
            throw new PeerException( "Failed to remove SSH key on all hosts" );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public PrepareTemplatesResponse prepareTemplates( final PrepareTemplatesRequest request ) throws PeerException
    {
        PrepareTemplatesResponse response = new PrepareTemplatesResponse();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final String resourceHostId : request.getTemplates().keySet() )
        {
            final ResourceHost resourceHost = getResourceHostById( resourceHostId );

            for ( final String templateName : request.getTemplates().get( resourceHostId ) )
            {
                HostUtil.Task<Object> importTask = new ImportTemplateTask( templateName, resourceHost );

                tasks.addTask( resourceHost, importTask );
            }
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, request.getEnvironmentId() );

        response.addResults( results );

        return response;
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public CreateEnvironmentContainersResponse createEnvironmentContainers(
            final CreateEnvironmentContainersRequest requestGroup ) throws PeerException
    {
        Preconditions.checkNotNull( requestGroup );

        NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( requestGroup.getEnvironmentId() );

        if ( reservedNetworkResource == null )
        {
            throw new PeerException( String.format( "No reserved network resources found for environment %s",
                    requestGroup.getEnvironmentId() ) );
        }

        //clone containers
        HostUtil.Tasks cloneTasks = new HostUtil.Tasks();

        for ( final CloneRequest request : requestGroup.getRequests() )
        {
            ResourceHost resourceHost = getResourceHostById( request.getResourceHostId() );

            CloneContainerTask task = new CloneContainerTask( request, resourceHost, reservedNetworkResource, this );

            cloneTasks.addTask( resourceHost, task );
        }

        HostUtil.Results cloneResults = hostUtil.execute( cloneTasks, reservedNetworkResource.getEnvironmentId() );

        //register succeeded containers
        HostUtil.Tasks quotaTasks = new HostUtil.Tasks();

        for ( HostUtil.Task cloneTask : cloneResults.getTasks().getTasks() )
        {
            CloneRequest request = ( ( CloneContainerTask ) cloneTask ).getRequest();

            if ( cloneTask.getTaskState() == HostUtil.Task.TaskState.SUCCEEDED )
            {

                final HostInterfaces interfaces = new HostInterfaces();

                interfaces.addHostInterface(
                        new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE, request.getIp().split( "/" )[0] ) );

                ContainerHostEntity containerHostEntity =
                        new ContainerHostEntity( getId(), ( ( CloneContainerTask ) cloneTask ).getResult(),
                                request.getHostname(), request.getTemplateArch(), interfaces,
                                request.getContainerName(), request.getTemplateName(), request.getTemplateArch().name(),
                                requestGroup.getEnvironmentId(), requestGroup.getOwnerId(),
                                requestGroup.getInitiatorPeerId(), request.getContainerSize() );

                registerContainer( request.getResourceHostId(), containerHostEntity );

                quotaTasks.addTask( cloneTask.getHost(),
                        new SetQuotaTask( request, ( ResourceHost ) cloneTask.getHost(), containerHostEntity ) );
            }
        }

        if ( !quotaTasks.isEmpty() )
        {
            //set quotas to succeeded containers asynchronously
            hostUtil.submit( quotaTasks, reservedNetworkResource.getEnvironmentId() );
        }

        return new CreateEnvironmentContainersResponse( cloneResults );
    }


    protected void registerContainer( String resourceHostId, ContainerHostEntity containerHost ) throws PeerException
    {

        ResourceHost resourceHost = getResourceHostById( resourceHostId );

        try
        {
            signContainerKeyWithPEK( containerHost.getId(), containerHost.getEnvironmentId() );

            resourceHost.addContainerHost( containerHost );

            resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );

            LOG.debug( "New container host registered: " + containerHost.getHostname() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );

            throw new PeerException( String.format( "Error registering container: %s", e.getMessage() ), e );
        }
    }


    private void signContainerKeyWithPEK( String containerId, EnvironmentId envId ) throws PeerException
    {
        String pairId = String.format( "%s_%s", getId(), envId.getId() );
        final PGPSecretKeyRing pekSecKeyRing = securityManager.getKeyManager().getSecretKeyRing( pairId );

        PGPPublicKeyRing containerPub = securityManager.getKeyManager().getPublicKeyRing( containerId );

        PGPPublicKeyRing signedKey =
                securityManager.getKeyManager().setKeyTrust( pekSecKeyRing, containerPub, KeyTrustLevel.Full.getId() );

        securityManager.getKeyManager().updatePublicKeyRing( signedKey );
    }


    @PermitAll
    @Override
    public Set<ContainerHost> findContainersByEnvironmentId( final String environmentId )
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        Set<ContainerHost> result = new HashSet<>();

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByEnvironmentId( environmentId ) );
        }
        return result;
    }


    @Override
    public Set<ContainerHost> findContainersByOwnerId( final String ownerId )
    {
        Preconditions.checkNotNull( ownerId, "Specify valid owner" );


        Set<ContainerHost> result = new HashSet<>();

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByOwnerId( ownerId ) );
        }

        return result;
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Container hostname shouldn't be null" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostByName( hostname );
            }
            catch ( HostNotFoundException ignore )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "No container host found for name %s", hostname ) );
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostById( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Invalid container host id" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostById( hostId );
            }
            catch ( HostNotFoundException e )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "Container host not found by id %s", hostId ) );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid resource host hostname" );


        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getHostname().equalsIgnoreCase( hostname ) )
            {
                return resourceHost;
            }
        }
        throw new HostNotFoundException( String.format( "Resource host not found by hostname %s", hostname ) );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostById( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Resource host id is null" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getId().equals( hostId ) )
            {
                return resourceHost;
            }
        }
        throw new HostNotFoundException( String.format( "Resource host not found by id %s", hostId ) );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByContainerName( final String containerName ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        ContainerHost c = getContainerHostByName( containerName );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByContainerId( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Container host id is invalid" );

        ContainerHost c = getContainerHostById( hostId );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public Host bindHost( String id ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( id );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getId().equals( id ) )
            {
                return resourceHost;
            }
            else
            {
                try
                {
                    return resourceHost.getContainerHostById( id );
                }
                catch ( HostNotFoundException ignore )
                {
                    //ignore
                }
            }
        }

        throw new HostNotFoundException( String.format( "Host by id %s is not registered", id ) );
    }


    @Override
    public ContainerHostEntity bindHost( final ContainerId containerId ) throws HostNotFoundException
    {
        return ( ContainerHostEntity ) bindHost( containerId.getId() );
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void startContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity containerHost = bindHost( containerId );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.startContainerHost( containerHost );
        }
        catch ( Exception e )
        {
            String errMsg =
                    String.format( "Could not start container %s: %s", containerHost.getHostname(), e.getMessage() );
            LOG.error( errMsg );
            throw new PeerException( errMsg, e );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void stopContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity containerHost = bindHost( containerId );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.stopContainerHost( containerHost );
        }
        catch ( Exception e )
        {
            String errMsg =
                    String.format( "Could not stop container %s: %s", containerHost.getHostname(), e.getMessage() );
            LOG.error( errMsg );
            throw new PeerException( errMsg, e );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void destroyContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity host = bindHost( containerId );
        ResourceHost resourceHost = host.getParent();

        try
        {
            resourceHost.destroyContainerHost( host );
            quotaManager.removeQuota( containerId );
        }
        catch ( ResourceHostException e )
        {
            String errMsg = String.format( "Could not destroy container %s: %s", host.getHostname(), e.getMessage() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e );
        }

        resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );
    }


    @Override
    public boolean isConnected( final HostId hostId )
    {
        Preconditions.checkNotNull( hostId, "Host id null" );

        try
        {
            HostInfo hostInfo = hostRegistry.getHostInfoById( hostId.getId() );
            return hostInfo.getId().equals( hostId.getId() );
        }
        catch ( HostDisconnectedException e )
        {
            return false;
        }
    }


    @Override
    public ResourceHost getManagementHost() throws HostNotFoundException
    {
        return getResourceHostByContainerName( Common.MANAGEMENT_HOSTNAME );
    }


    @Override
    public Set<ResourceHost> getResourceHosts()
    {
        return Collections.unmodifiableSet( this.resourceHosts );
    }


    public void addResourceHost( final ResourceHost host )
    {
        Preconditions.checkNotNull( host, "Resource host could not be null." );

        resourceHosts.add( host );
    }


    public void removeResourceHost( String rhId ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( rhId ) );

        ResourceHost resourceHost = getResourceHostById( rhId );

        //remove rh ssl cert
        securityManager.getKeyStoreManager().removeCertFromTrusted( SystemSettings.getSecurePortX2(), rhId );

        securityManager.getHttpContextManager().reloadKeyStore();

        //remove rh key
        KeyManager keyManager = securityManager.getKeyManager();

        keyManager.removeKeyData( rhId );

        //remove rh containers' keys
        for ( final ContainerHost containerHost : resourceHost.getContainerHosts() )
        {
            keyManager.removeKeyData( containerHost.getKeyId() );
        }

        //remove rh from cache
        resourceHosts.remove( resourceHost );

        //remove rh from db
        resourceHostDataService.remove( resourceHost.getId() );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        return execute( requestBuilder, host, null );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host aHost,
                                  final CommandCallback callback ) throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder, "Invalid request" );
        Preconditions.checkNotNull( aHost, "Invalid host" );

        CommandResult result;

        if ( callback == null )
        {
            result = commandExecutor.execute( aHost.getId(), requestBuilder );
        }
        else
        {
            result = commandExecutor.execute( aHost.getId(), requestBuilder, callback );
        }

        return result;
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host aHost, final CommandCallback callback )
            throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder, "Invalid request" );
        Preconditions.checkNotNull( aHost, "Invalid host" );

        if ( callback == null )
        {
            commandExecutor.executeAsync( aHost.getId(), requestBuilder );
        }
        else
        {
            commandExecutor.executeAsync( aHost.getId(), requestBuilder, callback );
        }
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
    public TemplateKurjun getTemplate( final String templateName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );

        return templateRegistry.getTemplate( templateName );
    }


    @PermitAll
    @Override
    public boolean isOnline()
    {
        return true;
    }


    @Override
    public <T, V> V sendRequest( final T request, final String recipient, final int requestTimeout,
                                 final Class<V> responseType, final int responseTimeout, Map<String, String> headers )
            throws PeerException
    {
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        return sendRequestInternal( request, recipient, responseType );
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int requestTimeout,
                                 Map<String, String> headers ) throws PeerException
    {
        sendRequestInternal( request, recipient, null );
    }


    protected <T, V> V sendRequestInternal( final T request, final String recipient, final Class<V> responseType )
            throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );

        for ( RequestListener requestListener : requestListeners )
        {
            if ( recipient.equalsIgnoreCase( requestListener.getRecipient() ) )
            {
                try
                {
                    Object response = requestListener.onRequest( new Payload( request, getId() ) );

                    if ( response != null && responseType != null )
                    {
                        return responseType.cast( response );
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage() );
                    throw new PeerException( e );
                }
            }
        }

        return null;
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, Set<QuotaAlertValue> alerts )
    {
        LOG.debug( "On heartbeat: " + resourceHostInfo.getHostname() );

        if ( initialized )
        {
            boolean firstMhRegistration = false;

            ResourceHostEntity host;

            try
            {
                host = ( ResourceHostEntity ) getResourceHostByName( resourceHostInfo.getHostname() );
            }
            catch ( HostNotFoundException e )
            {
                //register new RH
                host = new ResourceHostEntity( getId(), resourceHostInfo );

                resourceHostDataService.persist( host );

                addResourceHost( host );

                setResourceHostTransientFields( Sets.<ResourceHost>newHashSet( host ) );

                buildAdminHostRelation( host );

                LOG.debug( String.format( "Resource host %s registered.", resourceHostInfo.getHostname() ) );

                for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
                {
                    if ( Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( containerHostInfo.getHostname() ) )
                    {
                        firstMhRegistration = true;
                        break;
                    }
                }
            }

            //update host info from heartbeat
            if ( host.updateHostInfo( resourceHostInfo ) )
            {
                resourceHostDataService.update( host );

                LOG.debug( String.format( "Resource host %s updated.", resourceHostInfo.getHostname() ) );
            }

            if ( firstMhRegistration )
            {
                //exchange keys with MH container
                try
                {
                    exchangeKeys( host, Common.MANAGEMENT_HOSTNAME );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error exchanging keys with MH" );
                }

                //setup security
                try
                {
                    buildAdminHostRelation( getContainerHostByName( Common.MANAGEMENT_HOSTNAME ) );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error setting up security relations with MH", e );
                }
            }
        }
    }


    private void buildAdminHostRelation( Host host )
    {
        // Build relation between Admin and management/resource host.

        User peerOwner = identityManager.getUserByKeyId( identityManager.getPeerOwnerId() );
        if ( peerOwner != null )
        {
            // Simply pass key value object as map
            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
            Map<String, String> relationTraits = relationInfoMeta.getRelationTraits();
            relationTraits.put( "bandwidthControl", "true" );

            if ( Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( host.getHostname() ) )
            {
                relationTraits.put( "managementSupervisor", "true" );
            }
            else
            {
                relationTraits.put( "resourceSupervisor", "true" );
                relationTraits.put( "containerManagement", "true" );
            }

            RelationMeta relationMeta = new RelationMeta( peerOwner, peerOwner, host, peerOwner.getSecurityKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
    }


    @Override
    public void exchangeKeys( ResourceHost resourceHost, String hostname ) throws Exception
    {
        RegistrationManager registrationManager = ServiceLocator.getServiceNoCache( RegistrationManager.class );

        String token = registrationManager.generateContainerTTLToken( 30 * 1000L ).getToken();

        commandUtil.execute( localPeerCommands.getExchangeKeyCommand( hostname, token ), resourceHost );
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkArgument( pid > 0, "Process pid must be greater than 0" );

        try
        {
            return monitor.getProcessResourceUsage( containerId, pid );
        }
        catch ( MonitorException e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException( e );
        }
    }


    @Override
    public Set<Integer> getCpuSet( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getCpuSet( host.getContainerId() );
        }
        catch ( QuotaException e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException( e );
        }
    }


    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void setCpuSet( final ContainerHost host, final Set<Integer> cpuSet ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ), "Empty cpu set" );

        try
        {
            quotaManager.setCpuSet( host.getContainerId(), cpuSet );
        }
        catch ( QuotaException e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException( e );
        }
    }


    //networking


    @Override
    public String getVniDomain( final Long vni ) throws PeerException
    {
        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                return getNetworkManager().getVlanDomain( reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg =
                        String.format( "Error obtaining domain by vlan %d: %s", reservedNetworkResource.getVlan(),
                                e.getMessage() );
                LOG.error( errMsg );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void removeVniDomain( final Long vni ) throws PeerException
    {
        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                getNetworkManager().removeVlanDomain( reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg =
                        String.format( "Error removing domain by vlan %d: %s", reservedNetworkResource.getVlan(),
                                e.getMessage() );
                LOG.error( errMsg );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void setVniDomain( final Long vni, final String domain,
                              final DomainLoadBalanceStrategy domainLoadBalanceStrategy, final String sslCertPath )
            throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domain ) );
        Preconditions.checkNotNull( domainLoadBalanceStrategy );

        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                getNetworkManager().setVlanDomain( reservedNetworkResource.getVlan(), domain, domainLoadBalanceStrategy,
                        sslCertPath );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg = String.format( "Error setting domain by vlan %d: %s", reservedNetworkResource.getVlan(),
                        e.getMessage() );
                LOG.error( errMsg );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @Override
    public boolean isIpInVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ) );

        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                return getNetworkManager().isIpInVlanDomain( hostIp, reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg = String.format( "Error checking domain by ip %s and vlan %d: %s", hostIp,
                        reservedNetworkResource.getVlan(), e.getMessage() );
                LOG.error( errMsg );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void addIpToVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ) );

        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                getNetworkManager().addIpToVlanDomain( hostIp, reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg = String.format( "Error adding ip %s to domain by vlan %d: %s", hostIp,
                        reservedNetworkResource.getVlan(), e.getMessage() );
                LOG.error( errMsg );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void removeIpFromVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ) );

        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                getNetworkManager().removeIpFromVlanDomain( hostIp, reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg = String.format( "Error removing ip %s from domain by vlan %d: %s", hostIp,
                        reservedNetworkResource.getVlan(), e.getMessage() );
                LOG.error( errMsg );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public int setupSshTunnelForContainer( final String containerIp, final int sshIdleTimeout ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( sshIdleTimeout > 0 );


        try
        {
            return getNetworkManager().setupContainerSsh( containerIp, sshIdleTimeout );
        }
        catch ( NetworkManagerException e )
        {
            String errMsg =
                    String.format( "Error setting up ssh tunnel for container ip %s: %s", containerIp, e.getMessage() );
            LOG.error( errMsg );
            throw new PeerException( errMsg, e );
        }
    }


    @Override
    public List<ContainerHost> getPeerContainers( final String peerId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

        List<ContainerHost> result = new ArrayList<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByPeerId( peerId ) );
        }
        return result;
    }


    @Override
    public void addRequestListener( final RequestListener listener )
    {
        if ( listener != null )
        {
            requestListeners.add( listener );
        }
    }


    @Override
    public void removeRequestListener( final RequestListener listener )
    {
        if ( listener != null )
        {
            requestListeners.remove( listener );
        }
    }


    @Override
    public Set<RequestListener> getRequestListeners()
    {
        return Collections.unmodifiableSet( requestListeners );
    }


    /* ***********************************************
     *  Create PEK
     */
    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public PublicKeyContainer createPeerEnvironmentKeyPair( RelationLinkDto envLink ) throws PeerException
    {
        Preconditions.checkNotNull( envLink );

        KeyManager keyManager = securityManager.getKeyManager();
        EncryptionTool encTool = securityManager.getEncryptionTool();
        String pairId = String.format( "%s_%s", getId(), envLink.getUniqueIdentifier() );

        PGPPublicKeyRing envPubkey = keyManager.getPublicKeyRing( pairId );
        try
        {
            if ( envPubkey == null )
            {
                buildPeerEnvRelation( envLink );

                final PGPSecretKeyRing peerSecKeyRing = keyManager.getSecretKeyRing( null );
                KeyPair keyPair = keyManager.generateKeyPair( pairId, false );

                //******Create PEK *****************************************************************
                PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
                PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );

                //***************Save Keys *********************************************************
                keyManager.saveSecretKeyRing( pairId, SecurityKeyType.PeerEnvironmentKey.getId(), secRing );
                keyManager.savePublicKeyRing( pairId, SecurityKeyType.PeerEnvironmentKey.getId(), pubRing );

                pubRing = keyManager.setKeyTrust( peerSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

                return new PublicKeyContainer( getId(), pubRing.getPublicKey().getFingerprint(),
                        encTool.armorByteArrayToString( pubRing.getEncoded() ) );
            }
            else
            {
                return new PublicKeyContainer( getId(), envPubkey.getPublicKey().getFingerprint(),
                        encTool.armorByteArrayToString( envPubkey.getEncoded() ) );
            }
        }
        catch ( Exception e )
        {
            String errMsg = String.format( "Error creating PEK: %s", e.getMessage() );
            LOG.error( errMsg );
            throw new PeerException( errMsg, e );
        }
    }


    private void buildPeerEnvRelation( final RelationLink envLink )
    {

        // Build relation between LocalPeer and LocalEnvironment/CrossPeerEnvironment.

        User peerOwner = identityManager.getUserByKeyId( identityManager.getPeerOwnerId() );
        if ( peerOwner != null )
        {
            // Simply pass key value object as map
            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
            Map<String, String> relationTraits = relationInfoMeta.getRelationTraits();
            relationTraits.put( "hostEnvironment", "true" );
            relationTraits.put( "containerLimit", "unlimited" );
            relationTraits.put( "bandwidthLimit", "unlimited" );

            RelationMeta relationMeta = new RelationMeta( peerOwner, this, envLink, this.getKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
    }


    @Override
    public void updatePeerEnvironmentPubKey( final EnvironmentId environmentId, final PGPPublicKeyRing pubKeyRing )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId );
        Preconditions.checkNotNull( pubKeyRing );

        securityManager.getKeyManager().updatePublicKeyRing( pubKeyRing );
    }


    @Override
    public void addPeerEnvironmentPubKey( final String keyId, final PGPPublicKeyRing pubRing )
    {
        Preconditions.checkNotNull( keyId );
        Preconditions.checkNotNull( pubRing );

        securityManager.getKeyManager().savePublicKeyRing( keyId, SecurityKeyType.PeerEnvironmentKey.getId(), pubRing );

        // Build relation between LocalPeer => RemotePeer => Environment
        // for message encryption/decryption mechanism described in relation traits
        String[] ids = keyId.split( "_" );
        if ( ids.length == 2 )
        {
            String envId = ids[1];
            RelationLink envLink = relationManager.getRelationLink( envId );
            RelationLink peerLink = relationManager.getRelationLink( ids[0] );

            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
            Map<String, String> relationTraits = relationInfoMeta.getRelationTraits();
            relationTraits.put( "encryptMessage", "true" );
            relationTraits.put( "decryptMessage", "true" );

            RelationMeta relationMeta = new RelationMeta( this, peerLink, envLink, this.getKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
    }


    @Override
    public HostInterfaces getInterfaces() throws HostNotFoundException
    {
        return getManagementHost().getHostInterfaces();
    }


    @Override
    public synchronized void reserveNetworkResource( final NetworkResourceImpl networkResource ) throws PeerException
    {

        Preconditions.checkNotNull( networkResource );

        try
        {
            NetworkResource nr = networkResourceDao.find( networkResource );

            if ( nr != null )
            {
                throw new PeerException( String.format( "Network resource %s is already reserved", nr ) );
            }
            else
            {
                UsedNetworkResources usedNetworkResources = getUsedNetworkResources();

                if ( usedNetworkResources.containerSubnetExists( networkResource.getContainerSubnet() ) )
                {
                    throw new PeerException( String.format( "Container subnet %s is already reserved",
                            networkResource.getContainerSubnet() ) );
                }
                if ( usedNetworkResources.p2pSubnetExists( networkResource.getP2pSubnet() ) )
                {
                    throw new PeerException(
                            String.format( "P2P subnet %s is already reserved", networkResource.getP2pSubnet() ) );
                }
                if ( usedNetworkResources.vniExists( networkResource.getVni() ) )
                {
                    throw new PeerException( String.format( "VNI %d is already reserved", networkResource.getVni() ) );
                }

                //calculate free vlan for this environment
                int freeVlan = usedNetworkResources.calculateFreeVlan();
                if ( freeVlan == -1 )
                {
                    throw new PeerException( "No free VLANs slots are left" );
                }

                networkResourceDao.create( new NetworkResourceEntity( networkResource, freeVlan ) );
            }
        }
        catch ( Exception e )
        {
            String errMsg = String.format( "Error reserving network resources: %s", e.getMessage() );
            LOG.error( errMsg );
            throw new PeerException( errMsg, e );
        }
    }


    @Override
    public ReservedNetworkResources getReservedNetworkResources() throws PeerException
    {
        ReservedNetworkResources reservedNetworkResources = new ReservedNetworkResources();

        try
        {
            for ( NetworkResource networkResource : networkResourceDao.readAll() )
            {
                reservedNetworkResources.addNetworkResource( networkResource );
            }
        }
        catch ( Exception e )
        {
            String errMsg = String.format( "Error getting reserved network resources: %s", e.getMessage() );
            LOG.error( errMsg );
            throw new PeerException( errMsg, e );
        }

        return reservedNetworkResources;
    }


    @Override
    public UsedNetworkResources getUsedNetworkResources() throws PeerException
    {
        final UsedNetworkResources usedNetworkResources = new UsedNetworkResources();

        Set<ResourceHost> resourceHosts = getResourceHosts();

        HostUtil.Tasks hostTasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHosts )
        {
            hostTasks.addTask( resourceHost, new UsedHostNetResourcesTask( resourceHost, usedNetworkResources ) );
        }

        HostUtil.Results results = hostUtil.executeFailFast( hostTasks, null );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg =
                    String.format( "Error gathering reserved net resources on host %s: %s", task.getHost().getId(),
                            task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }


        //add reserved ones too
        for ( NetworkResource networkResource : getReservedNetworkResources().getNetworkResources() )
        {
            usedNetworkResources.addVni( networkResource.getVni() );
            usedNetworkResources.addVlan( networkResource.getVlan() );
            usedNetworkResources.addContainerSubnet( networkResource.getContainerSubnet() );
            usedNetworkResources.addP2pSubnet( networkResource.getP2pSubnet() );
        }

        return usedNetworkResources;
    }

    //----------- P2P SECTION BEGIN --------------------


    //TODO this is for basic environment via hub
    //@RolesAllowed( "Environment-Management|Write" )
    @Override
    public void setupTunnels( final P2pIps p2pIps, final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( p2pIps, "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( environmentId );

        if ( reservedNetworkResource == null )
        {
            throw new PeerException(
                    String.format( "No reserved network resources found for environment %s", environmentId ) );
        }

        Set<ResourceHost> resourceHosts = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHosts )
        {
            //setup tunnel only if this RH participates in the swarm
            if ( p2pIps.findByRhId( resourceHost.getId() ) != null )
            {
                tasks.addTask( resourceHost, new TunnelsTask( resourceHost, p2pIps, reservedNetworkResource ) );
            }
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, reservedNetworkResource.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg = String.format( "Error setting up tunnels on host %s: %s", task.getHost().getId(),
                    task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }
    }


    @Override
    public void resetSwarmSecretKey( final P2PCredentials p2PCredentials ) throws PeerException
    {

        Preconditions.checkNotNull( p2PCredentials, "Invalid p2p credentials" );

        Set<ResourceHost> resourceHosts = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHosts )
        {
            tasks.addTask( resourceHost, new ResetP2PSwarmSecretTask( resourceHost, p2PCredentials.getP2pHash(),
                    p2PCredentials.getP2pSecretKey(), p2PCredentials.getP2pTtlSeconds() ) );
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, p2PCredentials.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg = String.format( "Error resetting P2P secret key on host %s: %s", task.getHost().getId(),
                    task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void joinP2PSwarm( final P2PConfig config ) throws PeerException
    {
        Preconditions.checkNotNull( config, "Invalid p2p config" );

        NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( config.getEnvironmentId() );

        if ( reservedNetworkResource == null )
        {
            throw new PeerException(
                    String.format( "Reserved vni not found for environment %s", config.getEnvironmentId() ) );
        }

        final String p2pInterface = P2PUtil.generateInterfaceName( reservedNetworkResource.getVlan() );

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final RhP2pIp rhP2pIp : config.getRhP2pIps() )
        {
            final ResourceHost resourceHost = getResourceHostById( rhP2pIp.getRhId() );

            tasks.addTask( resourceHost,
                    new JoinP2PSwarmTask( resourceHost, rhP2pIp.getP2pIp(), p2pInterface, config.getHash(),
                            config.getSecretKey(), config.getSecretKeyTtlSec() ) );
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, reservedNetworkResource.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg = String.format( "Error joining P2P swarm on host %s: %s", task.getHost().getId(),
                    task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }
    }


    @Override
    public void joinOrUpdateP2PSwarm( final P2PConfig config ) throws PeerException
    {
        Preconditions.checkNotNull( config, "Invalid p2p config" );

        NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( config.getEnvironmentId() );

        if ( reservedNetworkResource == null )
        {
            throw new PeerException(
                    String.format( "Reserved vni not found for environment %s", config.getEnvironmentId() ) );
        }

        final String p2pInterface = P2PUtil.generateInterfaceName( reservedNetworkResource.getVlan() );

        Set<ResourceHost> resourceHosts = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHosts )
        {
            final RhP2pIp rhP2pIp = config.findByRhId( resourceHost.getId() );

            if ( rhP2pIp != null )
            {
                //try to join RH (updates if already participating)
                tasks.addTask( resourceHost,
                        new JoinP2PSwarmTask( resourceHost, rhP2pIp.getP2pIp(), p2pInterface, config.getHash(),
                                config.getSecretKey(), config.getSecretKeyTtlSec() ) );
            }
            else
            {
                //try to update missing RH in case it participates in the swarm
                tasks.addTask( resourceHost,
                        new ResetP2PSwarmSecretTask( resourceHost, config.getHash(), config.getSecretKey(),
                                config.getSecretKeyTtlSec() ) );
            }
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, reservedNetworkResource.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg = String.format( "Error joining/updating P2P swarm on host %s: %s", task.getHost().getId(),
                    task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }
    }


    //----------- P2P SECTION END --------------------


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void cleanupEnvironment( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        final NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( environmentId.getId() );

        if ( reservedNetworkResource == null )
        {
            LOG.warn( "Network reservation for environment {} not found", environmentId.getId() );
            return;
        }

        //interrupt active environment operations
        boolean hasActiveTasks = hostUtil.cancelEnvironmentTasks( environmentId.getId() );

        if ( hasActiveTasks )
        {
            //await clone commands on agent to complete, best effort
            TaskUtil.sleep( 10 * 1000 ); // 10 sec
        }

        //send cleanup command to RHs
        Set<ResourceHost> resourceHosts = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHosts )
        {
            tasks.addTask( resourceHost, new CleanupEnvironmentTask( resourceHost, reservedNetworkResource ) );
        }

        hostUtil.submit( tasks, reservedNetworkResource.getEnvironmentId() );


        try
        {
            //remove PEK
            KeyManager keyManager = securityManager.getKeyManager();

            keyManager.removeKeyData( environmentId.getId() );

            keyManager.removeKeyData( getId() + "_" + environmentId.getId() );

            //remove container keys
            Containers containers = getEnvironmentContainers( environmentId );

            for ( final ContainerHostInfo containerHostInfo : containers.getContainers() )
            {
                keyManager.removeKeyData( containerHostInfo.getId() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to delete PEK for environment {}", environmentId.getId(), e );
        }

        //remove reservation
        try
        {
            networkResourceDao.delete( ( NetworkResourceEntity ) reservedNetworkResource );
        }
        catch ( DaoException e )
        {
            LOG.error( "Failed to delete network reservation for environment {}", environmentId.getId(), e );
        }
    }


    @Override
    public ResourceHostMetrics getResourceHostMetrics()
    {
        return monitor.getResourceHostMetrics();
    }


    @Override
    public PeerResources getResourceLimits( final String peerId ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

        return quotaManager.getResourceLimits( peerId );
    }


    @Override
    public List<TemplateKurjun> getTemplates()
    {
        return templateRegistry.list();
    }


    @Override
    public TemplateKurjun getTemplateByName( final String name )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ) );

        return templateRegistry.getTemplate( name );
    }


    @Override
    public ContainerQuota getQuota( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            return quotaManager.getQuota( containerHost.getContainerId() );
        }
        catch ( QuotaException e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException(
                    String.format( "Could not obtain quota for %s: %s", containerId.getId(), e.getMessage() ) );
        }
    }


    @Override
    public void setQuota( final ContainerId containerId, final ContainerQuota containerQuota ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( containerQuota );
        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            quotaManager.setQuota( containerHost.getContainerId(), containerQuota );
        }
        catch ( QuotaException e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException(
                    String.format( "Could not set quota for %s: %s", containerId.getId(), e.getMessage() ) );
        }
    }


    @Override
    public void alert( AlertEvent alert )
    {
        Preconditions.checkNotNull( alert );

        monitor.addAlert( alert );
    }


    @Override
    public String getHistoricalMetrics( final String hostname, final Date startTime, final Date endTime )
            throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkNotNull( startTime );
        Preconditions.checkNotNull( endTime );

        try
        {
            Host host = findHostByName( hostname );
            return monitor.getHistoricalMetrics( host, startTime, endTime );
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException( e.getMessage(), e );
        }
    }


    @Override
    public Host findHostByName( final String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getHostname().equals( hostname ) )
            {
                return resourceHost;
            }
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                if ( containerHost.getHostname().equals( hostname ) )
                {
                    return containerHost;
                }
            }
        }

        throw new HostNotFoundException( "Host by name '" + hostname + "' not found." );
    }


    @Override
    public void addReverseProxy( final ReverseProxyConfig reverseProxyConfig ) throws PeerException
    {
        ContainerHost containerHost = getContainerHostById( reverseProxyConfig.getContainerId() );

        if ( containerHost == null )
        {
            throw new PeerException( "Container host not found." );
        }

        final NetworkResource networkResource =
                getReservedNetworkResources().findByEnvironmentId( containerHost.getEnvironmentId().getId() );

        if ( networkResource == null )
        {
            throw new PeerException( "Network resources not found." );
        }

        final HostInterface netInterface = containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );

        if ( netInterface instanceof NullHostInterface )
        {
            throw new PeerException( "Container network interface is null." );
        }

        try
        {
            getNetworkManager().removeVlanDomain( networkResource.getVlan() );
            getNetworkManager()
                    .setVlanDomain( networkResource.getVlan(), reverseProxyConfig.getDomainName(), netInterface.getIp(),
                            reverseProxyConfig.getSslCertPath() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on adding reverse proxy." );
        }
    }


    protected NetworkManager getNetworkManager()
    {

        return serviceLocator.getService( NetworkManager.class );
    }


    public Set<HostUtil.Task> getTasks()
    {
        return hostUtil.getAllTasks();
    }


    @Override
    public void cancelAllTasks()
    {
        hostUtil.cancelAll();
    }


    @Override
    public String getExternalIp()
    {
        return getPeerInfo().getIp();
    }


    @Override
    public HostId getResourceHostIdByContainerId( final ContainerId id ) throws PeerException
    {
        return new HostId( getResourceHostByContainerId( id.getId() ).getId() );
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof LocalPeerImpl ) )
        {
            return false;
        }

        final LocalPeerImpl that = ( LocalPeerImpl ) o;

        return getId().equals( that.getId() );
    }


    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }


    @Override
    public String getLinkId()
    {
        return String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
    }


    @Override
    public String getUniqueIdentifier()
    {
        return getId();
    }


    @Override
    public String getClassPath()
    {
        return this.getClass().getSimpleName();
    }


    @Override
    public String getContext()
    {
        return PermissionObject.PeerManagement.getName();
    }


    @Override
    public String getKeyId()
    {
        return getId();
    }
}

