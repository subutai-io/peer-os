package io.subutai.core.environment.impl;


import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerDistributionType;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.metric.AlertValue;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.Gateway;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerType;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.api.exception.EnvironmentSecurityException;
import io.subutai.core.environment.impl.dao.BlueprintDataService;
import io.subutai.core.environment.impl.dao.EnvironmentContainerDataService;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.environment.impl.entity.EnvironmentAlertHandlerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.construction.EnvironmentImportWorkflow;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.ContainerDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.EnvironmentDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.EnvironmentGrowingWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyModificationWorkflow;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.exception.RelationVerificationException;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.model.RelationMeta;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.relation.RelationManager;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionListener;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
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
    protected BlueprintDataService blueprintDataService;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Map<String, AlertHandler> alertHandlers = new ConcurrentHashMap<String, AlertHandler>();
    private SecurityManager securityManager;
    private boolean keyTrustCheckEnabled;


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
        this.blueprintDataService = new BlueprintDataService( daoManager );
        this.environmentDataService = new EnvironmentDataService( daoManager );
        this.environmentContainerDataService = new EnvironmentContainerDataService( daoManager );
    }


    public void dispose()
    {
        executor.shutdown();
    }


    public void setKeyTrustCheckEnabled( final boolean keyTrustCheckEnabled )
    {
        this.keyTrustCheckEnabled = keyTrustCheckEnabled;
    }


    public boolean isKeyTrustCheckEnabled()
    {
        return keyTrustCheckEnabled;
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


    @RolesAllowed( "Environment-Management|Write" )
    private Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Blueprint blueprint,
                                                           final boolean async, final boolean checkAccess,
                                                           TrackerOperation operationTracker )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        Preconditions.checkNotNull( blueprint, "Invalid blueprint" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !blueprint.getNodeGroups().isEmpty(), "Placement is empty" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );
        User activeUser = identityManager.getActiveUser();

        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environmentId,
                        PermissionObject.EnvironmentManagement, environment.getId() );

        if ( !relationManager.getRelationInfoManager().groupHasUpdatePermissions( relationMeta ) )
        {
            throw new EnvironmentNotFoundException();
        }
        String cdir = environment.getSubnetCidr();

        final Topology topology = buildTopology( environmentId, cdir, blueprint );


        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException(
                    String.format( "Environment status is %s", environment.getStatus() ) );
        }

        final Set<EnvironmentContainerHost> oldContainers = Sets.newHashSet( environment.getContainerHosts() );


        //launch environment growing workflow

        final EnvironmentGrowingWorkflow environmentGrowingWorkflow =
                getEnvironmentGrowingWorkflow( environment, topology, environment.getSshKey(), operationTracker );

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
                    notifyOnEnvironmentGrown( loadEnvironment( environment.getId(), checkAccess ), newContainers );
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

            if ( environmentGrowingWorkflow.getError() != null )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( environmentGrowingWorkflow.getError() ) );
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


    @RolesAllowed( "Environment-Management|Delete" )
    public void destroyEnvironment( final String environmentId, boolean async, final boolean forceMetadataRemoval,
                                    final boolean checkAccess, final TrackerOperation operationTracker )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );
        User activeUser = identityManager.getActiveUser();

        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environmentId,
                        PermissionObject.EnvironmentManagement, environment.getId() );

        if ( !relationManager.getRelationInfoManager().allHasDeletePermissions( relationMeta ) )
        {
            throw new EnvironmentNotFoundException();
        }
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


    @PermitAll
    @Override
    public Set<Environment> getEnvironments()
    {
        User activeUser = identityManager.getActiveUser();

        final boolean viewAll = identityManager
                .isUserPermitted( activeUser, PermissionObject.EnvironmentManagement, PermissionScope.ALL_SCOPE,
                        PermissionOperation.Read );


        Set<Environment> environments = new HashSet<>();
        for ( Environment environment : environmentDataService.getAll() )
        {
            RelationMeta relationMeta = new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment,
                    environment.getId(), PermissionObject.EnvironmentManagement, environment.getId() );
            boolean trustedRelation = relationManager.getRelationInfoManager().allHasReadPermissions( relationMeta );
            if ( viewAll || environment.getUserId().equals( activeUser.getId() ) || trustedRelation )
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
    public Environment setupRequisites( final Blueprint blueprint ) throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( blueprint, "Invalid blueprint" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( blueprint.getName() ), "Invalid name" );
        //        Preconditions.checkArgument( !Strings.isNullOrEmpty( blueprint.getCidr() ), "Invalid subnet CIDR" );
        Preconditions.checkArgument( !blueprint.getNodeGroups().isEmpty(), "Placement is empty" );

        String cidr = calculateCidr( blueprint );

        //create empty environment
        return createEmptyEnvironment( blueprint.getName(), cidr, blueprint.getSshKey(), blueprint );
    }


    @Override
    @RolesAllowed( "Environment-Management|Write" )
    public Environment startEnvironmentBuild( String environmentId, final String signedMessage, boolean async )
            throws EnvironmentCreationException
    {
        //create empty environment
        final EnvironmentImpl environment = environmentDataService.find( environmentId );

        // TODO should be handled on server side when user sends signed message
        try
        {
            relationManager.processTrustMessage( signedMessage, environment.getId() );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentCreationException( e );
        }

        Blueprint blueprint = JsonUtil.fromJson( environment.getRawBlueprint(), Blueprint.class );
        String cidr = calculateCidr( blueprint );

        // TODO add additional step for receiving trust message

        Topology topology = buildTopology( environment.getId(), cidr, blueprint );

        //create operation tracker
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Creating environment %s ", environment.getId() ) );

        //launch environment creation workflow
        final EnvironmentCreationWorkflow environmentCreationWorkflow =
                getEnvironmentCreationWorkflow( environment, topology, blueprint.getSshKey(), operationTracker );

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

            if ( environmentCreationWorkflow.getError() != null )
            {
                throw new EnvironmentCreationException(
                        exceptionUtil.getRootCause( environmentCreationWorkflow.getError() ) );
            }
        }

        //return created environment
        return environment;
    }


    protected Topology buildTopology( final String environmentId, final String cdir, final Blueprint blueprint )
    {
        Topology topology = new Topology( blueprint.getName(), environmentId, cdir, blueprint.getSshKey() );

        LOG.debug( "Building topology..." );
        for ( Map.Entry<String, Set<NodeGroup>> placementEntry : blueprint.getNodeGroupsMap().entrySet() )
        {
            Peer peer = peerManager.getPeer( placementEntry.getKey() );
            for ( NodeGroup nodeGroup : placementEntry.getValue() )
            {
                LOG.debug( String.format( "%s %s %s %s %s %s", nodeGroup.getName(), nodeGroup.getType(),
                        nodeGroup.getNumberOfContainers(), nodeGroup.getPeerId(),
                        nodeGroup.getContainerDistributionType() == ContainerDistributionType.AUTO ?
                        nodeGroup.getContainerPlacementStrategy().getStrategyId() : nodeGroup.getHostId(),
                        nodeGroup.getContainerDistributionType() ) );
                topology.addNodeGroupPlacement( peer, nodeGroup );
            }
        }
        LOG.debug( "Topology built." );

        return topology;
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public Environment createEnvironment( final Blueprint blueprint, final boolean async )
            throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( blueprint, "Invalid blueprint" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( blueprint.getName() ), "Invalid name" );
        //        Preconditions.checkArgument( !Strings.isNullOrEmpty( blueprint.getCidr() ), "Invalid subnet CIDR" );
        Preconditions.checkArgument( !blueprint.getNodeGroups().isEmpty(), "Placement is empty" );

        String cidr = calculateCidr( blueprint );

        //create empty environment
        final EnvironmentImpl environment =
                createEmptyEnvironment( blueprint.getName(), cidr, blueprint.getSshKey(), blueprint );

        // TODO add additional step for receiving trust message

        Topology topology = buildTopology( environment.getId(), cidr, blueprint );

        //create operation tracker
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Creating environment %s ", environment.getId() ) );

        //launch environment creation workflow
        final EnvironmentCreationWorkflow environmentCreationWorkflow =
                getEnvironmentCreationWorkflow( environment, topology, blueprint.getSshKey(), operationTracker );

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

            if ( environmentCreationWorkflow.getError() != null )
            {
                throw new EnvironmentCreationException(
                        exceptionUtil.getRootCause( environmentCreationWorkflow.getError() ) );
            }
        }

        //return created environment
        return environment;
    }


    // TODO refactor to pass one Blueprint parameter from subutai-common
    @Override
    public Environment importEnvironment( final String name, final Topology topology,
                                          final Map<NodeGroup, Set<ContainerHostInfo>> containers, final String ssh,
                                          final Integer vlan ) throws EnvironmentCreationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        Map.Entry<NodeGroup, Set<ContainerHostInfo>> containersEntry = containers.entrySet().iterator().next();
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
        final EnvironmentImpl environment = createEmptyEnvironment( name, ip, ssh, null );
        for ( Map.Entry<NodeGroup, Set<ContainerHostInfo>> entry : containers.entrySet() )
        {
            for ( ContainerHostInfo newHost : entry.getValue() )
            {
                ContainerType containerType = entry.getKey().getType();

                environment.addContainers( Sets.newHashSet(
                        new EnvironmentContainerImpl( peerManager.getLocalPeer().getId(), peerManager.getLocalPeer(),
                                entry.getKey().getName(), new ContainerHostInfoModel( newHost ),
                                templateRegistry.getTemplate( entry.getKey().getTemplateName() ),
                                entry.getKey().getSshGroupId(), entry.getKey().getHostsGroupId(),
                                Common.DEFAULT_DOMAIN_NAME, containerType ) ) );
            }
        }
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Creating environment %s ", environment.getId() ) );

        EnvironmentImportWorkflow environmentImportWorkflow =
                getEnvironmentImportWorkflow( environment, topology, ssh, operationTracker );

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
    public Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Blueprint blueprint,
                                                          final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        TrackerOperation operationTracker =
                tracker.createTrackerOperation( MODULE_NAME, String.format( "Growing environment %s", environmentId ) );

        return growEnvironment( environmentId, blueprint, async, true, operationTracker );
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public void setSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        TrackerOperation op = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Setting environment %s ssh key", environmentId ) );

        setSshKey( environmentId, sshKey, async, true, op );
    }


    public void setSshKey( final String environmentId, final String sshKey, final boolean async,
                           final boolean checkAccess, final TrackerOperation operationTracker )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );
        User activeUser = identityManager.getActiveUser();

        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environmentId,
                        PermissionObject.EnvironmentManagement, environment.getId() );
        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( relationMeta ) )
        {
            throw new EnvironmentNotFoundException();
        }
        final SshKeyModificationWorkflow sshKeyModificationWorkflow =
                getSshKeyModificationWorkflow( environment, sshKey, networkManager, operationTracker );

        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                sshKeyModificationWorkflow.start();
            }
        } );

        //wait
        if ( !async )
        {
            sshKeyModificationWorkflow.join();

            if ( sshKeyModificationWorkflow.getError() != null )
            {
                throw new EnvironmentModificationException(
                        exceptionUtil.getRootCause( sshKeyModificationWorkflow.getError() ) );
            }
        }
    }


    protected SshKeyModificationWorkflow getSshKeyModificationWorkflow( final EnvironmentImpl environment,
                                                                        final String sshKey,
                                                                        final NetworkManager networkManager,
                                                                        final TrackerOperation operationTracker )
    {
        return new SshKeyModificationWorkflow( environment, sshKey, networkManager, operationTracker, this );
    }


    @PermitAll
    protected Environment loadEnvironment( final String environmentId, boolean checkAccess )
            throws EnvironmentNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        EnvironmentImpl environment = environmentDataService.find( environmentId );

        if ( environment == null )
        {
            throw new EnvironmentNotFoundException();
        }

        //check access to environment
        if ( checkAccess )
        {
            checkAccess( environment );
        }

        //set environment's transient fields
        setEnvironmentTransientFields( environment );

        //set container's transient fields
        setContainersTransientFields( environment );

        return environment;
    }


    protected void checkAccess( final Environment environment )
    {
        if ( !( isUserAdmin() || Objects.equals( environment.getUserId(), getUserId() ) ) )
        {
            throw new EnvironmentSecurityException(
                    String.format( "Access to environment %s is denied", environment.getName() ) );
        }
        User activeUser = identityManager.getActiveUser();

        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environment.getId(),
                        PermissionObject.EnvironmentManagement, environment.getId() );

        if ( !relationManager.getRelationInfoManager().allHasReadPermissions( relationMeta ) )
        {
            throw new EnvironmentSecurityException(
                    String.format( "Access to environment %s is denied", environment.getName() ) );
        }
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
        TrackerOperation op = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Destroying environment %s", environmentId ) );

        destroyEnvironment( environmentId, async, forceMetadataRemoval, true, op );
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void destroyContainer( final String environmentId, final String containerId, final boolean async,
                                  final boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ), "Invalid container id" );

        TrackerOperation op =
                tracker.createTrackerOperation( MODULE_NAME, String.format( "Destroying container %s", containerId ) );

        destroyContainer( environmentId, containerId, async, forceMetadataRemoval, true, op );
    }


    @RolesAllowed( "Environment-Management|Delete" )
    public void destroyContainer( final String environmentId, final String containerId, final boolean async,
                                  final boolean forceMetadataRemoval, final boolean checkAccess,
                                  final TrackerOperation operationTracker )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ), "Invalid container id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        User activeUser = identityManager.getActiveUser();

        final boolean deleteAll = identityManager
                .isUserPermitted( activeUser, PermissionObject.EnvironmentManagement, PermissionScope.ALL_SCOPE,
                        PermissionOperation.Delete );
        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environmentId,
                        PermissionObject.EnvironmentManagement, environment.getId() );
        boolean canDelete = relationManager.getRelationInfoManager().allHasDeletePermissions( relationMeta );
        if ( !( deleteAll || environment.getUserId().equals( activeUser.getId() ) || canDelete ) )
        {
            throw new AccessControlException( "You have not enough permissions." );
        }


        if ( environment.getStatus() == EnvironmentStatus.UNDER_MODIFICATION )
        {
            operationTracker.addLogFailed( String.format( "Environment status is %s", environment.getStatus() ) );

            throw new EnvironmentModificationException( String.format( "Environment status is %s", environment.getStatus() ) );
        }

        ContainerHost environmentContainer;
        try
        {
            environmentContainer = environment.getContainerHostById( containerId );
            relationMeta = new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environmentContainer,
                    containerId, PermissionObject.EnvironmentManagement, environmentContainer.getId() );
            if ( !relationManager.getRelationInfoManager().allHasDeletePermissions( relationMeta ) )
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
                throw new EnvironmentModificationException( exceptionUtil.getRootCause( containerDestructionWorkflow.getError() ) );
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
        return loadEnvironment( environmentId, false );
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

        removeEnvironment( environmentId, true );
    }


    @RolesAllowed( "Environment-Management|Delete" )
    public void removeEnvironment( final String environmentId, final boolean checkAccess )
            throws EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        Environment environment = loadEnvironment( environmentId, checkAccess );

        User activeUser = identityManager.getActiveUser();
        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environmentId,
                        PermissionObject.EnvironmentManagement, environment.getId() );
        boolean canDelete = relationManager.getRelationInfoManager().allHasDeletePermissions( relationMeta );

        final boolean deleteAll = identityManager
                .isUserPermitted( activeUser, PermissionObject.EnvironmentManagement, PermissionScope.ALL_SCOPE,
                        PermissionOperation.Delete );

        if ( deleteAll || environment.getUserId().equals( activeUser.getId() ) || canDelete )
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
    public void saveBlueprint( final Blueprint blueprint ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( blueprint, "Invalid blueprint" );

        blueprintDataService.persist( blueprint );
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public Blueprint getBlueprint( final UUID id ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( id, "Blueprint ID could not be null" );
        return blueprintDataService.find( id );
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void removeBlueprint( final UUID blueprintId ) throws EnvironmentManagerException
    {
        Preconditions.checkNotNull( blueprintId, "Invalid blueprint id" );

        blueprintDataService.remove( blueprintId );
    }


    @PermitAll
    @Override
    public Set<Blueprint> getBlueprints() throws EnvironmentManagerException
    {
        return blueprintDataService.getAll();
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

                    User activeUser = identityManager.getActiveUser();

                    RelationMeta relationMeta =
                            new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environmentContainer,
                                    environmentContainer.getId(), PermissionObject.EnvironmentManagement,
                                    environmentContainer.getId() );

                    if ( !relationManager.getRelationInfoManager().allHasDeletePermissions( relationMeta ) )
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
        User activeUser = identityManager.getActiveUser();

        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environmentId,
                        PermissionObject.EnvironmentManagement, environment.getId() );

        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( relationMeta ) )
        {
            return;
        }

        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Removing environment %s domain", environmentId ) );

        modifyEnvironmentDomain( environmentId, null, null, operationTracker, true, null );
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
        User activeUser = identityManager.getActiveUser();

        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environmentId,
                        PermissionObject.EnvironmentManagement, environment.getId() );

        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( relationMeta ) )
        {
            throw new EnvironmentNotFoundException();
        }


        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Assigning environment %s domain", environmentId ) );

        modifyEnvironmentDomain( environmentId, newDomain, domainLoadBalanceStrategy, operationTracker, true,
                sslCertPath );
    }


    public void modifyEnvironmentDomain( final String environmentId, final String domain,
                                         final DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                                         final TrackerOperation operationTracker, boolean checkAccess,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkNotNull( operationTracker );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        try
        {
            if ( Strings.isNullOrEmpty( domain ) )
            {
                peerManager.getLocalPeer().removeVniDomain( environment.getVni() );
            }
            else
            {
                peerManager.getLocalPeer()
                           .setVniDomain( environment.getVni(), domain, domainLoadBalanceStrategy, sslCertPath );
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

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, true );

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

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, true );

        try
        {
            ContainerHost containerHost = environment.getContainerHostById( containerHostId );

            return peerManager.getLocalPeer().isIpInVniDomain(
                    containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ), environment.getVni() );
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
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Adding container %s to environment domain", containerHostId ) );

        toggleContainerDomain( containerHostId, environmentId, true, operationTracker, true );
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public int setupContainerSsh( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, true );
        User activeUser = identityManager.getActiveUser();

        EnvironmentContainerImpl environmentContainer = environmentContainerDataService.find( containerHostId );
        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environmentContainer,
                        containerHostId, PermissionObject.EnvironmentManagement, environmentContainer.getId() );

        if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( relationMeta ) )
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
    public void removeContainerFromEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        TrackerOperation operationTracker = tracker.createTrackerOperation( MODULE_NAME,
                String.format( "Removing container %s from environment domain", containerHostId ) );

        toggleContainerDomain( containerHostId, environmentId, false, operationTracker, true );
    }


    public void toggleContainerDomain( final String containerHostId, final String environmentId, final boolean add,
                                       final TrackerOperation operationTracker, final boolean checkAccess )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ), "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        final EnvironmentImpl environment = ( EnvironmentImpl ) loadEnvironment( environmentId, checkAccess );

        ContainerHost containerHost = environment.getContainerHostById( containerHostId );
        if ( checkAccess )
        {
            User activeUser = identityManager.getActiveUser();

            RelationMeta relationMeta =
                    new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), containerHost,
                            containerHost.getId(), PermissionObject.EnvironmentManagement, containerHost.getId() );

            if ( !relationManager.getRelationInfoManager().allHasUpdatePermissions( relationMeta ) )
            {
                throw new EnvironmentSecurityException(
                        String.format( "Access to container %s is denied", environment.getName() ) );
            }
        }
        try
        {
            if ( add )
            {
                peerManager.getLocalPeer()
                           .addIpToVniDomain( containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ),
                                   environment.getVni() );
            }
            else
            {
                peerManager.getLocalPeer().removeIpFromVniDomain(
                        containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ),
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
                                                                      final Topology topology, final String sshKey,
                                                                      final TrackerOperation tracker )
    {
        return new EnvironmentImportWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, this, networkManager,
                peerManager, securityManager, identityManager, environment, topology, sshKey, tracker );
    }


    @PermitAll
    protected EnvironmentCreationWorkflow getEnvironmentCreationWorkflow( final EnvironmentImpl environment,
                                                                          final Topology topology, final String sshKey,
                                                                          final TrackerOperation operationTracker )
    {
        User activeUser = identityManager.getActiveUser();

        RelationMeta relationMeta =
                new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment, environment.getId(),
                        PermissionObject.EnvironmentManagement, environment.getId() );

        if ( !relationManager.getRelationInfoManager().allHasReadPermissions( relationMeta ) )
        {
            throw new AccessControlException( "You don't have enough permissions to create environment" );
        }
        return new EnvironmentCreationWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, this, networkManager,
                peerManager, securityManager, identityManager, environment, topology, sshKey, operationTracker );
    }


    @RolesAllowed( "Environment-Management|Write" )
    protected EnvironmentImpl createEmptyEnvironment( final String name, final String subnetCidr, final String sshKey,
                                                      final Blueprint blueprint ) throws EnvironmentCreationException
    {

        EnvironmentImpl environment =
                new EnvironmentImpl( name, subnetCidr, sshKey, getUserId(), peerManager.getLocalPeer().getId() );

        User activeUser = identityManager.getActiveUser();
        environment.setRawBlueprint( JsonUtil.toJson( blueprint ) );
        environment.setUserId( activeUser.getId() );
        createEnvironmentKeyPair( environment.getEnvironmentId(), activeUser.getSecurityKeyId() );
        try
        {
            KeyManager keyManager = securityManager.getKeyManager();
            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            // TODO user should send signed trust message
            RelationInfo relationInfo = relationManager
                    .generateTrustRelationship( PermissionObject.EnvironmentManagement.getName(),
                            Sets.newHashSet( PermissionOperation.Delete.getName(), PermissionOperation.Read.getName(),
                                    PermissionOperation.Update.getName(), PermissionOperation.Write.getName() ),
                            Ownership.USER.getLevel() );

            // TODO set target public key id to verify access it later to verify signature
            RelationMeta relationMeta = new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environment,
                    environment.getId(), PermissionObject.EnvironmentManagement, activeUser.getSecurityKeyId() );

            Relation relation = relationManager.buildTrustRelation( relationInfo, relationMeta );

            String relationJson = JsonUtil.toJson( relation );

            PGPPublicKey publicKey = keyManager.getPublicKey( environment.getEnvironmentId().getId() );
            byte[] relationEncrypted = encryptionTool.encrypt( relationJson.getBytes(), publicKey, true );

            String encryptedMessage = new String( relationEncrypted, "UTF-8" );

            // relation declaration is created only once so if user signature verification is failed then environment
            // creation have to fail. Declaration will be saved in encrypted format where relation information is saved
            environment.setRelationDeclaration( encryptedMessage );

            // TODO should be handled on client side
            //            PGPSecretKey userSecretKey = keyManager.getSecretKey( activeUser.getSecurityKeyId() );
            //            byte[] signedEncrypted = encryptionTool.clearSign( encryptedMessage.getBytes(),
            // userSecretKey, "" );
            //            String signedMessage = new String( signedEncrypted, "UTF-8" );


            // TODO should be handled on server side when user sends signed message
            //            relationManager.processTrustMessage( signedMessage, environment.getId() );
        }
        catch ( Exception e )
        {
            LOG.warn( "Error message.", e );
        }

        environment = saveOrUpdate( environment );

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

            //***************Sign Keys *********************************************************
            // User private key is no longer in system
            //            securityManager.getKeyManager().setKeyTrust( userSecKeyRing, pubRing, KeyTrustLevel.Full
            // .getId() );

            return secRing;
        }
        catch ( PGPException ex )
        {
            throw new EnvironmentCreationException( ex );
        }
    }

    private String calculateCidr( final Blueprint blueprint ) throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( blueprint );

        try
        {
            Set<String> usedIps = new HashSet<>();
            usedIps.addAll( getUsedIps( peerManager.getLocalPeer() ) );
            for ( String peerId : blueprint.getNodeGroupsMap().keySet() )
            {
                Peer peer = peerManager.getPeer( peerId );
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
        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {
            EnvironmentContainerImpl environmentContainer = ( EnvironmentContainerImpl ) containerHost;

            RelationMeta relationMeta =
                    new RelationMeta( activeUser, String.valueOf( activeUser.getId() ), environmentContainer,
                            environmentContainer.getId(), PermissionObject.EnvironmentManagement,
                            environmentContainer.getId() );
            boolean trustedRelation = relationManager.getRelationInfoManager().allHasReadPermissions( relationMeta );

            if ( trustedRelation )
            {
                environmentContainer.setEnvironmentManager( this );

                String peerId = environmentContainer.getPeerId();
                Peer peer = peerManager.getPeer( peerId );

                environmentContainer.setPeer( peer );
            }
            else
            {
                environment.getContainerHosts().remove( environmentContainer );
            }
        }
    }


    protected EnvironmentGrowingWorkflow getEnvironmentGrowingWorkflow( final EnvironmentImpl environment,
                                                                        final Topology topology, final String sshKey,
                                                                        final TrackerOperation operationTracker )
    {
        return new EnvironmentGrowingWorkflow( Common.DEFAULT_DOMAIN_NAME, templateRegistry, networkManager,
                peerManager, environment, topology, sshKey, operationTracker, this );
    }


    protected EnvironmentDestructionWorkflow getEnvironmentDestructionWorkflow( final PeerManager peerManager, final EnvironmentManagerImpl
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
        saveOrUpdate( environment );
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


    @RolesAllowed( "Environment-Management|A|Write" )
    protected EnvironmentImpl createEmptyEnvironment( final String name, final String subnetCidr, final String sshKey )
    {

        EnvironmentImpl environment =
                new EnvironmentImpl( name, subnetCidr, sshKey, getUserId(), peerManager.getLocalPeer().getId() );

        environment.setUserId( identityManager.getActiveUser().getId() );
        environment = saveOrUpdate( environment );

        setEnvironmentTransientFields( environment );

        notifyOnEnvironmentCreated( environment );

        return environment;
    }


    protected Long getUserId()
    {
        return ( long ) 0;//getUser().getId();
    }


    public Peer resolvePeer( final String peerId )
    {
        return peerManager.getPeer( peerId );
    }


    public EnvironmentImpl saveOrUpdate( final Environment environment )
    {
        EnvironmentImpl env = environmentDataService.saveOrUpdate( environment );
        setEnvironmentTransientFields( env );
        setContainersTransientFields( env );
        return env;
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
                if ( alertValue != null )
                {
                    alertEvent.addLog(
                            String.format( "Invoking pre-processor of '%s:%s'.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    handler.preProcess( alertValue );
                    alertEvent.addLog(
                            String.format( "Pre-processor of '%s:%s' finished.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    alertEvent.addLog(
                            String.format( "Invoking main processor of '%s:%s'.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    handler.process( alertValue );
                    alertEvent.addLog(
                            String.format( "Main processor of '%s:%s' finished.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    alertEvent.addLog(
                            String.format( "Invoking post-processor of '%s:%s'.", handlerId.getAlertHandlerId(),
                                    handlerId.getAlertHandlerPriority() ) );
                    handler.postProcess( alertValue );
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

/*
            //make sure subscriber id is truncated to 100 characters
            String trimmedSubscriberId = StringUtil.trimToSize( handlerId, Constants.MAX_SUBSCRIBER_ID_LEN );
*/
        AlertHandler alertHandler = alertHandlers.get( handlerId );
        if ( alertHandler == null )
        {
            throw new EnvironmentManagerException( "Alert handler not found." );
        }
        try
        {
            Environment environment = loadEnvironment( environmentId );

            environment.addAlertHandler( new EnvironmentAlertHandlerImpl( handlerId, handlerPriority ) );

            saveOrUpdate( environment );
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

/*
            //make sure subscriber id is truncated to 100 characters
            String trimmedSubscriberId = StringUtil.trimToSize( handlerId, Constants.MAX_SUBSCRIBER_ID_LEN );
*/
        //remove subscription from database
        try
        {
            Environment environment = environmentDataService.find( environmentId );
            environment.removeAlertHandler( new EnvironmentAlertHandlerImpl( handlerId, handlerPriority ) );
            environmentDataService.saveOrUpdate( environment );
        }
        catch ( Exception e )
        {
            LOG.error( "Error on stop monitoring", e );
            throw new EnvironmentManagerException( e.getMessage(), e );
        }
    }
}
