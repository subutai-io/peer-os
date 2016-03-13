package io.subutai.core.environment.impl;


import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.metric.AlertValue;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.ShareDto.ShareDto;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.api.exception.EnvironmentSecurityException;
import io.subutai.core.environment.impl.dao.EnvironmentContainerDataService;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.environment.impl.dao.TopologyDataService;
import io.subutai.core.environment.impl.entity.EnvironmentAlertHandlerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.construction.EnvironmentImportWorkflow;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.ContainerDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.EnvironmentDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.EnvironmentGrowingWorkflow;
import io.subutai.core.environment.impl.workflow.modification.EnvironmentModifyWorkflow;
import io.subutai.core.environment.impl.workflow.modification.P2PSecretKeyModificationWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyAdditionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyRemovalWorkflow;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.model.Relation;
import io.subutai.core.object.relation.api.model.RelationInfo;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationMeta;
import io.subutai.core.object.relation.api.model.RelationStatus;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionListener;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.tracker.api.Tracker;


@PermitAll
public class EnvironmentManagerImpl implements EnvironmentManager, PeerActionListener, AlertListener
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class );

    private static final String MODULE_NAME = "Environment Manager";
    private static final String DEFAULT_GATEWAY_TEMPLATE = "192.168.%s.1/24";

    private final IdentityManager identityManager;
    private final RelationManager relationManager;
    private final PeerManager peerManager;
    private final NetworkManager networkManager;
    private final Tracker tracker;
    private final TemplateManager templateRegistry;
    private final DaoManager daoManager;
    protected Set<EnvironmentEventListener> listeners = Sets.newConcurrentHashSet();
    protected ExecutorService executor = SubutaiExecutors.newCachedThreadPool();
    protected EnvironmentDataService environmentDataService;
    protected EnvironmentContainerDataService environmentContainerDataService;
    protected TopologyDataService topologyDataService;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Map<String, AlertHandler> alertHandlers = new ConcurrentHashMap<String, AlertHandler>();
    private SecurityManager securityManager;


    public EnvironmentManagerImpl( final TemplateManager templateRegistry, final PeerManager peerManager,
                                   SecurityManager securityManager, final NetworkManager networkManager,
                                   final DaoManager daoManager, final IdentityManager identityManager,
                                   final Tracker tracker, final RelationManager relationManager )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( networkManager );
        Preconditions.checkNotNull( daoManager );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( relationManager );
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( tracker );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.networkManager = networkManager;
        this.daoManager = daoManager;
        this.identityManager = identityManager;
        this.relationManager = relationManager;
        this.tracker = tracker;
    }


    public void init()
    {
        this.topologyDataService = new TopologyDataService( daoManager );
        this.environmentDataService = new EnvironmentDataService( daoManager );
        this.environmentContainerDataService = new EnvironmentContainerDataService( daoManager );
    }


    public void dispose()
    {
        executor.shutdown();
    }


    public SecurityManager getSecurityManager()
    {
        return securityManager;
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
        for ( Iterator<EnvironmentImpl> i = environmentDataService.getAll().iterator(); !inUse && i.hasNext(); )
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


    @PermitAll
    @Override
    public Set<Environment> getEnvironments()
    {
        User activeUser = identityManager.getActiveUser();


        Set<Environment> environments = new HashSet<>();
        for ( Environment environment : environmentDataService.getAll() )
        {
            boolean trustedRelation = relationManager.getRelationInfoManager().allHasReadPermissions( environment );
            if ( environment.getUserId().equals( activeUser.getId() ) || trustedRelation )
            {
                environments.add( environment );

                setEnvironmentTransientFields( environment );
                setContainersTransientFields( environment );
            }
        }

        return environments;
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public Environment setupRequisites( final Topology topology ) throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topology.getEnvironmentName() ), "Invalid name" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        try
        {
            topology.setSubnet( calculateCidr( getPeers( topology ) ) );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentCreationException( e.getMessage() );
        }

        //create empty environment
        return createEmptyEnvironment( topology );
    }


    @Override
    @RolesAllowed( "Environment-Management|Write" )
    public Environment startEnvironmentBuild( String environmentId, final String signedMessage, boolean async )
            throws EnvironmentCreationException
    {
        //create empty environment
        final EnvironmentImpl environment = environmentDataService.find( environmentId );

        // TODO should be handled on server side when user sends signed message

        Topology topology = JsonUtil.fromJson( environment.getRawTopology(), Topology.class );

        try
        {
            topology.setSubnet( calculateCidr( getPeers( topology ) ) );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentCreationException( e );
        }

        // TODO add additional step for receiving trust message


        //create operation tracker
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Creating environment %s ", environment.getId() ) );

        //launch environment creation workflow
        final EnvironmentCreationWorkflow environmentCreationWorkflow =
                getEnvironmentCreationWorkflow( environment, topology, topology.getSshKey(), operationTracker );

        //start environment creation workflow
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                environmentCreationWorkflow.start();
            }
        } );

        //notify environment event listeners
        environmentCreationWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {

                notifyOnEnvironmentCreated( environment );
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

        topology.setSubnet( calculateCidr( allPeers ) );

        try
        {
            //create empty environment
            final EnvironmentImpl environment = createEmptyEnvironment( topology );
            // TODO add additional step for receiving trust message


            //launch environment creation workflow
            final EnvironmentCreationWorkflow environmentCreationWorkflow =
                    getEnvironmentCreationWorkflow( environment, topology, topology.getSshKey(), operationTracker );

            //start environment creation workflow
            executor.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    environmentCreationWorkflow.start();
                }
            } );

            //notify environment event listeners
            environmentCreationWorkflow.onStop( new Runnable()
            {
                @Override
                public void run()
                {

                    notifyOnEnvironmentCreated( environment );
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
        catch ( EnvironmentCreationException e )
        {
            operationTracker.addLogFailed( e.getMessage() );
            throw new EnvironmentCreationException( e );
        }
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

        topology.setSubnet( calculateCidr( allPeers ) );

        try
        {
            //create empty environment
            final EnvironmentImpl environment = createEmptyEnvironment( topology );
            // TODO add additional step for receiving trust message


            //launch environment creation workflow
            final EnvironmentCreationWorkflow environmentCreationWorkflow =
                    getEnvironmentCreationWorkflow( environment, topology, topology.getSshKey(), operationTracker );

            //start environment creation workflow
            executor.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    environmentCreationWorkflow.start();
                }
            } );

            //notify environment event listeners
            environmentCreationWorkflow.onStop( new Runnable()
            {
                @Override
                public void run()
                {

                    notifyOnEnvironmentCreated( environment );
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
            return operationTracker.getId();
        }
        catch ( EnvironmentCreationException e )
        {
            operationTracker.addLogFailed( e.getMessage() );
            throw new EnvironmentCreationException( e );
        }
    }


    // TODO refactor to pass one Blueprint parameter from subutai-common
    @Override
    public Environment importEnvironment( final String name, final Topology topology,
                                          final Map<Node, Set<ContainerHostInfo>> containers, final Integer vlan )
            throws EnvironmentCreationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        Map.Entry<Node, Set<ContainerHostInfo>> containersEntry = containers.entrySet().iterator().next();
        Iterator<ContainerHostInfo> hostIterator = containersEntry.getValue().iterator();

        String ip = "";

        while ( hostIterator.hasNext() && StringUtil.isStringNullOrEmpty( ip ) )
        {
            HostInfo sampleHostInfo = hostIterator.next();

            //TODO ip is chosen from first standing container host info
            for ( final HostInterface iface : sampleHostInfo.getHostInterfaces().getAll() )
            {
                if ( StringUtil.isStringNullOrEmpty( iface.getIp() ) )
                {
                    continue;
                }
                ip = iface.getIp() + "/24";
                break;
            }
        }

        if ( StringUtil.isStringNullOrEmpty( ip ) )
        {
            throw new EnvironmentCreationException( "Invalid environment ip range" );
        }

        //create empty environment
        final EnvironmentImpl environment = createEmptyEnvironment( name, ip, topology.getSshKey() );
        for ( Map.Entry<Node, Set<ContainerHostInfo>> entry : containers.entrySet() )
        {
            for ( ContainerHostInfo newHost : entry.getValue() )
            {
                ContainerSize containerType = entry.getKey().getType();

                environment.addContainers( Sets.newHashSet(
                        new EnvironmentContainerImpl( peerManager.getLocalPeer().getId(), entry.getKey().getPeerId(),
                                entry.getKey().getName(), new ContainerHostInfoModel( newHost ),
                                entry.getKey().getTemplateName(), HostArchitecture.AMD64,
                                entry.getKey().getSshGroupId(), entry.getKey().getHostsGroupId(),
                                Common.DEFAULT_DOMAIN_NAME, containerType, entry.getKey().getResourceHostId(),
                                entry.getKey().getName() ) ) );
            }
        }
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Creating environment %s ", environment.getId() ) );

        EnvironmentImportWorkflow environmentImportWorkflow =
                getEnvironmentImportWorkflow( environment, topology, operationTracker );

        environmentImportWorkflow.start();
        //notify environment event listeners
        environmentImportWorkflow.onStop( new Runnable()
        {
            @Override
            public void run()
            {
                notifyOnEnvironmentCreated( environment );
            }
        } );


        return environment;
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
        if ( !relationManager.getRelationInfoManager().groupHasUpdatePermissions( environment ) )
        {
            throw new EnvironmentNotFoundException();
        }

        TrackerOperation operationTracker =
                tracker.createTrackerOperation( MODULE_NAME, String.format( "Growing environment %s", environmentId ) );

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

        topology.setSubnet( environment.getSubnetCidr() );

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final Set<EnvironmentContainerHost> oldContainers = Sets.newHashSet( environment.getContainerHosts() );


        //launch environment growing workflow

        final EnvironmentGrowingWorkflow environmentGrowingWorkflow =
                getEnvironmentGrowingWorkflow( environment, topology, operationTracker );

        //start environment growing workflow
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                environmentGrowingWorkflow.start();
            }
        } );

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
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );
        if ( !relationManager.getRelationInfoManager().groupHasUpdatePermissions( environment ) )
        {
            throw new EnvironmentNotFoundException();
        }

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Modifying environment %s", environmentId ) );

        Set<Peer> allPeers = new HashSet<>();
        final Set<EnvironmentContainerHost> oldContainers = Sets.newHashSet( environment.getContainerHosts() );


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

        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }


        if ( topology != null )
        {
            topology.setSubnet( environment.getSubnetCidr() );
        }


        //launch environment growing workflow

        final EnvironmentModifyWorkflow environmentModifyWorkflow =
                getEnvironmentModifyingWorkflow( environment, topology, operationTracker, removedContainers, false );

        //start environment growing workflow
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                environmentModifyWorkflow.start();
            }
        } );

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
                if ( topology != null )
                {
                    Set<EnvironmentContainerHost> newContainers =
                            Sets.newHashSet( loadEnvironment( environment.getId() ).getContainerHosts() );
                    newContainers.removeAll( oldContainers );
                }
            }
        }

        return operationTracker.getId();
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void addSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshKey ), "Invalid ssh key" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( environment ) )
        {
            throw new EnvironmentNotFoundException();
        }

        TrackerOperation op = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Adding ssh key %s to environment %s ", sshKey, environmentId ) );

        final SshKeyAdditionWorkflow sshKeyAdditionWorkflow =
                getSshKeyAdditionWorkflow( environment, sshKey, networkManager, op );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                sshKeyAdditionWorkflow.start();
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

        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( environment ) )
        {
            throw new EnvironmentNotFoundException();
        }

        TrackerOperation op = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Removing ssh key %s from environment %s ", sshKey, environmentId ) );

        final SshKeyRemovalWorkflow sshKeyRemovalWorkflow =
                getSshKeyRemovalWorkflow( environment, sshKey, networkManager, op );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                sshKeyRemovalWorkflow.start();
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


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void resetP2PSecretKey( final String environmentId, final String newP2pSecretKey,
                                   final long p2pSecretKeyTtlSec, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newP2pSecretKey ), "Invalid p2p secret key" );
        Preconditions.checkArgument( p2pSecretKeyTtlSec > 0, "Invalid p2p secret key time-to-live" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );
        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( environment ) )
        {
            throw new EnvironmentNotFoundException();
        }

        TrackerOperation op = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Resetting p2p secret key for environment %s ", environmentId ) );

        final P2PSecretKeyModificationWorkflow p2PSecretKeyModificationWorkflow =
                getP2PSecretKeyModificationWorkflow( environment, newP2pSecretKey, p2pSecretKeyTtlSec, op );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                p2PSecretKeyModificationWorkflow.start();
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
                                                                final NetworkManager networkManager,
                                                                final TrackerOperation operationTracker )
    {
        return new SshKeyAdditionWorkflow( environment, sshKey, networkManager, operationTracker, this );
    }


    protected SshKeyRemovalWorkflow getSshKeyRemovalWorkflow( final EnvironmentImpl environment, final String sshKey,
                                                              final NetworkManager networkManager,
                                                              final TrackerOperation operationTracker )
    {
        return new SshKeyRemovalWorkflow( environment, sshKey, networkManager, operationTracker, this );
    }


    protected boolean isUserAdmin()
    {
        return true;//getUser().isAdmin();
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void destroyEnvironment( final String environmentId, final boolean async,
                                    final boolean forceMetadataRemoval )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {


        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );
        if ( !relationManager.getRelationInfoManager().allHasDeletePermissions( environment ) )
        {
            throw new EnvironmentNotFoundException();
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
                getEnvironmentDestructionWorkflow( peerManager, this, environment, forceMetadataRemoval,
                        operationTracker );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                environmentDestructionWorkflow.start();
            }
        } );

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
            }
        } );

        //wait
        if ( !async )
        {
            environmentDestructionWorkflow.join();

            if ( environmentDestructionWorkflow.getError() != null )
            {
                throw new EnvironmentDestructionException(
                        exceptionUtil.getRootCause( environmentDestructionWorkflow.getError() ) );
            }
        }
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void destroyContainer( final String environmentId, final String containerId, final boolean async,
                                  final boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ), "Invalid container id" );


        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        User activeUser = identityManager.getActiveUser();

        //final boolean deleteAll = identityManager
        //.isUserPermitted                   ( activeUser, PermissionObject.EnvironmentManagement,
        //PermissionScope.ALL_SCOPE,
        //PermissionOperation.Delete );
        boolean canDelete = relationManager.getRelationInfoManager().allHasDeletePermissions( environment );
        if ( !( environment.getUserId().equals( activeUser.getId() ) || canDelete ) )
        {
            throw new AccessControlException( "You have not enough permissions." );
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
            if ( !relationManager.getRelationInfoManager().allHasDeletePermissions( environmentContainer ) )
            {
                throw new ContainerHostNotFoundException( "Container host not found." );
            }
        }
        catch ( ContainerHostNotFoundException e )
        {
            operationTracker.addLogFailed( String.format( "Container not registered: %s", e.getMessage() ) );

            throw new EnvironmentModificationException( e );
        }


        final ContainerDestructionWorkflow containerDestructionWorkflow =
                getContainerDestructionWorkflow( this, environment, environmentContainer, forceMetadataRemoval,
                        operationTracker );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                containerDestructionWorkflow.start();
            }
        } );

        //wait
        if ( !async )
        {
            containerDestructionWorkflow.join();

            if ( containerDestructionWorkflow.getError() != null )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( containerDestructionWorkflow.getError() ) );
            }
        }
    }


    protected ContainerDestructionWorkflow getContainerDestructionWorkflow(
            final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
            final ContainerHost containerHost, final boolean forceMetadataRemoval,
            final TrackerOperation operationTracker )
    {
        return new ContainerDestructionWorkflow( environmentManager, environment, containerHost, forceMetadataRemoval,
                operationTracker );
    }


    @PermitAll
    @Override
    public Environment loadEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        EnvironmentImpl environment = environmentDataService.find( environmentId );

        if ( environment == null )
        {
            throw new EnvironmentNotFoundException();
        }

        //check access to environment
        if ( !relationManager.getRelationInfoManager().allHasReadPermissions( environment ) )
        {
            throw new EnvironmentSecurityException(
                    String.format( "Access to environment %s is denied", environment.getName() ) );
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


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void removeEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        Environment environment = loadEnvironment( environmentId );

        User activeUser = identityManager.getActiveUser();
        boolean canDelete = relationManager.getRelationInfoManager().allHasDeletePermissions( environment );

        //final boolean deleteAll = identityManager
        //.isUserPermitted                   ( activeUser, PermissionObject.EnvironmentManagement,
        // PermissionScope.ALL_SCOPE,
        //PermissionOperation.Delete );

        if ( environment.getUserId().equals( activeUser.getId() ) || canDelete )
        {
            environmentDataService.remove( ( EnvironmentImpl ) environment );
            notifyOnEnvironmentDestroyed( environmentId );
        }
        else
        {
            throw new AccessControlException( "You have not enough permissions." );
        }
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public void saveTopology( final Topology topology ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );

        topologyDataService.persist( topology );
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public Topology getTopology( final UUID id ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( id, "Blueprint ID could not be null" );
        return topologyDataService.find( id );
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void removeTopology( final UUID topologyId ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( topologyId, "Invalid blueprint id" );

        topologyDataService.remove( topologyId );
    }


    @PermitAll
    @Override
    public Set<Topology> getBlueprints() throws EnvironmentManagerException
    {
        return topologyDataService.getAll();
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void updateEnvironmentContainersMetadata( final String environmentId ) throws EnvironmentManagerException
    {
        try
        {
            Environment environment = loadEnvironment( environmentId );
            Set<EnvironmentContainerHost> containerHosts = environment.getContainerHosts();

            for ( final ContainerHost containerHost : containerHosts )
            {
                try
                {
                    HostInfo hostInfo = containerHost.getPeer().getContainerHostInfoById( containerHost.getId() );

                    EnvironmentContainerImpl environmentContainer =
                            environmentContainerDataService.find( containerHost.getId() );
                    if ( !relationManager.getRelationInfoManager().allHasDeletePermissions( environmentContainer ) )
                    {
                        continue;
                    }
                    environmentContainer.setHostname( hostInfo.getHostname() );
                    environmentContainer.setHostInterfaces( hostInfo.getHostInterfaces() );

                    environmentContainerDataService.update( environmentContainer );
                }
                catch ( Exception e )
                {
                    LOG.error( "Couldn't get container host info from hosting peer", e );
                }
            }
        }
        catch ( EnvironmentNotFoundException e )
        {
            throw new EnvironmentManagerException(
                    String.format( "Couldn't find environment by id: %s", environmentId ), e );
        }
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void removeEnvironmentDomain( final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );
        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( environment ) )
        {
            return;
        }

        modifyEnvironmentDomain( environmentId, null, null, null );
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void assignEnvironmentDomain( final String environmentId, final String newDomain,
                                         final DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newDomain ), "Invalid domain" );
        Preconditions.checkArgument( newDomain.matches( Common.HOSTNAME_REGEX ), "Invalid domain" );
        Preconditions.checkNotNull( domainLoadBalanceStrategy );

        EnvironmentImpl environment = environmentDataService.find( environmentId );
        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( environment ) )
        {
            throw new EnvironmentNotFoundException();
        }


        modifyEnvironmentDomain( environmentId, newDomain, domainLoadBalanceStrategy, sslCertPath );
    }


    public void modifyEnvironmentDomain( final String environmentId, final String domain,
                                         final DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );

        boolean assign = !Strings.isNullOrEmpty( domain );
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Modifying environment %s domain", environmentId ) );
        try
        {
            if ( assign )
            {
                peerManager.getLocalPeer()
                           .setVniDomain( environment.getVni(), domain, domainLoadBalanceStrategy, sslCertPath );
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
    public int setupContainerSsh( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId );
        EnvironmentContainerImpl environmentContainer = environmentContainerDataService.find( containerHostId );
        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( environmentContainer ) )
        {
            throw new ContainerHostNotFoundException( "Container host not found." );
        }

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Setting up ssh for container %s ", containerHostId ) );

        environment.getContainerHostById( containerHostId );
        try
        {
            int sshPort =
                    peerManager.getLocalPeer().setupContainerSsh( containerHostId, Common.CONTAINER_SSH_TIMEOUT_SEC );

            operationTracker.addLogDone(
                    String.format( "Ssh for container %s is ready on port %d", containerHostId, sshPort ) );

            return sshPort;
        }
        catch ( Exception e )
        {
            operationTracker.addLogFailed(
                    String.format( "Error setting up ssh for container %s: %s", containerHostId, e.getMessage() ) );
            throw new EnvironmentModificationException( e );
        }
    }


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
        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( containerHost ) )
        {
            throw new EnvironmentSecurityException(
                    String.format( "Access to container %s is denied", environment.getName() ) );
        }

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "%s container %s environment domain", add ? "Adding" : "Removing", containerHostId ) );
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


    protected EnvironmentImportWorkflow getEnvironmentImportWorkflow( final EnvironmentImpl environment,
                                                                      final Topology topology,
                                                                      final TrackerOperation tracker )
    {
        return new EnvironmentImportWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, this, networkManager,
                peerManager, securityManager, identityManager, environment, topology, tracker );
    }


    @PermitAll
    protected EnvironmentCreationWorkflow getEnvironmentCreationWorkflow( final EnvironmentImpl environment,
                                                                          final Topology topology, final String sshKey,
                                                                          final TrackerOperation operationTracker )
    {
        if ( !relationManager.getRelationInfoManager().allHasReadPermissions( environment ) )
        {
            throw new AccessControlException( "You don't have enough permissions to create environment" );
        }
        return new EnvironmentCreationWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, this, networkManager,
                peerManager, securityManager, environment, topology, sshKey, operationTracker );
    }


    @RolesAllowed( "Environment-Management|Write" )
    protected EnvironmentImpl createEmptyEnvironment( final Topology topology ) throws EnvironmentCreationException
    {

        EnvironmentImpl environment =
                new EnvironmentImpl( topology.getEnvironmentName(), topology.getSubnet(), topology.getSshKey(),
                        getUserId(), peerManager.getLocalPeer().getId() );
        environment.setStatus( EnvironmentStatus.PENDING );

        User activeUser = identityManager.getActiveUser();
        UserDelegate delegatedUser = identityManager.getUserDelegate( activeUser.getId() );

        // User - Delegated user - Environment
        // Delegated user - Delegated user - Environment
        // Delegated user - Environment - Container

        // TODO create relation between activeUser and delegatedUser
        environment.setRawTopology( JsonUtil.toJson( topology ) );
        environment.setUserId( delegatedUser.getUserId() );
        createEnvironmentKeyPair( environment.getEnvironmentId(), delegatedUser.getId() );
        try
        {

            // TODO user should send signed trust message between delegatedUser and himself
            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );

            RelationInfo relationInfo = relationManager.createTrustRelationship( relationInfoMeta );

            // TODO relation verification should be done by delegated user, automatically
            RelationMeta relationMeta =
                    new RelationMeta( delegatedUser, delegatedUser, environment, activeUser.getSecurityKeyId() );
            Relation relation = relationManager.buildTrustRelation( relationInfo, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
        catch ( Exception e )
        {
            LOG.warn( "Error message.", e );
            throw new EnvironmentCreationException( e );
        }

        environment = save( environment );

        setEnvironmentTransientFields( environment );

        notifyOnEnvironmentCreated( environment );

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


    private String calculateCidr( final Set<Peer> peers ) throws EnvironmentCreationException
    {
        try
        {
            Set<String> usedIps = new HashSet<>();
            usedIps.addAll( getUsedIps( peerManager.getLocalPeer() ) );
            for ( Peer peer : peers )
            {
                usedIps.addAll( getUsedIps( peer ) );
            }

            String theCidr = null;

            for ( int i = 1; i < 255; i++ )
            {
                SubnetUtils.SubnetInfo info = new SubnetUtils( String.format( DEFAULT_GATEWAY_TEMPLATE, i ) ).getInfo();

                boolean isUsed = false;

                for ( String usedIp : usedIps )
                {
                    if ( info.isInRange( usedIp ) )
                    {
                        isUsed = true;
                        break;
                    }
                }

                if ( !isUsed )
                {
                    theCidr = info.getCidrSignature();
                    break;
                }
            }

            if ( theCidr == null )
            {
                throw new EnvironmentCreationException( "Could not determine subnet cidr." );
            }

            return theCidr;
        }
        catch ( PeerException e )
        {
            throw new EnvironmentCreationException( String.format( "Error on validating subnet: %s", e.getMessage() ) );
        }
    }


    protected Set<String> getUsedIps( Peer peer ) throws PeerException
    {

        Set<String> usedIps = Sets.newHashSet();

        for ( HostInterface hostInterface : peer.getInterfaces().getAll() )
        {
            usedIps.add( hostInterface.getIp() );
        }

        return usedIps;
    }


    public void setEnvironmentTransientFields( final Environment environment )
    {
        ( ( EnvironmentImpl ) environment ).setEnvironmentManager( this );
    }


    public void setContainersTransientFields( final Environment environment )
    {
        User activeUser = identityManager.getActiveUser();
        Set<EnvironmentContainerHost> containers = environment.getContainerHosts();
        for ( ContainerHost containerHost : containers )
        {
            EnvironmentContainerImpl environmentContainer = ( EnvironmentContainerImpl ) containerHost;

            environmentContainer.setEnvironmentManager( this );

            String peerId = environmentContainer.getPeerId();
        }
        // remove containers which doesn't have trust relation
    }


    protected EnvironmentGrowingWorkflow getEnvironmentGrowingWorkflow( final EnvironmentImpl environment,
                                                                        final Topology topology,
                                                                        final TrackerOperation operationTracker )
    {
        return new EnvironmentGrowingWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, networkManager,
                peerManager, environment, topology, operationTracker, this );
    }


    protected EnvironmentModifyWorkflow getEnvironmentModifyingWorkflow( final EnvironmentImpl environment,
                                                                         final Topology topology,
                                                                         final TrackerOperation operationTracker,
                                                                         final List<String> removedContainers,
                                                                         final boolean removeMetaData )

    {
        return new EnvironmentModifyWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, networkManager, peerManager,
                environment, topology, removedContainers, operationTracker, this, removeMetaData );
    }


    protected EnvironmentDestructionWorkflow getEnvironmentDestructionWorkflow( final PeerManager peerManager,
                                                                                final EnvironmentManagerImpl
                                                                                        environmentManager,
                                                                                final EnvironmentImpl environment,
                                                                                final boolean forceMetadataRemoval,
                                                                                final TrackerOperation
                                                                                        operationTracker )
    {
        return new EnvironmentDestructionWorkflow( environmentManager, environment, forceMetadataRemoval,
                operationTracker );
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
    @Override
    public void notifyOnContainerStateChanged( final Environment environment, final ContainerHost containerHost )
    {
        update( ( EnvironmentImpl ) environment );
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


    @RolesAllowed( "Environment-Management|Write" )
    protected EnvironmentImpl createEmptyEnvironment( final String name, final String subnetCidr, final String sshKey )
    {

        EnvironmentImpl environment =
                new EnvironmentImpl( name, subnetCidr, sshKey, getUserId(), peerManager.getLocalPeer().getId() );

        environment.setUserId( identityManager.getActiveUser().getId() );
        environment = save( environment );

        setEnvironmentTransientFields( environment );

        notifyOnEnvironmentCreated( environment );

        return environment;
    }


    protected Long getUserId()
    {
        return ( long ) 0;//getUser().getId();
    }


    public Peer resolvePeer( final String peerId ) throws PeerException
    {
        return peerManager.getPeer( peerId );
    }


    public EnvironmentImpl save( final Environment environment )
    {
        EnvironmentImpl env = environmentDataService.save( environment );
        setEnvironmentTransientFields( env );
        setContainersTransientFields( env );
        return env;
    }


    public EnvironmentImpl update( final EnvironmentImpl environment )
    {
        environmentDataService.update( ( EnvironmentImpl ) environment );
        setEnvironmentTransientFields( environment );
        setContainersTransientFields( environment );
        return environment;
    }


    public void remove( final EnvironmentImpl environment )
    {
        environmentDataService.remove( environment );
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
            Environment environment = environmentDataService.find( environmentId );
            environment.removeAlertHandler( new EnvironmentAlertHandlerImpl( handlerId, handlerPriority ) );
            environmentDataService.save( environment );
        }
        catch ( Exception e )
        {
            LOG.error( "Error on stop monitoring", e );
            throw new EnvironmentManagerException( e.getMessage(), e );
        }
    }


    @Override
    public List<ShareDto> getSharedUsers( final String objectId ) throws EnvironmentNotFoundException
    {
        Environment environment = loadEnvironment( objectId );
        RelationLink objectRelationLink = relationManager.getRelationLink( environment );

        List<Relation> relations = relationManager.getRelationsByObject( objectRelationLink );
        List<ShareDto> sharedUsers = Lists.newArrayList();

        for ( final Relation relation : relations )
        {
            UserDelegate delegatedUser = identityManager.getUserDelegate( relation.getTarget().getUniqueIdentifier() );
            if ( delegatedUser == null )
            {
                continue;
            }
            ShareDto shareDto = new ShareDto();
            shareDto.setId( delegatedUser.getUserId() );

            RelationInfo relationInfo = relation.getRelationInfo();
            shareDto.setRead( relationInfo.isReadPermission() );
            shareDto.setDelete( relationInfo.isDeletePermission() );
            shareDto.setUpdate( relationInfo.isUpdatePermission() );
            shareDto.setWrite( relationInfo.isWritePermission() );

            sharedUsers.add( shareDto );
        }

        return sharedUsers;
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void shareEnvironment( final ShareDto[] shareDto, final String environmentId )
    {
        Environment environment;
        try
        {
            environment = loadEnvironment( environmentId );
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Don't have permissions.", e );
            return;
        }

        if ( !relationManager.getRelationInfoManager().groupHasUpdatePermissions( environment ) )
        {
            throw new EnvironmentSecurityException(
                    String.format( "Access to environment %s is denied", environment.getName() ) );
        }

        List<Relation> relations = relationManager.getRelationsByObject( environment );

        for ( final Relation relation : relations )
        {
            if ( !relation.getSource().equals( relation.getTarget() ) )
            {
                relationManager.removeRelation( relation.getId() );
            }
        }

        for ( final ShareDto dto : shareDto )
        {
            User activeUser = identityManager.getActiveUser();
            UserDelegate delegatedUser = identityManager.getUserDelegate( activeUser.getId() );
            User targetUser = identityManager.getUser( dto.getId() );
            UserDelegate targetDelegate = identityManager.getUserDelegate( targetUser.getId() );

            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( dto.isRead(), dto.isWrite(), dto.isUpdate(), dto.isDelete(),
                            Ownership.GROUP.getLevel() );

            RelationInfo relationInfo = relationManager.createTrustRelationship( relationInfoMeta );

            RelationMeta relationMeta =
                    new RelationMeta( delegatedUser, targetDelegate, environment, delegatedUser.getId() );

            Relation relation = relationManager.buildTrustRelation( relationInfo, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
    }
}
