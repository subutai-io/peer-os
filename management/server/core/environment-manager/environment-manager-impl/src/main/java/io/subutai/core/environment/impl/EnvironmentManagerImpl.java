package io.subutai.core.environment.impl;


import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;
import javax.ws.rs.WebApplicationException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentCreationRef;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.AlertValue;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeerEventListener;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RemotePeer;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Template;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.environment.impl.adapter.HubEnvironment;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.EnvironmentAlertHandlerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.ContainerDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.EnvironmentDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.EnvironmentModifyWorkflow;
import io.subutai.core.environment.impl.workflow.modification.HostnameModificationWorkflow;
import io.subutai.core.environment.impl.workflow.modification.P2PSecretKeyModificationWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyAdditionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyRemovalWorkflow;
import io.subutai.core.environment.impl.xpeer.RemoteEnvironment;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionListener;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.template.api.TemplateManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.hub.share.common.HubAdapter;
import io.subutai.hub.share.common.HubEventListener;
import io.subutai.hub.share.dto.PeerProductDataDto;
import io.subutai.hub.share.quota.ContainerQuota;


/**
 * TODO
 *
 * 1) add p2pSecret property to peerConf, set it only after successful p2p secret update on the associated peer (in
 * P2PSecretKeyResetStep)
 *
 * 2) add secret key TTL property to environment (user should be able to change it - add to EM API), update background
 * task to consider this TTL (make background task run frequently with short intervals)
 **/
public class EnvironmentManagerImpl
        implements EnvironmentManager, PeerActionListener, AlertListener, HubEventListener, HostListener,
        LocalPeerEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class );

    protected static final String MODULE_NAME = "Environment Manager";
    private static final long RESET_ENVS_P2P_KEYS_INTERVAL_MIN = 60;
    private static final long SYNC_ENVS_WITH_HUB_INTERVAL_MIN = 10;
    private static final String REMOTE_OWNER_NAME = "remote";
    private static final String UKNOWN_OWNER_NAME = "unknown";

    private final IdentityManager identityManager;
    private final RelationManager relationManager;
    private final PeerManager peerManager;
    private final TemplateManager templateManager;
    private final Tracker tracker;
    protected Set<EnvironmentEventListener> listeners = Sets.newConcurrentHashSet();
    protected ExecutorService executor;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Map<String, AlertHandler> alertHandlers = new ConcurrentHashMap<>();
    private SecurityManager securityManager;
    protected ScheduledExecutorService backgroundTasksExecutorService;
    protected Map<String, CancellableWorkflow> activeWorkflows = Maps.newConcurrentMap();
    private Subject systemUser;

    private EnvironmentAdapter environmentAdapter;
    private EnvironmentService environmentService;
    protected JsonUtil jsonUtil = new JsonUtil();
    protected PGPKeyUtil pgpKeyUtil = new PGPKeyUtil();
    private volatile long lastP2pSecretKeyResetTs = 0L;
    private volatile long lastEnvSyncTs = 0L;


    public EnvironmentManagerImpl( final TemplateManager templateManager, final PeerManager peerManager,
                                   SecurityManager securityManager, final IdentityManager identityManager,
                                   final Tracker tracker, final RelationManager relationManager, HubAdapter hubAdapter,
                                   final EnvironmentService environmentService )
    {
        Preconditions.checkNotNull( templateManager );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( relationManager );
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( tracker );

        this.templateManager = templateManager;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.identityManager = identityManager;
        this.relationManager = relationManager;
        this.tracker = tracker;


        //******************************************
        Session session = identityManager.loginSystemUser();
        if ( session != null )
        {
            systemUser = session.getSubject();
        }
        //******************************************

        backgroundTasksExecutorService = getScheduleExecutor();
        backgroundTasksExecutorService.scheduleWithFixedDelay( new BackgroundTasksRunner(), 1, 1, TimeUnit.MINUTES );

        executor = getCachedExecutor();

        environmentAdapter = getEnvironmentAdapter( hubAdapter );

        this.environmentService = environmentService;
    }


    protected ExecutorService getCachedExecutor()
    {
        return Executors.newCachedThreadPool();
    }


    protected ScheduledExecutorService getScheduleExecutor()
    {
        return Executors.newSingleThreadScheduledExecutor();
    }


    protected EnvironmentAdapter getEnvironmentAdapter( HubAdapter hubAdapter )
    {
        return new EnvironmentAdapter( this, peerManager, hubAdapter, identityManager );
    }


    public void dispose()
    {
        executor.shutdown();
        backgroundTasksExecutorService.shutdown();

        for ( CancellableWorkflow cancellableWorkflow : activeWorkflows.values() )
        {
            cancellableWorkflow.cancel();
        }
    }


    public IdentityManager getIdentityManager()
    {
        return identityManager;
    }


    public RelationManager getRelationManager()
    {
        return relationManager;
    }


    @Override
    public String getName()
    {
        return MODULE_NAME;
    }


    @Override
    public PeerActionResponse onPeerAction( final PeerAction peerAction )
    {
        Preconditions.checkNotNull( peerAction );

        PeerActionResponse response = PeerActionResponse.Ok();

        switch ( peerAction.getType() )
        {
            case UNREGISTER:
                if ( isPeerInUse( ( String ) peerAction.getData() ) )
                {
                    response = PeerActionResponse.Fail( "Peer in use." );
                }
                break;
            default:
                LOG.info( "Peer action {}", peerAction.getType() );
                break;
        }

        return response;
    }


    protected boolean isPeerInUse( String peerId )
    {
        for ( LocalEnvironment e : environmentService.getAll() )
        {
            if ( e.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
            {
                return true;
            }

            for ( EnvironmentPeer p : e.getEnvironmentPeers() )
            {
                if ( peerId.equals( p.getPeerId() ) )
                {
                    return true;
                }
            }
        }

        return !peerManager.getLocalPeer().getPeerContainers( peerId ).isEmpty();
    }


    protected Set<Peer> getPeers( final Topology topology ) throws PeerException
    {
        final Set<Peer> result = new HashSet<>();
        for ( String peerId : topology.getAllPeers() )
        {
            result.add( peerManager.getPeer( peerId ) );
        }
        return result;
    }


    @Override
    public Set<Environment> getEnvironments()
    {
        Set<Environment> envs = new HashSet<>();

        envs.addAll( environmentService.getAll() );

        try
        {
            Set<HubEnvironment> hubEnvironments = environmentAdapter.getEnvironments( false );

            // remove environments that exist on Hub but don't exist on peer
            // workaround for https://github.com/subutai-io/base/issues/1464
            removeStaleHubEnvironments( hubEnvironments );

            envs.addAll( hubEnvironments );
        }
        catch ( ActionFailedException e )
        {
            //ignore
        }

        setTransientFields( envs );

        return envs;
    }


    protected void setTransientFields( Set<Environment> envs )
    {
        for ( Environment env : envs )
        {
            setEnvironmentTransientFields( env );

            setContainersTransientFields( env );
        }
    }


    void setEnvironmentTransientFields( final Environment environment )
    {
        // Using environmentManager for ProxyEnvironment may give side effects. For example, empty container list.
        if ( !( environment instanceof HubEnvironment ) )
        {
            ( ( LocalEnvironment ) environment ).setEnvironmentManager( this );
        }
    }


    void setContainersTransientFields( final Environment environment )
    {
        Set<EnvironmentContainerHost> containers = environment.getContainerHosts();

        for ( ContainerHost containerHost : containers )
        {
            EnvironmentContainerImpl environmentContainer = ( EnvironmentContainerImpl ) containerHost;

            environmentContainer.setEnvironmentManager( this );
        }
    }


    @Override
    public Set<Environment> getEnvironmentsByOwnerId( long userId )
    {
        Set<Environment> envs = new HashSet<>();

        for ( Environment env : environmentService.getAll() )
        {
            if ( env.getUserId().equals( userId ) )
            {
                envs.add( env );
            }
        }

        setTransientFields( envs );

        return envs;
    }


    Environment createEnvironment( final Topology topology, final boolean async, TrackerOperation operationTracker )
            throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topology.getEnvironmentName() ), "Invalid name" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );


        //collect participating peers
        Set<Peer> allPeers;
        try
        {
            allPeers = getPeers( topology );
        }
        catch ( PeerException e )
        {
            operationTracker.addLogFailed( e.getMessage() );
            throw new EnvironmentCreationException( e.getMessage() );
        }

        //check if peers are accessible
        for ( Peer peer : allPeers )
        {
            if ( !peer.isOnline() )
            {
                operationTracker.addLogFailed( String.format( "Peer %s is offline", peer.getName() ) );
                throw new EnvironmentCreationException( String.format( "Peer %s is offline", peer.getName() ) );
            }
        }

        //create empty environment
        final LocalEnvironment environment = createEmptyEnvironment( topology );
        // TODO add additional step for receiving trust message


        //launch environment creation workflow
        final EnvironmentCreationWorkflow environmentCreationWorkflow =
                getEnvironmentCreationWorkflow( environment, topology, topology.getSshKey(), operationTracker );

        registerActiveWorkflow( environment, environmentCreationWorkflow );

        //notify environment event listeners
        environmentCreationWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {

                notifyOnEnvironmentCreated( environment );

                removeActiveWorkflow( environment.getId() );
            }
        } );

        //wait
        if ( !async )
        {
            environmentCreationWorkflow.join();

            if ( environmentCreationWorkflow.isFailed() )
            {
                throw new EnvironmentCreationException(
                        exceptionUtil.getRootCause( environmentCreationWorkflow.getError() ) );
            }
        }

        //return created environment
        return environment;
    }


    @Override
    public EnvironmentCreationRef createEnvironment( final Topology topology, final boolean async )
            throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topology.getEnvironmentName() ), "Invalid name" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );
        if ( !Strings.isNullOrEmpty( topology.getSshKey() ) )
        {
            Preconditions.checkArgument( StringUtil.isValidSshPublicKey( topology.getSshKey() ), "Invalid ssh key" );
        }

        //create operation tracker
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Creating environment %s ", topology.getEnvironmentName() ) );

        operationTracker.addLog( "Logger initialized" );

        return new EnvironmentCreationRef( operationTracker.getId().toString(),
                createEnvironment( topology, async, operationTracker ).getId() );
    }


    LocalEnvironment createEmptyEnvironment( final Topology topology ) throws EnvironmentCreationException
    {
        LocalEnvironment environment =
                new LocalEnvironment( topology.getEnvironmentName(), topology.getSshKey(), getUserId(),
                        peerManager.getLocalPeer().getId() );

        User activeUser = identityManager.getActiveUser();

        UserDelegate delegatedUser = identityManager.getUserDelegate( activeUser.getId() );

        environment.setRawTopology( jsonUtil.to( topology ) );

        environment.setUserId( delegatedUser.getUserId() );

        environment.setSshKeyType( topology.getSshKeyType() );

        save( environment );

        createEnvironmentKeyPair( environment.getEnvironmentId() );

        setTransientFields( Sets.<Environment>newHashSet( environment ) );

        return environment;
    }


    @Override
    public Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                                          final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        final Set<EnvironmentContainerHost> oldContainers = Sets.newHashSet( environment.getContainerHosts() );

        modifyEnvironment( environmentId, topology, null, null, async );

        Set<EnvironmentContainerHost> newContainers = Sets.newHashSet();

        if ( !async )
        {
            newContainers = Sets.newHashSet( loadEnvironment( environmentId ).getContainerHosts() );
            newContainers.removeAll( oldContainers );
        }

        return newContainers;
    }


    @Override
    public EnvironmentCreationRef modifyEnvironment( final String environmentId, final Topology topology,
                                                     final List<String> removedContainers,
                                                     final Map<String, ContainerQuota> changedContainers,
                                                     final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        boolean hasQuotaModification = !CollectionUtil.isMapEmpty( changedContainers );
        boolean hasContainerDestruction = !CollectionUtil.isCollectionEmpty( removedContainers );
        boolean hasContainerCreation = topology != null && !CollectionUtil.isCollectionEmpty( topology.getAllPeers() );

        Preconditions.checkArgument( hasQuotaModification || hasContainerDestruction || hasContainerCreation,
                "No environment modification task found" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Modifying environment %s", environment.getName() ) );

        operationTracker.addLog( "Logger initialized" );

        Set<Peer> allPeers = new HashSet<>();

        try
        {
            if ( topology != null )
            {
                allPeers.addAll( getPeers( topology ) );
                allPeers.addAll( environment.getPeers() );
            }
        }
        catch ( PeerException e )
        {
            operationTracker.addLogFailed( e.getMessage() );

            throw new EnvironmentModificationException( e.getMessage() );
        }

        //check if peers are accessible
        for ( Peer peer : allPeers )
        {
            if ( !peer.isOnline() )
            {
                operationTracker.addLogFailed( String.format( "Peer %s is offline", peer.getName() ) );

                throw new EnvironmentModificationException( String.format( "Peer %s is offline", peer.getName() ) );
            }
        }

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION
                || environment.getStatus() == EnvironmentStatus.CANCELLED )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final Set<EnvironmentContainerHost> oldContainers = Sets.newHashSet( environment.getContainerHosts() );

        //launch environment growing workflow
        final EnvironmentModifyWorkflow environmentModifyWorkflow =
                getEnvironmentModifyingWorkflow( environment, topology, operationTracker, removedContainers,
                        changedContainers );

        registerActiveWorkflow( environment, environmentModifyWorkflow );

        //notify environment event listeners
        environmentModifyWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Set<EnvironmentContainerHost> newContainers = Sets.newHashSet( environment.getContainerHosts() );

                    newContainers.removeAll( oldContainers );

                    notifyOnEnvironmentGrown( loadEnvironment( environment.getId() ), newContainers );

                    removeActiveWorkflow( environment.getId() );
                }
                catch ( EnvironmentNotFoundException e )
                {
                    LOG.error( "Error notifying environment event listeners", e );
                }
            }
        } );

        //wait
        if ( !async )
        {
            environmentModifyWorkflow.join();

            if ( environmentModifyWorkflow.isFailed() )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( environmentModifyWorkflow.getError() ) );
            }
            else
            {
                Set<EnvironmentContainerHost> newContainers =
                        Sets.newHashSet( loadEnvironment( environment.getId() ).getContainerHosts() );
                newContainers.removeAll( oldContainers );
            }
        }

        return new EnvironmentCreationRef( operationTracker.getId().toString(), environmentId );
    }


    @Override
    public void addSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( StringUtil.isValidSshPublicKey( sshKey ), "Invalid ssh key" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Adding ssh key %s to environment %s ", sshKey, environment.getName() ) );


        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION
                || environment.getStatus() == EnvironmentStatus.CANCELLED )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final SshKeyAdditionWorkflow sshKeyAdditionWorkflow =
                getSshKeyAdditionWorkflow( environment, sshKey.trim(), operationTracker );

        registerActiveWorkflow( environment, sshKeyAdditionWorkflow );

        sshKeyAdditionWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                removeActiveWorkflow( environment.getId() );
            }
        } );

        //wait
        if ( !async )
        {
            sshKeyAdditionWorkflow.join();

            if ( sshKeyAdditionWorkflow.isFailed() )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( sshKeyAdditionWorkflow.getError() ) );
            }
        }

        environmentAdapter.addSshKey( environmentId, sshKey );
    }


    @Override
    public void removeSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshKey ), "Invalid ssh key" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Removing ssh key %s from environment %s ", sshKey, environment.getName() ) );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION
                || environment.getStatus() == EnvironmentStatus.CANCELLED )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final SshKeyRemovalWorkflow sshKeyRemovalWorkflow =
                getSshKeyRemovalWorkflow( environment, sshKey.trim(), operationTracker );

        registerActiveWorkflow( environment, sshKeyRemovalWorkflow );

        sshKeyRemovalWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                removeActiveWorkflow( environment.getId() );
            }
        } );

        //wait
        if ( !async )
        {
            sshKeyRemovalWorkflow.join();

            if ( sshKeyRemovalWorkflow.isFailed() )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( sshKeyRemovalWorkflow.getError() ) );
            }
        }

        environmentAdapter.removeSshKey( environmentId, sshKey );
    }


    @Override
    public SshKeys getSshKeys( final String environmentId, final SshEncryptionType encType )
    {
        SshKeys sshKeys = new SshKeys();
        try
        {
            Environment environment = loadEnvironment( environmentId );

            for ( Peer peer : environment.getPeers() )
            {
                SshKeys keys = peer.getSshKeys( environment.getEnvironmentId(), encType );
                sshKeys.addKeys( keys.getKeys() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
        return sshKeys;
    }


    @Override
    public SshKeys createSshKey( final String environmentId, final String hostname, final SshEncryptionType encType )
    {
        SshKeys sshKeys = new SshKeys();
        try
        {
            Environment environment = loadEnvironment( environmentId );

            ContainerHost host = environment.getContainerHostByHostname( hostname );
            SshKey sshKey =
                    host.getPeer().createSshKey( environment.getEnvironmentId(), host.getContainerId(), encType );
            sshKeys.addKey( sshKey );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new WebApplicationException( e.getMessage() );
        }
        return sshKeys;
    }


    @Override
    public void resetP2PSecretKey( final String environmentId, final String newP2pSecretKey,
                                   final long p2pSecretKeyTtlSec, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newP2pSecretKey ), "Invalid p2p secret key" );
        Preconditions.checkArgument( p2pSecretKeyTtlSec > 0, "Invalid p2p secret key time-to-live" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Resetting p2p secret key for environment %s ", environment.getName() ) );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION
                || environment.getStatus() == EnvironmentStatus.CANCELLED )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final P2PSecretKeyModificationWorkflow p2PSecretKeyModificationWorkflow =
                getP2PSecretKeyModificationWorkflow( environment, newP2pSecretKey, p2pSecretKeyTtlSec,
                        operationTracker );

        registerActiveWorkflow( environment, p2PSecretKeyModificationWorkflow );

        p2PSecretKeyModificationWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                removeActiveWorkflow( environment.getId() );
            }
        } );

        //wait
        if ( !async )
        {
            p2PSecretKeyModificationWorkflow.join();

            if ( p2PSecretKeyModificationWorkflow.isFailed() )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( p2PSecretKeyModificationWorkflow.getError() ) );
            }
        }
    }


    @Override
    public void destroyEnvironment( final String environmentId, final boolean async )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        LocalEnvironment environment;

        try
        {
            environment = ( LocalEnvironment ) loadEnvironment( environmentId );
        }
        catch ( EnvironmentNotFoundException e )
        {
            // try to get remote environment
            environment = findRemoteEnvironment( environmentId );

            if ( environment == null )
            {
                throw e;
            }
        }

        // If environment from Hub, send destroy request to Hub
        if ( environment instanceof HubEnvironment )
        {
            environmentAdapter.removeEnvironment( environment );

            notifyOnEnvironmentDestroyed( environmentId );

            return;
        }
        else if ( environment instanceof RemoteEnvironment )
        {
            try
            {
                peerManager.getLocalPeer().cleanupEnvironment( environment.getEnvironmentId() );
            }
            catch ( PeerException e )
            {
                throw new EnvironmentDestructionException( e );
            }

            // notify initiator peer to exclude this peer from the environment
            RemotePeer initiatorPeer =
                    peerManager.findPeer( ( ( RemoteEnvironment ) environment ).getInitiatorPeerId() );

            if ( initiatorPeer != null )
            {
                try
                {
                    initiatorPeer.excludePeerFromEnvironment( environment.getId(), peerManager.getLocalPeer().getId() );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error excluding local peer from remote environment: {}", e.getMessage() );
                }
            }

            notifyOnEnvironmentDestroyed( environmentId );

            return;
        }

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Destroying environment %s", environment.getName() ) );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentDestructionException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final EnvironmentDestructionWorkflow environmentDestructionWorkflow =
                getEnvironmentDestructionWorkflow( environment, operationTracker );

        registerActiveWorkflow( environment, environmentDestructionWorkflow );

        environmentDestructionWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    loadEnvironment( environmentId );
                }
                catch ( EnvironmentNotFoundException e )
                {
                    notifyOnEnvironmentDestroyed( environmentId );
                }

                removeActiveWorkflow( environmentId );
            }
        } );

        //wait
        if ( !async )
        {
            environmentDestructionWorkflow.join();

            if ( environmentDestructionWorkflow.isFailed() )
            {
                throw new EnvironmentDestructionException(
                        exceptionUtil.getRootCause( environmentDestructionWorkflow.getError() ) );
            }
        }
    }


    @Override
    public void destroyContainer( final String environmentId, final String containerId, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ), "Invalid container id" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        if ( environment instanceof HubEnvironment )
        {
            environmentAdapter.destroyContainer( ( HubEnvironment ) environment, containerId );

            return;
        }

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        ContainerHost environmentContainer;
        try
        {
            environmentContainer = environment.getContainerHostById( containerId );
        }
        catch ( ContainerHostNotFoundException e )
        {
            throw new EnvironmentModificationException( e );
        }

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Destroying container %s", environmentContainer.getHostname() ) );

        final ContainerDestructionWorkflow containerDestructionWorkflow =
                getContainerDestructionWorkflow( environment, environmentContainer, operationTracker );

        registerActiveWorkflow( environment, containerDestructionWorkflow );

        containerDestructionWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                removeActiveWorkflow( environment.getId() );
            }
        } );

        //wait
        if ( !async )
        {
            containerDestructionWorkflow.join();

            if ( containerDestructionWorkflow.isFailed() )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( containerDestructionWorkflow.getError() ) );
            }
        }
    }


    @Override
    public void changeContainerHostname( final ContainerId containerId, final String newHostname, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ), "Invalid hostname" );

        final LocalEnvironment environment =
                ( LocalEnvironment ) loadEnvironment( containerId.getEnvironmentId().getId() );

        //check that container exists in the environment
        environment.getContainerHostById( containerId.getId() );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Changing container hostname(s) in environment %s", environment.getName() ) );

        final HostnameModificationWorkflow hostnameModificationWorkflow =
                getHostnameModificationWorkflow( environment, containerId, newHostname, operationTracker );

        registerActiveWorkflow( environment, hostnameModificationWorkflow );

        hostnameModificationWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                removeActiveWorkflow( environment.getId() );
            }
        } );

        //wait
        if ( !async )
        {
            hostnameModificationWorkflow.join();

            if ( hostnameModificationWorkflow.isFailed() )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( hostnameModificationWorkflow.getError() ) );
            }
        }
    }


    @Override
    public String createTemplate( final String environmentId, final String containerId, final String templateName,
                                  final boolean privateTemplate ) throws PeerException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        String kurjunToken = identityManager.getActiveSession().getKurjunToken();
        Preconditions.checkNotNull( kurjunToken, "Kurjun token is missing or expired" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        //check that container exists in the environment
        EnvironmentContainerHost containerHost = environment.getContainerHostById( containerId );

        List<Template> ownerTemplates =
                templateManager.getTemplatesByOwner( identityManager.getActiveUser().getFingerprint() );

        for ( Template template : ownerTemplates )
        {
            if ( template.getName().equalsIgnoreCase( templateName ) )
            {
                throw new IllegalStateException(
                        String.format( "Template with name %s already exists in your repository", templateName ) );
            }
        }

        Peer targetPeer = containerHost.getPeer();

        targetPeer.promoteTemplate( containerHost.getContainerId(), templateName );

        return targetPeer.exportTemplate( containerHost.getContainerId(), templateName, privateTemplate, kurjunToken );
    }


    protected void registerActiveWorkflow( Environment environment, CancellableWorkflow newWorkflow )
    {
        Preconditions.checkNotNull( environment );
        Preconditions.checkNotNull( newWorkflow );

        CancellableWorkflow checkWorkflow = activeWorkflows.get( environment.getId() );

        if ( checkWorkflow != null )
        {
            throw new IllegalStateException( String.format( "There is already an active workflow %s for environment %s",
                    checkWorkflow.getClass().getSimpleName(), environment.getName() ) );
        }

        activeWorkflows.put( environment.getId(), newWorkflow );
    }


    protected void removeActiveWorkflow( String environmentId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        activeWorkflows.remove( environmentId );
    }


    @Override
    public void cancelEnvironmentWorkflow( final String environmentId ) throws EnvironmentManagerException
    {
        try
        {
            CancellableWorkflow activeWorkflow = activeWorkflows.get( environmentId );

            if ( activeWorkflow != null )
            {
                activeWorkflow.cancel();

                removeActiveWorkflow( environmentId );
            }
            else
            {
                LocalEnvironment environment = environmentService.find( environmentId );

                if ( environment != null )
                {
                    environment.setStatus( EnvironmentStatus.CANCELLED );

                    update( environment );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error cancelling environment workflow: {}", e.getMessage() );

            throw new EnvironmentManagerException(
                    String.format( "Error cancelling environment workflow %s", e.getMessage() ) );
        }
    }


    @Override
    public Map<String, CancellableWorkflow> getActiveWorkflows()
    {
        return Collections.unmodifiableMap( activeWorkflows );
    }


    @Override
    public Environment loadEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        // First get from Hub
        LocalEnvironment environment = environmentAdapter.get( environmentId );

        if ( environment != null )
        {
            return environment;
        }

        // try to get local environment
        environment = environmentService.find( environmentId );

        if ( environment != null )
        {
            setTransientFields( Sets.<Environment>newHashSet( environment ) );

            return environment;
        }

        throw new EnvironmentNotFoundException();
    }


    LocalEnvironment findRemoteEnvironment( String environmentId )
    {
        try
        {
            NetworkResource networkResource =
                    peerManager.getLocalPeer().getReservedNetworkResources().findByEnvironmentId( environmentId );

            if ( networkResource != null && !peerManager.getLocalPeer().getId()
                                                        .equals( networkResource.getInitiatorPeerId() ) )
            {
                RemotePeer initiatorPeer = peerManager.findPeer( networkResource.getInitiatorPeerId() );

                return new RemoteEnvironment( networkResource, String.format( "Of %s",
                        initiatorPeer == null ? networkResource.getInitiatorPeerId() : initiatorPeer.getName() ),
                        peerManager.getLocalPeer().findContainersByEnvironmentId( environmentId ) );
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error finding remote environment: {}", e.getMessage() );
        }

        return null;
    }


    @Override
    public void removeEnvironmentDomain( final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        modifyEnvironmentDomain( environmentId, null, null, null );
    }


    @Override
    public void assignEnvironmentDomain( final String environmentId, final String newDomain,
                                         final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newDomain ), "Invalid domain" );
        Preconditions.checkArgument( newDomain.matches( Common.HOSTNAME_REGEX ), "Invalid domain" );
        Preconditions.checkNotNull( proxyLoadBalanceStrategy );

        modifyEnvironmentDomain( environmentId, newDomain, proxyLoadBalanceStrategy, sslCertPath );
    }


    void modifyEnvironmentDomain( final String environmentId, final String domain,
                                  final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy, final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        boolean assign = !Strings.isNullOrEmpty( domain );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Modifying domain for environment %s", environment.getName() ) );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION
                || environment.getStatus() == EnvironmentStatus.CANCELLED )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }
        try
        {
            if ( assign )
            {
                peerManager.getLocalPeer()
                           .setVniDomain( environment.getVni(), domain, proxyLoadBalanceStrategy, sslCertPath );
            }
            else
            {
                peerManager.getLocalPeer().removeVniDomain( environment.getVni() );
            }

            operationTracker.addLogDone( "Environment domain modified" );
        }
        catch ( Exception e )
        {
            operationTracker.addLogFailed( String.format( "Error modifying environment domain: %s", e.getMessage() ) );
            throw new EnvironmentModificationException( e );
        }
    }


    @Override
    public String getEnvironmentDomain( final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        try
        {
            return peerManager.getLocalPeer().getVniDomain( environment.getVni() );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentManagerException( "Error obtaining environment domain", e );
        }
    }


    @Override
    public boolean isContainerInEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        try
        {
            EnvironmentContainerHost containerHost = environment.getContainerHostById( containerHostId );

            return peerManager.getLocalPeer()
                              .isIpInVniDomain( containerHost.getIp() + ":" + containerHost.getDomainPort(),
                                      environment.getVni() );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentManagerException( "Error checking container domain", e );
        }
    }

    //************ utility methods


    @Override
    public void addContainerToEnvironmentDomain( final String containerHostId, final String environmentId, int port )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( port, Common.MIN_PORT, Common.MAX_PORT ) );
        toggleContainerDomain( containerHostId, environmentId, port, true );
    }


    @Override
    public void removeContainerFromEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {

        toggleContainerDomain( containerHostId, environmentId, -1, false );
    }


    public void toggleContainerDomain( final String containerHostId, final String environmentId, int port,
                                       final boolean add )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        EnvironmentContainerHost containerHost = environment.getContainerHostById( containerHostId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "%s container %s environment domain", add ? "Adding" : "Removing",
                        containerHost.getHostname() ) );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION
                || environment.getStatus() == EnvironmentStatus.CANCELLED )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }
        try
        {
            if ( add )
            {
                peerManager.getLocalPeer().addIpToVniDomain( containerHost.getIp() + ":" + port, environment.getVni() );

                ( ( EnvironmentContainerImpl ) containerHost ).setDomainPort( port );
            }
            else
            {
                peerManager.getLocalPeer()
                           .removeIpFromVniDomain( containerHost.getIp() + ":" + containerHost.getDomainPort(),
                                   environment.getVni() );

                ( ( EnvironmentContainerImpl ) containerHost ).setDomainPort( null );
            }

            update( ( EnvironmentContainerImpl ) containerHost );

            operationTracker.addLogDone(
                    String.format( "Container is %s environment domain", add ? "included in" : "excluded from" ) );
        }
        catch ( Exception e )
        {
            operationTracker.addLogFailed( String.format( "Error %s environment domain: %s",
                    add ? "including container in" : "excluding container from", e.getMessage() ) );
            throw new EnvironmentModificationException( e );
        }
    }


    @Override
    public SshTunnel setupSshTunnelForContainer( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        EnvironmentContainerHost environmentContainer = environment.getContainerHostById( containerHostId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Setting up ssh tunnel for container %s ", environmentContainer.getHostname() ) );

        try
        {
            SshTunnel sshTunnel = peerManager.getLocalPeer().setupSshTunnelForContainer( environmentContainer.getIp(),
                    Common.CONTAINER_SSH_TIMEOUT_SEC );

            operationTracker.addLogDone(
                    String.format( "Ssh for container %s is ready on tunnel %s", environmentContainer.getHostname(),
                            sshTunnel ) );

            return sshTunnel;
        }
        catch ( Exception e )
        {
            operationTracker.addLogFailed(
                    String.format( "Error setting up ssh for container %s: %s", environmentContainer.getHostname(),
                            e.getMessage() ) );
            throw new EnvironmentModificationException( e );
        }
    }


    PGPSecretKeyRing createEnvironmentKeyPair( EnvironmentId envId ) throws EnvironmentCreationException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        String pairId = envId.getId();
        try
        {
            KeyPair keyPair = keyManager.generateKeyPair( pairId, false );

            //******Create PEK *****************************************************************
            PGPSecretKeyRing secRing = pgpKeyUtil.getSecretKeyRing( keyPair.getSecKeyring() );
            PGPPublicKeyRing pubRing = pgpKeyUtil.getPublicKeyRing( keyPair.getPubKeyring() );

            //***************Save Keys *********************************************************
            keyManager.saveSecretKeyRing( pairId, SecurityKeyType.ENVIRONMENT_KEY.getId(), secRing );
            keyManager.savePublicKeyRing( pairId, SecurityKeyType.ENVIRONMENT_KEY.getId(), pubRing );


            return secRing;
        }
        catch ( PGPException ex )
        {
            throw new EnvironmentCreationException( ex );
        }
    }

    //-- workflow factories start


    protected P2PSecretKeyModificationWorkflow getP2PSecretKeyModificationWorkflow( final LocalEnvironment environment,
                                                                                    final String p2pSecretKey,
                                                                                    final long p2pSecretKeyTtlSec,
                                                                                    final TrackerOperation
                                                                                            operationTracker )
    {
        return new P2PSecretKeyModificationWorkflow( environment, p2pSecretKey, p2pSecretKeyTtlSec, operationTracker,
                this );
    }


    protected SshKeyAdditionWorkflow getSshKeyAdditionWorkflow( final LocalEnvironment environment, final String sshKey,

                                                                final TrackerOperation operationTracker )
    {
        return new SshKeyAdditionWorkflow( environment, sshKey, operationTracker, this );
    }


    protected SshKeyRemovalWorkflow getSshKeyRemovalWorkflow( final LocalEnvironment environment, final String sshKey,
                                                              final TrackerOperation operationTracker )
    {
        return new SshKeyRemovalWorkflow( environment, sshKey, operationTracker, this );
    }


    protected ContainerDestructionWorkflow getContainerDestructionWorkflow( final LocalEnvironment environment,
                                                                            final ContainerHost containerHost,
                                                                            final TrackerOperation operationTracker )
    {
        return new ContainerDestructionWorkflow( this, environment, containerHost, operationTracker );
    }


    protected EnvironmentCreationWorkflow getEnvironmentCreationWorkflow( final LocalEnvironment environment,
                                                                          final Topology topology, final String sshKey,
                                                                          final TrackerOperation operationTracker )
    {
        return new EnvironmentCreationWorkflow( Common.DEFAULT_DOMAIN_NAME, identityManager, this, peerManager,
                securityManager, environment, topology, sshKey, operationTracker );
    }


    protected EnvironmentModifyWorkflow getEnvironmentModifyingWorkflow( final LocalEnvironment environment,
                                                                         final Topology topology,
                                                                         final TrackerOperation operationTracker,
                                                                         final List<String> removedContainers,
                                                                         final Map<String, ContainerQuota>
                                                                                 changedContainers )

    {
        return new EnvironmentModifyWorkflow( Common.DEFAULT_DOMAIN_NAME, identityManager, peerManager, securityManager,
                environment, topology, removedContainers, changedContainers, operationTracker, this );
    }


    protected EnvironmentDestructionWorkflow getEnvironmentDestructionWorkflow( final LocalEnvironment environment,
                                                                                final TrackerOperation
                                                                                        operationTracker )
    {
        return new EnvironmentDestructionWorkflow( this, environment, operationTracker );
    }


    protected HostnameModificationWorkflow getHostnameModificationWorkflow( final LocalEnvironment environment,
                                                                            final ContainerId containerId,
                                                                            final String newHostname,
                                                                            final TrackerOperation operationTracker )
    {
        return new HostnameModificationWorkflow( environment, containerId, newHostname, operationTracker, this );
    }


    //-- workflow factories end


    public void registerListener( final EnvironmentEventListener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    public void unregisterListener( final EnvironmentEventListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
    }


    public void notifyOnEnvironmentCreated( final Environment environment )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onEnvironmentCreated( environment );
                }
            } );
        }
    }


    public void notifyOnEnvironmentGrown( final Environment environment,
                                          final Set<EnvironmentContainerHost> containers )
    {
        if ( !containers.isEmpty() )
        {
            for ( final EnvironmentEventListener listener : listeners )
            {
                executor.submit( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onEnvironmentGrown( environment, containers );
                    }
                } );
            }
        }
    }


    @Override
    public void notifyOnContainerDestroyed( final Environment environment, final String containerId )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onContainerDestroyed( environment, containerId );
                }
            } );
        }
    }


    @Override
    public void notifyOnEnvironmentDestroyed( final String environmentId )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onEnvironmentDestroyed( environmentId );
                }
            } );
        }
    }


    public void notifyOnContainerStarted( final Environment environment, final String containerId )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onContainerStarted( environment, containerId );
                }
            } );
        }
    }


    public void notifyOnContainerStopped( final Environment environment, final String containerId )
    {
        for ( final EnvironmentEventListener listener : listeners )
        {
            executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onContainerStopped( environment, containerId );
                }
            } );
        }
    }


    protected Long getUserId()
    {
        return identityManager.getActiveUser().getId();
    }


    public Peer resolvePeer( final String peerId ) throws PeerException
    {
        return peerManager.getPeer( peerId );
    }


    public void save( final LocalEnvironment environment )
    {
        environmentService.persist( environment );

        setTransientFields( Sets.<Environment>newHashSet( environment ) );
    }


    public synchronized LocalEnvironment update( LocalEnvironment environment )
    {
        if ( environment instanceof HubEnvironment )
        {
            // Environment from Hub
            return environment;
        }

        environment = environmentService.merge( environment );

        setTransientFields( Sets.<Environment>newHashSet( environment ) );

        boolean uploaded = environmentAdapter.uploadEnvironment( environment );

        if ( !uploaded )
        {
            environment.markAsNotUploaded();

            environment = environmentService.merge( environment );

            setTransientFields( Sets.<Environment>newHashSet( environment ) );
        }

        return environment;
    }


    public void remove( final LocalEnvironment environment )
    {
        if ( !environmentAdapter.isRegisteredWithHub() || environmentAdapter.removeEnvironment( environment ) )
        {
            environmentService.remove( environment.getId() );
        }
        else
        {
            environment.markAsDeleted();

            environmentService.merge( environment );
        }
    }


    public synchronized EnvironmentContainerImpl update( final EnvironmentContainerImpl container )
    {
        Environment environment = container.getEnvironment();

        EnvironmentContainerImpl envContainer = environmentService.mergeContainer( container );

        envContainer.setEnvironmentManager( this );

        //update cache
        ( ( LocalEnvironment ) environment ).removeContainer( envContainer );
        ( ( LocalEnvironment ) environment ).addContainers( Sets.newHashSet( envContainer ) );

        return envContainer;
    }


    @Override
    public void addAlertHandler( final AlertHandler alertHandler )
    {
        if ( alertHandler != null && alertHandler.getId() != null )
        {
            this.alertHandlers.put( alertHandler.getId(), alertHandler );
        }
        else
        {
            LOG.warn( "Alert handler rejected: " + alertHandler );
        }
    }


    @Override
    public void removeAlertHandler( final AlertHandler alertHandler )
    {
        if ( alertHandler != null )
        {
            this.alertHandlers.remove( alertHandler.getId() );
        }
    }


    @Override
    public Collection<AlertHandler> getRegisteredAlertHandlers()
    {
        return new ArrayList<>( alertHandlers.values() );
    }


    @Override
    public String getId()
    {
        return "ENVIRONMENT_MANAGER";
    }


    @Override
    public void onAlert( final AlertEvent alertEvent )
    {
        try
        {
            EnvironmentAlertHandlers handlers =
                    getEnvironmentAlertHandlers( new EnvironmentId( alertEvent.getEnvironmentId() ) );
            handleAlertPack( alertEvent, handlers );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in handling alert package.", e );
        }
    }


    @Override
    public EnvironmentAlertHandlers getEnvironmentAlertHandlers( final EnvironmentId environmentId )
            throws EnvironmentNotFoundException
    {
        Environment environment = loadEnvironment( environmentId.getId() );

        Set<EnvironmentAlertHandler> handlerList = environment.getAlertHandlers();

        EnvironmentAlertHandlers handlers = new EnvironmentAlertHandlersImpl( environment.getEnvironmentId() );
        for ( EnvironmentAlertHandler environmentAlertHandler : handlerList )
        {
            handlers.add( environmentAlertHandler, alertHandlers.get( environmentAlertHandler.getAlertHandlerId() ) );
        }
        return handlers;
    }


    protected void handleAlertPack( final AlertEvent alertEvent, EnvironmentAlertHandlers handlers )
    {
        for ( final EnvironmentAlertHandler handlerId : handlers.getAllHandlers().keySet() )
        {
            try
            {
                AlertHandler handler = handlers.getHandler( handlerId );
                if ( handler == null )
                {
                    alertEvent.addLog( String.format( "Alert Handler not found: %s. Skipped.", handlerId ) );
                    continue;
                }
                AlertValue alertValue = alertEvent.getResource().getAlertValue( handler.getSupportedAlertValue() );
                if ( alertValue != null && alertEvent.getEnvironmentId() != null )
                {
                    final Environment environment = loadEnvironment( alertEvent.getEnvironmentId() );
                    alertEvent.addLog(
                            String.format( "Invoking pre-processor of '%s:%s'.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    handler.preProcess( environment, alertValue );
                    alertEvent.addLog(
                            String.format( "Pre-processor of '%s:%s' finished.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    alertEvent.addLog(
                            String.format( "Invoking main processor of '%s:%s'.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    handler.process( environment, alertValue );
                    alertEvent.addLog(
                            String.format( "Main processor of '%s:%s' finished.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    alertEvent.addLog(
                            String.format( "Invoking post-processor of '%s:%s'.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    handler.postProcess( environment, alertValue );
                    alertEvent.addLog(
                            String.format( "Pre-processor of '%s:%s' finished.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                }
            }
            catch ( Exception e )
            {
                alertEvent.addLog( e.getMessage() );
            }
        }
    }


    @Override
    public void startMonitoring( final String handlerId, final AlertHandlerPriority handlerPriority,
                                 final String environmentId ) throws EnvironmentManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( handlerId ), "Invalid alert handler id." );
        Preconditions.checkNotNull( handlerPriority, "Invalid alert priority." );

        AlertHandler alertHandler = alertHandlers.get( handlerId );
        if ( alertHandler == null )
        {
            throw new EnvironmentManagerException( "Alert handler not found." );
        }
        try
        {
            LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

            environment.addAlertHandler( new EnvironmentAlertHandlerImpl( handlerId, handlerPriority ) );

            update( environment );
        }
        catch ( Exception e )
        {
            LOG.error( "Error on start monitoring", e );
            throw new EnvironmentManagerException( e.getMessage(), e );
        }
    }


    @Override
    public void stopMonitoring( final String handlerId, final AlertHandlerPriority handlerPriority,
                                final String environmentId ) throws EnvironmentManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( handlerId ), "Invalid alert handler id." );
        Preconditions.checkNotNull( handlerPriority, "Invalid alert priority." );

        //remove subscription from database
        try
        {
            LocalEnvironment environment = environmentService.find( environmentId );
            environment.removeAlertHandler( new EnvironmentAlertHandlerImpl( handlerId, handlerPriority ) );
            update( environment );
        }
        catch ( Exception e )
        {
            LOG.error( "Error on E monitoring", e );
            throw new EnvironmentManagerException( e.getMessage(), e );
        }
    }


    @Override
    public void onPluginEvent( final String pluginUid, final PeerProductDataDto.State state )
    {
        LOG.info( "Plugin event, id: {}, state: {}", pluginUid, state );
    }


    @Override
    public void addSshKeyToEnvironmentEntity( final String environmentId, final String sshKey )
            throws EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshKey ), "Invalid ssh key" );

        LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        environment.addSshKey( sshKey );

        update( environment );
    }


    @Override
    public void excludePeerFromEnvironment( final String environmentId, final String peerId )
            throws EnvironmentNotFoundException
    {
        LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        Set<EnvironmentContainerHost> peerContainers = environment.getContainerHostsByPeerId( peerId );

        EnvironmentPeer environmentPeer = environment.getEnvironmentPeer( peerId );

        destroyTunnelToPeer( environmentPeer, environment );

        environment.excludePeerFromEnvironment( peerId );

        if ( environment.getEnvironmentPeers().isEmpty() )
        {
            remove( environment );

            notifyOnEnvironmentDestroyed( environmentId );

            relationManager.removeRelation( environment );

            cleanupEnvironment( environment.getEnvironmentId() );
        }
        else
        {
            update( environment );

            for ( EnvironmentContainerHost containerHost : peerContainers )
            {
                notifyOnContainerDestroyed( environment, containerHost.getId() );
            }
        }
    }


    @Override
    public void excludeContainerFromEnvironment( final String environmentId, final String containerId )
            throws EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        EnvironmentContainerHost containerHost = environment.getContainerHostById( containerId );

        environment.removeContainer( containerHost );

        if ( environment.getContainerHostsByPeerId( containerHost.getPeerId() ).isEmpty() )
        {
            EnvironmentPeer environmentPeer = environment.getEnvironmentPeer( containerHost.getPeerId() );

            environment.removeEnvironmentPeer( containerHost.getPeerId() );

            destroyTunnelToPeer( environmentPeer, environment );
        }

        if ( environment.getEnvironmentPeers().isEmpty() )
        {
            remove( environment );

            notifyOnEnvironmentDestroyed( environmentId );

            relationManager.removeRelation( environment );

            cleanupEnvironment( environment.getEnvironmentId() );
        }
        else
        {
            update( environment );

            notifyOnContainerDestroyed( environment, containerId );
        }

        relationManager.removeRelation( containerHost );
    }


    @Override
    public void updateContainerHostname( final String environmentId, final String containerId, final String hostname )
            throws EnvironmentNotFoundException, PeerException
    {
        final LocalEnvironment environment = ( LocalEnvironment ) loadEnvironment( environmentId );

        EnvironmentContainerImpl containerHost =
                ( EnvironmentContainerImpl ) environment.getContainerHostById( containerId );

        String oldHostname = containerHost.getHostname();

        if ( oldHostname.equalsIgnoreCase( hostname ) )
        {
            return;
        }

        containerHost.setHostname( hostname, true );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Propagating container hostname change in environment %s", environment.getName() ) );

        final HostnameModificationWorkflow hostnameModificationWorkflow =
                new HostnameModificationWorkflow( environment, containerHost.getContainerId(), hostname,
                        operationTracker, this, oldHostname );

        registerActiveWorkflow( environment, hostnameModificationWorkflow );

        hostnameModificationWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                removeActiveWorkflow( environment.getId() );
            }
        } );
    }


    @Override
    public Set<EnvironmentDto> getTenantEnvironments()
    {
        Set<Environment> environments = Sets.newHashSet();

        // add local env-s
        environments.addAll( environmentService.getAll() );

        try
        {
            // add hub env-s
            Set<HubEnvironment> hubEnvironments = environmentAdapter.getEnvironments( true );

            // remove environments that exist on Hub but don't exist on peer
            // workaround for https://github.com/subutai-io/base/issues/1464
            removeStaleHubEnvironments( hubEnvironments );

            environments.addAll( hubEnvironments );

            // add remote env-s
            environments.addAll( getRemoteEnvironments( true ) );
        }
        catch ( ActionFailedException e )
        {
            //failed to obtain Hub metadata, return all locally registered env-s
            environments.addAll( getRemoteEnvironments( true ) );
        }

        Set<EnvironmentDto> environmentDtos = Sets.newHashSet();

        for ( Environment environment : environments )
        {
            EnvironmentDto environmentDto =
                    new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                            environment.getContainerDtos(),
                            environment instanceof HubEnvironment ? Common.HUB_ID : Common.SUBUTAI_ID,
                            getEnvironmentOwnerName( environment ) );

            environmentDtos.add( environmentDto );
        }

        return environmentDtos;
    }


    @Override
    public String getEnvironmentOwnerName( Environment environment )
    {
        if ( environment instanceof RemoteEnvironment )
        {
            RemoteEnvironment remoteEnvironment = ( RemoteEnvironment ) environment;

            boolean hubEnv = Objects.equals( remoteEnvironment.getInitiatorPeerId(), Common.HUB_ID );

            if ( hubEnv )
            {
                return remoteEnvironment.getUsername() == null ? Common.HUB_ID :
                       String.format( "%s@%s", remoteEnvironment.getUsername(), remoteEnvironment.getRemoteUserId() );
            }
            else
            {
                return remoteEnvironment.getUsername() == null ? REMOTE_OWNER_NAME : remoteEnvironment.getUsername();
            }
        }
        else if ( environment instanceof HubEnvironment )
        {
            HubEnvironment hubEnvironment = ( ( HubEnvironment ) environment );
            return String.format( "%s@%s", hubEnvironment.getOwner(), hubEnvironment.getOwnerHubId() );
        }

        User user = ServiceLocator.lookup( IdentityManager.class ).getUser( environment.getUserId() );

        if ( user == null )
        {
            return UKNOWN_OWNER_NAME;
        }
        else
        {
            return user.getUserName();
        }
    }


    @Override
    public void onRegistrationSucceeded()
    {
        uploadPeerOwnerEnvironmentsToHub();
    }


    @Override
    public void onUnregister()
    {
        Set<LocalEnvironment> envs = new HashSet<>();

        envs.addAll( environmentService.getAll() );

        for ( Iterator<LocalEnvironment> iterator = envs.iterator(); iterator.hasNext(); )
        {
            LocalEnvironment environment = iterator.next();

            if ( environment.isUploaded() )
            {
                environment.markAsNotUploaded();

                environmentService.merge( environment );
            }
        }
    }


    // remove environments that exist on Hub but don't exist on peer
    // workaround for https://github.com/subutai-io/base/issues/1464
    private void removeStaleHubEnvironments( Set<HubEnvironment> hubEnvironments )
    {
        try
        {
            ReservedNetworkResources networkResources = peerManager.getLocalPeer().getReservedNetworkResources();

            for ( Iterator<HubEnvironment> iterator = hubEnvironments.iterator(); iterator.hasNext(); )
            {
                HubEnvironment environment = iterator.next();

                if ( networkResources.findByEnvironmentId( environment.getId() ) == null )
                {
                    iterator.remove();
                }
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error removing stale Hub environments: {}", e.getMessage() );
        }
    }


    private Set<RemoteEnvironment> getRemoteEnvironments( boolean includeHubEnvironments )
    {
        Set<RemoteEnvironment> remoteEnvironments = Sets.newHashSet();

        try
        {
            ReservedNetworkResources networkResources = peerManager.getLocalPeer().getReservedNetworkResources();

            for ( NetworkResource networkResource : networkResources.getNetworkResources() )
            {
                // exclude local reservations
                if ( !peerManager.getLocalPeer().getId().equals( networkResource.getInitiatorPeerId() ) )
                {
                    if ( !includeHubEnvironments && Common.HUB_ID.equals( networkResource.getInitiatorPeerId() ) )
                    {
                        continue;
                    }

                    RemotePeer initiatorPeer = networkResource.getInitiatorPeerId() == null ? null :
                                               peerManager.findPeer( networkResource.getInitiatorPeerId() );

                    remoteEnvironments.add( new RemoteEnvironment( networkResource, String.format( "Of %s",
                            initiatorPeer == null ? networkResource.getInitiatorPeerId() : initiatorPeer.getName() ),
                            peerManager.getLocalPeer()
                                       .findContainersByEnvironmentId( networkResource.getEnvironmentId() ) ) );
                }
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error getting remote environments: {}", e.getMessage() );
        }

        return remoteEnvironments;
    }


    private Set<RemoteEnvironment> getLocallyRegisteredHubEnvironments()
    {
        Set<RemoteEnvironment> hubEnvironments = Sets.newHashSet();

        try
        {
            ReservedNetworkResources networkResources = peerManager.getLocalPeer().getReservedNetworkResources();

            for ( NetworkResource networkResource : networkResources.getNetworkResources() )
            {
                if ( Common.HUB_ID.equals( networkResource.getInitiatorPeerId() ) )
                {
                    hubEnvironments.add( new RemoteEnvironment( networkResource, Common.HUB_ID,
                            peerManager.getLocalPeer()
                                       .findContainersByEnvironmentId( networkResource.getEnvironmentId() ) ) );
                }
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error getting locally registered Hub environments: {}", e.getMessage() );
        }

        return hubEnvironments;
    }


    private class BackgroundTasksRunner implements Runnable
    {
        @Override
        public void run()
        {
            LOG.debug( "Environment background tasks started..." );

            syncEnvironments();

            resetP2pKeys();

            LOG.debug( "Environment background tasks finished." );
        }
    }


    //todo run in a thread
    protected void uploadPeerOwnerEnvironmentsToHub()
    {
        //0. check if peer is registered with Hub and Hub is reachable
        if ( !environmentAdapter.canWorkWithHub() )
        {
            return;
        }


        //1. obtain peer owner
        User peerOwner = identityManager.getUserByKeyId( identityManager.getPeerOwnerId() );


        //2. filter out not peer owner's environments or uploaded environments
        Set<LocalEnvironment> envs = new HashSet<>();

        envs.addAll( environmentService.getAll() );

        for ( Iterator<LocalEnvironment> iterator = envs.iterator(); iterator.hasNext(); )
        {
            final LocalEnvironment environment = iterator.next();

            if ( environment.isUploaded() || !Objects.equals( environment.getUserId(), peerOwner.getId() ) )
            {
                iterator.remove();
            }
        }

        if ( envs.isEmpty() )
        {
            return;
        }


        //3. upload them to Hub
        Set<Environment> environments = Sets.newHashSet();

        environments.addAll( envs );

        setTransientFields( environments );

        for ( LocalEnvironment environment : envs )
        {
            try
            {
                if ( environmentAdapter.uploadPeerOwnerEnvironment( environment ) )
                {
                    environment.markAsUploaded();

                    environmentService.merge( environment );
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error uploading environment {} to Hub: {}", environment.getName(), e.getMessage() );
            }
        }
    }


    private void resetP2pKeys()
    {
        if ( System.currentTimeMillis() - lastP2pSecretKeyResetTs >= TimeUnit.MINUTES
                .toMillis( RESET_ENVS_P2P_KEYS_INTERVAL_MIN ) )
        {
            lastP2pSecretKeyResetTs = System.currentTimeMillis();

            Subject.doAs( systemUser, new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    doResetP2Pkeys();
                    return null;
                }
            } );
        }
    }


    protected void doResetP2Pkeys()
    {
        try
        {
            //process only SS side environments
            for ( Environment environment : environmentService.getAll() )
            {
                if ( !( environment.getStatus() != EnvironmentStatus.HEALTHY || (
                        ( System.currentTimeMillis() - environment.getCreationTimestamp() ) < TimeUnit.MINUTES
                                .toMillis( RESET_ENVS_P2P_KEYS_INTERVAL_MIN ) ) ) )
                {
                    final String secretKey = UUID.randomUUID().toString();
                    final long keyTtl = Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC;
                    resetP2PSecretKey( environment.getId(), secretKey, keyTtl, true );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage() );
        }
    }


    private void syncEnvironments()
    {
        if ( System.currentTimeMillis() - lastEnvSyncTs >= TimeUnit.MINUTES
                .toMillis( SYNC_ENVS_WITH_HUB_INTERVAL_MIN ) )
        {
            lastEnvSyncTs = System.currentTimeMillis();

            Subject.doAs( systemUser, new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    uploadPeerOwnerEnvironmentsToHub();

                    doSyncEnvironments();

                    return null;
                }
            } );
        }
    }


    //todo run in a thread
    private void doSyncEnvironments()
    {
        if ( !environmentAdapter.canWorkWithHub() )
        {
            return;
        }

        try
        {
            Set<HubEnvironment> environmentsObtainedFromHub = environmentAdapter.getEnvironments( true );

            Set<RemoteEnvironment> locallyRegisteredHubEnvironments = getLocallyRegisteredHubEnvironments();


            // 1. remove environments on Hub that are missing locally

            Set<Environment> environmentsMissingLocally = Sets.newHashSet();

            for ( Environment hubEnvironment : environmentsObtainedFromHub )
            {
                boolean isMissingLocally = true;

                for ( Environment localEnvironment : locallyRegisteredHubEnvironments )
                {
                    if ( hubEnvironment.getId().equalsIgnoreCase( localEnvironment.getId() ) )
                    {
                        isMissingLocally = false;

                        break;
                    }
                }

                if ( isMissingLocally )
                {
                    environmentsMissingLocally.add( hubEnvironment );
                }
            }

            // remove all missing env-s from Hub

            for ( Environment environment : environmentsMissingLocally )
            {
                environmentAdapter.removeEnvironment( ( LocalEnvironment ) environment );
            }


            // 2. remove local environments that are missing on Hub

            Set<String> deletedEnvironmentsIdsOnHub = environmentAdapter.getDeletedEnvironmentsIds();
            Set<Environment> environmentsMissingOnHub = Sets.newHashSet();

            for ( Environment localEnvironment : locallyRegisteredHubEnvironments )
            {
                boolean isMissingOnHub = false;

                for ( String hubEnvironmentId : deletedEnvironmentsIdsOnHub )
                {
                    if ( localEnvironment.getId().equalsIgnoreCase( hubEnvironmentId ) )
                    {
                        isMissingOnHub = true;

                        break;
                    }
                }

                if ( isMissingOnHub )
                {
                    environmentsMissingOnHub.add( localEnvironment );
                }
            }

            // destroy local env-s missing on Hub

            for ( Environment environment : environmentsMissingOnHub )
            {
                cleanupEnvironment( environment.getEnvironmentId() );

                notifyOnEnvironmentDestroyed( environment.getId() );
            }

            // notify Hub about environment deletion

            for ( String hubEnvironmentId : deletedEnvironmentsIdsOnHub )
            {
                environmentAdapter.removeEnvironment( hubEnvironmentId );
            }

            // 3. Remove deleted local env-s from Hub

            Collection<LocalEnvironment> deletedEnvs = environmentService.getDeleted();

            for ( LocalEnvironment deletedEnvironment : deletedEnvs )
            {
                if ( environmentAdapter.removeEnvironment( deletedEnvironment ) )
                {
                    environmentService.remove( deletedEnvironment.getId() );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, final Set<QuotaAlertValue> alerts )
    {
        // not needed
    }


    @Override
    public void onContainerStateChanged( final ContainerHostInfo containerInfo, final ContainerHostState previousState,
                                         final ContainerHostState currentState )
    {
        // not needed
    }


    @Override
    public void onContainerCreated( final ContainerHostInfo containerInfo )
    {
        // not needed
    }


    @Override
    public void onContainerNetInterfaceChanged( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel oldNetInterface,
                                                final HostInterfaceModel newNetInterface )
    {
        // todo implement
    }


    @Override
    public void onContainerNetInterfaceAdded( final ContainerHostInfo containerInfo,
                                              final HostInterfaceModel netInterface )
    {
        // todo implement
    }


    @Override
    public void onContainerNetInterfaceRemoved( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel netInterface )
    {
        // todo implement
    }


    private ContainerHost getContainerHostById( String containerId )
    {
        try
        {
            return peerManager.getLocalPeer().getContainerHostById( containerId );
        }
        catch ( HostNotFoundException e )
        {
            return null;
        }
    }


    private ContainerHost getContainerHostByIp( String containerIp )
    {
        try
        {
            return peerManager.getLocalPeer().getContainerHostByIp( containerIp );
        }
        catch ( HostNotFoundException e )
        {
            return null;
        }
    }


    private Set<Environment> getLocalEnvironments()
    {
        Set<Environment> environments = Sets.newHashSet();

        environments.addAll( environmentService.getAll() );

        setTransientFields( environments );

        return environments;
    }


    public boolean rhHasEnvironments( String rhId )
    {
        Preconditions.checkArgument( !StringUtils.isBlank( rhId ) );

        for ( EnvironmentDto environment : getTenantEnvironments() )
        {
            for ( ContainerDto container : environment.getContainers() )
            {
                if ( rhId.equalsIgnoreCase( container.getRhId() ) )
                {
                    return true;
                }
            }
        }

        return false;
    }


    @Override
    public void onContainerHostnameChanged( final ContainerHostInfo containerInfo, final String previousHostname,
                                            final String currentHostname )
    {
        boolean environmentFound = false;

        ContainerHost containerHost = getContainerHostById( containerInfo.getId() );

        if ( containerHost == null )
        {
            return;
        }

        Set<Environment> environments = getLocalEnvironments();

        for ( Environment environment : environments )
        {
            try
            {
                EnvironmentContainerImpl environmentContainerHost =
                        ( EnvironmentContainerImpl ) environment.getContainerHostById( containerInfo.getId() );

                environmentFound = true;

                updateContainerHostname( environment.getId(), environmentContainerHost.getId(), currentHostname );
            }
            catch ( ContainerHostNotFoundException e )
            {
                //ignore
            }
            catch ( EnvironmentNotFoundException | PeerException e )
            {
                LOG.error( "Error updating container hostname: {}", e.getMessage() );

                break;
            }
        }

        if ( !environmentFound && !Common.HUB_ID.equals( containerHost.getInitiatorPeerId() ) )
        {
            try
            {
                Peer peer = peerManager.getPeer( containerHost.getInitiatorPeerId() );

                if ( peer instanceof RemotePeer )
                {
                    ( ( RemotePeer ) peer )
                            .updateContainerHostname( containerHost.getEnvironmentId().getId(), containerHost.getId(),
                                    currentHostname );
                }
            }
            catch ( PeerException e )
            {
                LOG.error( "Error updating container hostname on remote peer: {}", e.getMessage() );
            }
        }
        else if ( !environmentFound )
        {
            // Hub environment
            environmentAdapter.handleHostnameChange( containerInfo, previousHostname, currentHostname );
        }
    }


    @Override
    public void onContainerDestroyed( final ContainerHost containerHost )
    {
        boolean environmentFound = false;

        Set<Environment> environments = getLocalEnvironments();

        for ( final Environment environment : environments )
        {
            try
            {
                //remote container metadata
                EnvironmentContainerImpl environmentContainerHost =
                        ( EnvironmentContainerImpl ) environment.getContainerHostById( containerHost.getId() );

                environmentFound = true;

                Environment env = environmentContainerHost.destroy( true );

                //if environment got empty, remove environment metadata
                if ( env.getContainerHosts().isEmpty() )
                {
                    remove( ( LocalEnvironment ) env );

                    notifyOnEnvironmentDestroyed( env.getId() );

                    relationManager.removeRelation( env );

                    Subject.doAs( systemUser, new PrivilegedAction<Void>()
                    {
                        @Override
                        public Void run()
                        {
                            cleanupEnvironment( environment.getEnvironmentId() );

                            return null;
                        }
                    } );
                }
                else
                {
                    notifyOnContainerDestroyed( env, containerHost.getId() );
                }

                //remove security relation
                relationManager.removeRelation( containerHost );

                break;
            }
            catch ( ContainerHostNotFoundException e )
            {
                // ignore
            }
            catch ( PeerException e )
            {
                LOG.error( "Error processing container destroy event: {}", e.getMessage() );

                break;
            }
        }

        //process an x-peer environment
        RemoteEnvironment xPeerEnvironment = null;

        if ( !environmentFound && !Common.HUB_ID.equals( containerHost.getInitiatorPeerId() ) )
        {
            Set<RemoteEnvironment> remoteEnvironments = getRemoteEnvironments( false );

            for ( RemoteEnvironment remoteEnvironment : remoteEnvironments )
            {
                if ( remoteEnvironment.getId().equals( containerHost.getEnvironmentId().getId() ) )
                {
                    xPeerEnvironment = remoteEnvironment;

                    break;
                }
            }
        }

        if ( xPeerEnvironment != null )
        {
            //if this is the only container in a remote environment
            //we need to remove the environment
            //environment.getContainerDtos() is used b/c getContainers exposes containers to owner only
            if ( xPeerEnvironment.getContainerDtos().isEmpty() || ( xPeerEnvironment.getContainerDtos().size() == 1
                    && containerHost.getId().equals( xPeerEnvironment.getContainerDtos().iterator().next().getId() ) ) )
            {
                cleanupEnvironment( xPeerEnvironment.getEnvironmentId() );
            }

            //notify remote peer about container destruction
            try
            {
                Peer peer = peerManager.getPeer( containerHost.getInitiatorPeerId() );

                if ( peer instanceof RemotePeer )
                {
                    ( ( RemotePeer ) peer ).excludeContainerFromEnvironment( containerHost.getEnvironmentId().getId(),
                            containerHost.getId() );
                }
            }
            catch ( PeerException e )
            {
                LOG.error( "Error excluding container from environment on remote peer: {}", e.getMessage() );
            }
        }
    }


    @Override
    public Set<String> getDeletedEnvironmentsFromHub()
    {
        return environmentAdapter.getDeletedEnvironmentsIds();
    }


    //called by local client
    @Override
    public void placeEnvironmentInfoByContainerIp( final String containerIp ) throws PeerException, CommandException
    {
        ContainerHost containerHost = getContainerHostByIp( containerIp );

        if ( containerHost == null )
        {
            throw new ContainerHostNotFoundException( "Container not found by ip " + containerIp );
        }

        Set<EnvironmentDto> environmentDtos = getTenantEnvironments();

        for ( EnvironmentDto environmentDto : environmentDtos )
        {
            //skip remote env-s
            if ( !REMOTE_OWNER_NAME.equalsIgnoreCase( environmentDto.getUsername() ) )
            {
                for ( ContainerDto containerDto : environmentDto.getContainers() )
                {
                    if ( containerIp.equals( containerDto.getIp() ) )
                    {
                        placeInfoIntoContainer( environmentDto, containerHost );

                        return;
                    }
                }
            }
        }

        try
        {
            RemotePeer peer = peerManager.findPeer( containerHost.getInitiatorPeerId() );

            if ( peer != null )
            {
                peer.placeEnvironmentInfoByContainerId( containerHost.getEnvironmentId().getId(),
                        containerHost.getId() );
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error requesting placement of environment info on remote peer: {}", e.getMessage() );

            for ( EnvironmentDto environmentDto : environmentDtos )
            {
                if ( REMOTE_OWNER_NAME.equalsIgnoreCase( environmentDto.getUsername() ) )
                {
                    for ( ContainerDto containerDto : environmentDto.getContainers() )
                    {
                        if ( containerIp.equals( containerDto.getIp() ) )
                        {
                            placeInfoIntoContainer( environmentDto, containerHost );

                            return;
                        }
                    }
                }
            }

            //rethrow error if env metadata not found
            throw e;
        }
    }


    //called by remote peer
    @Override
    public void placeEnvironmentInfoByContainerId( final String environmentId, final String containerId )
            throws EnvironmentNotFoundException, ContainerHostNotFoundException, CommandException
    {
        final LocalEnvironment environment = environmentService.find( environmentId );

        if ( environment == null )
        {
            throw new EnvironmentNotFoundException();
        }

        EnvironmentContainerImpl containerHost =
                ( EnvironmentContainerImpl ) environment.getContainerHostById( containerId );

        setTransientFields( Sets.<Environment>newHashSet( environment ) );

        EnvironmentDto environmentDto =
                new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                        environment.getContainerDtos(),
                        environment instanceof HubEnvironment ? Common.HUB_ID : Common.SUBUTAI_ID,
                        getEnvironmentOwnerName( environment ) );

        placeInfoIntoContainer( environmentDto, containerHost );
    }


    private void placeInfoIntoContainer( EnvironmentDto environmentDto, ContainerHost containerHost )
            throws CommandException
    {
        if ( containerHost instanceof EnvironmentContainerImpl )
        {
            // workaround to disable security checks for this call
            ( ( EnvironmentContainerImpl ) containerHost ).executeUnsafe( new RequestBuilder(
                    String.format( "rm /root/env ; echo '%s' > /root/env", JsonUtil.toJson( environmentDto ) ) ) );
        }
        else
        {
            containerHost.execute( new RequestBuilder(
                    String.format( "rm /root/env ; echo '%s' > /root/env", JsonUtil.toJson( environmentDto ) ) ) );
        }
    }


    private void cleanupEnvironment( EnvironmentId environmentId )
    {
        try
        {
            peerManager.getLocalPeer().cleanupEnvironment( environmentId );
        }
        catch ( PeerException e )
        {
            LOG.warn( "Error cleaning up environment: {}", e.getMessage() );
        }
    }


    private void destroyTunnelToPeer( EnvironmentPeer environmentPeer, Environment environment )
    {
        P2pIps p2pIps = new P2pIps();
        p2pIps.addP2pIps( environmentPeer.getRhP2pIps() );

        try
        {
            peerManager.getLocalPeer().deleteTunnels( p2pIps, environment.getEnvironmentId() );
        }
        catch ( PeerException e )
        {
            LOG.error( "Error destroying tunnels to peer {}: {}", environmentPeer.getPeerId(), e.getMessage() );
        }
    }
}
