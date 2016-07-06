package io.subutai.core.environment.impl;


import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.security.auth.Subject;
import javax.ws.rs.WebApplicationException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.Topology;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.metric.AlertValue;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.ReverseProxyConfig;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.environment.impl.adapter.ProxyEnvironment;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.EnvironmentAlertHandlerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.ContainerDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.EnvironmentDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.EnvironmentGrowingWorkflow;
import io.subutai.core.environment.impl.workflow.modification.EnvironmentModifyWorkflow;
import io.subutai.core.environment.impl.workflow.modification.P2PSecretKeyModificationWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyAdditionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyRemovalWorkflow;
import io.subutai.core.hubadapter.api.HubAdapter;
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
import io.subutai.core.tracker.api.Tracker;
import io.subutai.hub.share.common.HubEventListener;
import io.subutai.hub.share.dto.PeerProductDataDto;


@PermitAll
public class EnvironmentManagerImpl implements EnvironmentManager, PeerActionListener, AlertListener, HubEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class );

    public static final String MODULE_NAME = "Environment Manager";

    private final IdentityManager identityManager;
    private final RelationManager relationManager;
    private final PeerManager peerManager;
    private final Tracker tracker;
    protected Set<EnvironmentEventListener> listeners = Sets.newConcurrentHashSet();
    protected ExecutorService executor = SubutaiExecutors.newCachedThreadPool();
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Map<String, AlertHandler> alertHandlers = new ConcurrentHashMap<>();
    private SecurityManager securityManager;
    protected ScheduledExecutorService backgroundTasksExecutorService;
    private final Map<String, CancellableWorkflow> activeWorkflows = Maps.newConcurrentMap();
    Subject systemUser = null;

    private EnvironmentAdapter environmentAdapter;
    private EnvironmentService environmentService;


    public EnvironmentManagerImpl( final PeerManager peerManager, SecurityManager securityManager,
                                   final IdentityManager identityManager, final Tracker tracker,
                                   final RelationManager relationManager, HubAdapter hubAdapter,
                                   final EnvironmentService environmentService )
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( relationManager );
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( tracker );

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

        backgroundTasksExecutorService = Executors.newSingleThreadScheduledExecutor();
        backgroundTasksExecutorService.scheduleWithFixedDelay( new BackgroundTasksRunner(), 1, 60, TimeUnit.MINUTES );

        environmentAdapter = new EnvironmentAdapter( this, peerManager, hubAdapter );

        this.environmentService = environmentService;
    }


    public void init()
    {
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
            case REGISTER:
                // it is ok
                break;
            case UNREGISTER:
                if ( isPeerInUse( ( ( String ) peerAction.getData() ) ) )
                {
                    response = PeerActionResponse.Fail( "Peer in use." );
                }

                break;
        }
        return response;
    }


    private boolean isPeerInUse( String peerId )
    {
        boolean inUse = false;
        for ( Iterator<EnvironmentImpl> i = environmentService.getAll().iterator(); !inUse && i.hasNext(); )
        {
            EnvironmentImpl e = i.next();
            if ( e.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
            {
                inUse = true;
                break;
            }

            for ( PeerConf p : e.getPeerConfs() )
            {
                if ( peerId.equals( p.getPeerId() ) )
                {
                    inUse = true;
                    break;
                }
            }
        }
        return inUse;
    }


    private Set<Peer> getPeers( final Topology topology ) throws PeerException
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

        envs.addAll( environmentAdapter.getEnvironments() );

        setTransientFields( envs );

        return envs;
    }


    private void setTransientFields( Set<Environment> envs )
    {
        for ( Environment env : envs )
        {
            setEnvironmentTransientFields( env );

            setContainersTransientFields( env );
        }
    }


    @PermitAll
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


    private void registerActiveWorkflow( Environment environment, CancellableWorkflow newWorkflow )
    {
        Preconditions.checkNotNull( environment );
        Preconditions.checkNotNull( newWorkflow );

        CancellableWorkflow checkWorkflow = activeWorkflows.get( environment.getId() );

        if ( checkWorkflow != null )
        {
            throw new IllegalStateException( String.format( "There is already an active workflow %s for environment %s",
                    checkWorkflow.getClass().getSimpleName(), environment.getId() ) );
        }

        activeWorkflows.put( environment.getId(), newWorkflow );
    }


    private void removeActiveWorkflow( String environmentId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        activeWorkflows.remove( environmentId );
    }


    public Environment createEnvironment( final Topology topology, final boolean async,
                                          TrackerOperation operationTracker ) throws EnvironmentCreationException
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
                operationTracker.addLogFailed( String.format( "Peer %s is offline", peer.getId() ) );
                throw new EnvironmentCreationException( String.format( "Peer %s is offline", peer.getId() ) );
            }
        }

        //create empty environment
        final EnvironmentImpl environment = createEmptyEnvironment( topology );
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
                        exceptionUtil.getRootCause( environmentCreationWorkflow.getFailedException() ) );
            }
        }

        //return created environment
        return environment;
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public Environment createEnvironment( final Topology topology, final boolean async )
            throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topology.getEnvironmentName() ), "Invalid name" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        //create operation tracker
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Creating environment %s ", topology.getEnvironmentName() ) );

        return createEnvironment( topology, async, operationTracker );
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public UUID createEnvironmentAndGetTrackerID( final Topology topology, final boolean async )
            throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topology.getEnvironmentName() ), "Invalid name" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        //create operation tracker
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Creating environment %s ", topology.getEnvironmentName() ) );

        operationTracker.addLog( "Logger initialized" );

        createEnvironment( topology, async, operationTracker );

        return operationTracker.getId();
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                                          final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Growing environment %s", environment.getId() ) );

        //collect participating peers
        Set<Peer> allPeers = new HashSet<>();

        try
        {
            allPeers.addAll( getPeers( topology ) );
            allPeers.addAll( environment.getPeers() );
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
                operationTracker.addLogFailed( String.format( "Peer %s is offline", peer.getId() ) );
                throw new EnvironmentModificationException( String.format( "Peer %s is offline", peer.getId() ) );
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
        final EnvironmentGrowingWorkflow environmentGrowingWorkflow =
                getEnvironmentGrowingWorkflow( environment, topology, operationTracker );

        registerActiveWorkflow( environment, environmentGrowingWorkflow );

        //notify environment event listeners
        environmentGrowingWorkflow.onStop( new Runnable()
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
            environmentGrowingWorkflow.join();

            if ( environmentGrowingWorkflow.isFailed() )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( environmentGrowingWorkflow.getFailedException() ) );
            }
            else
            {
                Set<EnvironmentContainerHost> newContainers =
                        Sets.newHashSet( loadEnvironment( environment.getId() ).getContainerHosts() );
                newContainers.removeAll( oldContainers );
                return newContainers;
            }
        }

        return Sets.newHashSet();
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public UUID modifyEnvironmentAndGetTrackerID( final String environmentId, final Topology topology,
                                                  final List<String> removedContainers, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Modifying environment %s", environment.getId() ) );

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
                operationTracker.addLogFailed( String.format( "Peer %s is offline", peer.getId() ) );
                throw new EnvironmentModificationException( String.format( "Peer %s is offline", peer.getId() ) );
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
                getEnvironmentModifyingWorkflow( environment, topology, operationTracker, removedContainers );

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
                        exceptionUtil.getRootCause( environmentModifyWorkflow.getFailedException() ) );
            }
            else
            {
                Set<EnvironmentContainerHost> newContainers =
                        Sets.newHashSet( loadEnvironment( environment.getId() ).getContainerHosts() );
                newContainers.removeAll( oldContainers );
            }
        }

        return operationTracker.getId();
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void addSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshKey ), "Invalid ssh key" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Adding ssh key %s to environment %s ", sshKey, environmentId ) );


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
                        exceptionUtil.getRootCause( sshKeyAdditionWorkflow.getFailedException() ) );
            }
        }
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void removeSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshKey ), "Invalid ssh key" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Removing ssh key %s from environment %s ", sshKey, environmentId ) );

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
                        exceptionUtil.getRootCause( sshKeyRemovalWorkflow.getFailedException() ) );
            }
        }
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


    @RolesAllowed( { "Environment-Management|Update", "System-Management|Write", "System-Management|Update" } )
    @Override
    public void resetP2PSecretKey( final String environmentId, final String newP2pSecretKey,
                                   final long p2pSecretKeyTtlSec, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newP2pSecretKey ), "Invalid p2p secret key" );
        Preconditions.checkArgument( p2pSecretKeyTtlSec > 0, "Invalid p2p secret key time-to-live" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Resetting p2p secret key for environment %s ", environmentId ) );

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
                        exceptionUtil.getRootCause( p2PSecretKeyModificationWorkflow.getFailedException() ) );
            }
        }
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void destroyEnvironment( final String environmentId, final boolean async )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        // If environment from Hub, send destroy request to Hub
        if ( environment instanceof ProxyEnvironment )
        {
            environmentAdapter.removeEnvironment( environment );

            return;
        }

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Destroying environment %s", environmentId ) );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentDestructionException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final EnvironmentDestructionWorkflow environmentDestructionWorkflow =
                getEnvironmentDestructionWorkflow( this, environment, operationTracker );

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

                removeActiveWorkflow( environment.getId() );
            }
        } );

        //wait
        if ( !async )
        {
            environmentDestructionWorkflow.join();

            if ( environmentDestructionWorkflow.isFailed() )
            {
                throw new EnvironmentDestructionException(
                        exceptionUtil.getRootCause( environmentDestructionWorkflow.getFailedException() ) );
            }
        }
    }


    @Override
    @RolesAllowed( "Environment-Management|Delete" )
    public void destroyContainer( final String environmentId, final String containerId, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ), "Invalid container id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        if ( environment instanceof ProxyEnvironment )
        {
            environmentAdapter.destroyContainer( ( ProxyEnvironment ) environment, containerId );

            return;
        }

        TrackerOperation operationTracker =
                tracker.createTrackerOperation( MODULE_NAME, String.format( "Destroying container %s", containerId ) );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

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
            operationTracker.addLogFailed( String.format( "Container not registered: %s", e.getMessage() ) );

            throw new EnvironmentModificationException( e );
        }


        final ContainerDestructionWorkflow containerDestructionWorkflow =
                getContainerDestructionWorkflow( this, environment, environmentContainer, operationTracker );

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
                        exceptionUtil.getRootCause( containerDestructionWorkflow.getFailedException() ) );
            }
        }
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
                EnvironmentImpl environment = environmentService.find( environmentId );

                if ( environment != null )
                {
                    environment.setStatus( EnvironmentStatus.CANCELLED );

                    update( environment );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error cancelling environment workflow {}", e.getMessage(), e );

            throw new EnvironmentManagerException(
                    String.format( "Error cancelling environment workflow %s", e.getMessage() ) );
        }
    }


    @Override
    public Map<String, CancellableWorkflow> getActiveWorkflows()
    {
        return Collections.unmodifiableMap( activeWorkflows );
    }


    @PermitAll
    @Override
    public Environment loadEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        // First get from Hub
        EnvironmentImpl environment = environmentAdapter.get( environmentId );

        if ( environment != null )
        {
            return environment;
        }

        environment = environmentService.find( environmentId );

        if ( environment == null )
        {
            throw new EnvironmentNotFoundException();
        }

        //set environment's transient fields
        setEnvironmentTransientFields( environment );

        //set container's transient fields
        setContainersTransientFields( environment );

        return environment;
    }


    @Override
    public String getDefaultDomainName()
    {
        return Common.DEFAULT_DOMAIN_NAME;
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void removeEnvironmentDomain( final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        modifyEnvironmentDomain( environmentId, null, null, null );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
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


    public void modifyEnvironmentDomain( final String environmentId, final String domain,
                                         final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        boolean assign = !Strings.isNullOrEmpty( domain );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Modifying environment %s domain", environmentId ) );

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


    @PermitAll
    @Override
    public String getEnvironmentDomain( final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        try
        {
            return peerManager.getLocalPeer().getVniDomain( environment.getVni() );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentManagerException( "Error obtaining environment domain", e );
        }
    }


    @PermitAll
    @Override
    public boolean isContainerInEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        try
        {
            ContainerHost containerHost = environment.getContainerHostById( containerHostId );

            return peerManager.getLocalPeer().isIpInVniDomain(
                    containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp(),
                    environment.getVni() );
        }
        catch ( ContainerHostNotFoundException | PeerException e )
        {
            throw new EnvironmentManagerException( "Error checking container domain", e );
        }
    }

    //************ utility methods


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void addContainerToEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        toggleContainerDomain( containerHostId, environmentId, true );
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void removeContainerFromEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {

        toggleContainerDomain( containerHostId, environmentId, false );
    }


    public void toggleContainerDomain( final String containerHostId, final String environmentId, final boolean add )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        ContainerHost containerHost = environment.getContainerHostById( containerHostId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "%s container %s environment domain", add ? "Adding" : "Removing", containerHostId ) );

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
                peerManager.getLocalPeer().addIpToVniDomain(
                        containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp(),
                        environment.getVni() );
            }
            else
            {
                peerManager.getLocalPeer().removeIpFromVniDomain(
                        containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp(),
                        environment.getVni() );
            }

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


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public SshTunnel setupSshTunnelForContainer( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        EnvironmentContainerHost environmentContainer = environment.getContainerHostById( containerHostId );

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Setting up ssh tunnel for container %s ", containerHostId ) );

        try
        {
            SshTunnel sshTunnel = peerManager.getLocalPeer().setupSshTunnelForContainer(
                    environmentContainer.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp(),
                    Common.CONTAINER_SSH_TIMEOUT_SEC );

            operationTracker.addLogDone(
                    String.format( "Ssh for container %s is ready on tunnel %s", containerHostId, sshTunnel ) );

            return sshTunnel;
        }
        catch ( Exception e )
        {
            operationTracker.addLogFailed(
                    String.format( "Error setting up ssh for container %s: %s", containerHostId, e.getMessage() ) );
            throw new EnvironmentModificationException( e );
        }
    }


    @RolesAllowed( "Environment-Management|Write" )
    protected EnvironmentImpl createEmptyEnvironment( final Topology topology ) throws EnvironmentCreationException
    {
        EnvironmentImpl environment =
                new EnvironmentImpl( topology.getEnvironmentName(), topology.getSshKey(), getUserId(),
                        peerManager.getLocalPeer().getId() );

        environment.setStatus( EnvironmentStatus.PENDING );

        User activeUser = identityManager.getActiveUser();

        UserDelegate delegatedUser = identityManager.getUserDelegate( activeUser.getId() );

        environment.setRawTopology( JsonUtil.toJson( topology ) );

        environment.setUserId( delegatedUser.getUserId() );

        save( environment );

        createEnvironmentKeyPair( environment.getEnvironmentId(), delegatedUser.getId() );

        setEnvironmentTransientFields( environment );

        return environment;
    }


    private PGPSecretKeyRing createEnvironmentKeyPair( EnvironmentId envId, String userSecKeyId )
            throws EnvironmentCreationException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        String pairId = envId.getId();
        try
        {
            KeyPair keyPair = keyManager.generateKeyPair( pairId, false );

            //******Create PEK *****************************************************************
            PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );

            //***************Save Keys *********************************************************
            keyManager.saveSecretKeyRing( pairId, SecurityKeyType.EnvironmentKey.getId(), secRing );
            keyManager.savePublicKeyRing( pairId, SecurityKeyType.EnvironmentKey.getId(), pubRing );


            return secRing;
        }
        catch ( PGPException ex )
        {
            throw new EnvironmentCreationException( ex );
        }
    }


    public void setEnvironmentTransientFields( final Environment environment )
    {
        // Using environmentManager for ProxyEnvironment may give side effects. For example, empty container list.
        if ( !( environment instanceof ProxyEnvironment ) )
        {
            ( ( EnvironmentImpl ) environment ).setEnvironmentManager( this );
        }
    }


    public void setContainersTransientFields( final Environment environment )
    {
        Set<EnvironmentContainerHost> containers = environment.getContainerHosts();

        for ( ContainerHost containerHost : containers )
        {
            EnvironmentContainerImpl environmentContainer = ( EnvironmentContainerImpl ) containerHost;

            environmentContainer.setEnvironmentManager( this );

            environmentContainer.setEnvironmentAdapter( environmentAdapter );
        }
    }


    protected P2PSecretKeyModificationWorkflow getP2PSecretKeyModificationWorkflow( final EnvironmentImpl environment,
                                                                                    final String p2pSecretKey,
                                                                                    final long p2pSecretKeyTtlSec,
                                                                                    final TrackerOperation
                                                                                            operationTracker )
    {
        return new P2PSecretKeyModificationWorkflow( environment, p2pSecretKey, p2pSecretKeyTtlSec, operationTracker,
                this );
    }


    protected SshKeyAdditionWorkflow getSshKeyAdditionWorkflow( final EnvironmentImpl environment, final String sshKey,

                                                                final TrackerOperation operationTracker )
    {
        return new SshKeyAdditionWorkflow( environment, sshKey, operationTracker, this );
    }


    protected SshKeyRemovalWorkflow getSshKeyRemovalWorkflow( final EnvironmentImpl environment, final String sshKey,
                                                              final TrackerOperation operationTracker )
    {
        return new SshKeyRemovalWorkflow( environment, sshKey, operationTracker, this );
    }


    protected ContainerDestructionWorkflow getContainerDestructionWorkflow(
            final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
            final ContainerHost containerHost, final TrackerOperation operationTracker )
    {
        return new ContainerDestructionWorkflow( environmentManager, environment, containerHost, operationTracker );
    }


    @PermitAll
    protected EnvironmentCreationWorkflow getEnvironmentCreationWorkflow( final EnvironmentImpl environment,
                                                                          final Topology topology, final String sshKey,
                                                                          final TrackerOperation operationTracker )
    {
        return new EnvironmentCreationWorkflow( Common.DEFAULT_DOMAIN_NAME, this, peerManager, securityManager,
                environment, topology, sshKey, operationTracker );
    }


    protected EnvironmentGrowingWorkflow getEnvironmentGrowingWorkflow( final EnvironmentImpl environment,
                                                                        final Topology topology,
                                                                        final TrackerOperation operationTracker )
    {
        return new EnvironmentGrowingWorkflow( Common.DEFAULT_DOMAIN_NAME, peerManager, securityManager, environment,
                topology, operationTracker, this );
    }


    protected EnvironmentModifyWorkflow getEnvironmentModifyingWorkflow( final EnvironmentImpl environment,
                                                                         final Topology topology,
                                                                         final TrackerOperation operationTracker,
                                                                         final List<String> removedContainers )

    {
        return new EnvironmentModifyWorkflow( Common.DEFAULT_DOMAIN_NAME, peerManager, securityManager, environment,
                topology, removedContainers, operationTracker, this );
    }


    protected EnvironmentDestructionWorkflow getEnvironmentDestructionWorkflow(
            final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
            final TrackerOperation operationTracker )
    {
        return new EnvironmentDestructionWorkflow( environmentManager, environment, operationTracker );
    }


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


    @PermitAll
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


    @PermitAll
    public void notifyOnEnvironmentGrown( final Environment environment,
                                          final Set<EnvironmentContainerHost> containers )
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


    @PermitAll
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


    @PermitAll
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


    protected Long getUserId()
    {
        return identityManager.getActiveUser().getId();
    }


    public Peer resolvePeer( final String peerId ) throws PeerException
    {
        return peerManager.getPeer( peerId );
    }


    public void save( final EnvironmentImpl environment )
    {
        environmentService.persist( environment );

        setEnvironmentTransientFields( environment );

        setContainersTransientFields( environment );
    }


    public EnvironmentImpl update( EnvironmentImpl environment )
    {
        if ( environment instanceof ProxyEnvironment )
        {
            // Environment from Hub
            return environment;
        }

        environment = environmentService.merge( environment );

        setEnvironmentTransientFields( environment );

        setContainersTransientFields( environment );

        environmentAdapter.uploadEnvironment( environment );

        return environment;
    }


    public void remove( final EnvironmentImpl environment )
    {
        environmentService.remove( environment.getId() );

        environmentAdapter.removeEnvironment( environment );
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
                            String.format( "Invoking pre-processor of" + " '%s:%s'.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    handler.preProcess( environment, alertValue );
                    alertEvent.addLog(
                            String.format( "Pre-processor of '%s:%s' " + "finished.", handlerId.getAlertHandlerId(),
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
                            String.format( "Pre-processor of '%s:%s' " + "finished.", handlerId.getAlertHandlerId(),
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
            Environment environment = loadEnvironment( environmentId );

            environment.addAlertHandler( new EnvironmentAlertHandlerImpl( handlerId, handlerPriority ) );

            update( ( EnvironmentImpl ) environment );
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
            Environment environment = environmentService.find( environmentId );
            environment.removeAlertHandler( new EnvironmentAlertHandlerImpl( handlerId, handlerPriority ) );
            update( ( EnvironmentImpl ) environment );
        }
        catch ( Exception e )
        {
            LOG.error( "Error on stop monitoring", e );
            throw new EnvironmentManagerException( e.getMessage(), e );
        }
    }


    @Override
    public void addReverseProxy( final Environment environment, final ReverseProxyConfig reverseProxyConfig )
            throws EnvironmentModificationException
    {
        try
        {
            ContainerHost containerHost = environment.getContainerHostById( reverseProxyConfig.getContainerId() );
            Peer peer = peerManager.getPeer( containerHost.getPeerId() );
            peer.addReverseProxy( reverseProxyConfig );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentModificationException( "Error on adding reverse proxy." );
        }
    }


    @Override
    public void onRegistrationSucceeded()
    {
        Set<Environment> envs = new HashSet<>();

        envs.addAll( environmentService.getAll() );

        setTransientFields( envs );

        LOG.info( "onRegistrationSucceeded: local environments count = {}", envs.size() );

        try
        {
            environmentAdapter.uploadEnvironments( envs );
        }
        catch ( Exception e )
        {
            LOG.error( "Error uploading environments to Hub: {}", e.getMessage() );
        }
    }


    @Override
    public void onPluginEvent( final String pluginUid, final PeerProductDataDto.State state )
    {
    }


    public EnvironmentContainerImpl update( final EnvironmentContainerImpl container )
    {
        Environment environment = container.getEnvironment();

        EnvironmentContainerImpl envContainer = environmentService.mergeContainer( container );

        envContainer.setEnvironmentManager( this );

        //update cache
        ( ( EnvironmentImpl ) environment ).removeContainer( envContainer );
        ( ( EnvironmentImpl ) environment ).addContainers( Sets.newHashSet( envContainer ) );

        return envContainer;
    }


    private class BackgroundTasksRunner implements Runnable
    {
        @Override
        public void run()
        {
            LOG.debug( "Environment background tasks started..." );


            //**************************************************
            Subject.doAs( systemUser, new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    resetP2Pkey();
                    return null;
                }
            } );
            //**************************************************

            LOG.debug( "Environment background tasks finished." );
        }
    }


    private void resetP2Pkey()
    {
        try
        {
            //process only SS side environments
            for ( Environment environment : environmentService.getAll() )
            {
                if ( !( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION
                        || environment.getStatus() == EnvironmentStatus.CANCELLED || (
                        ( System.currentTimeMillis() - environment.getCreationTimestamp() ) < TimeUnit.HOURS
                                .toMillis( 1 ) ) ) )
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
}
