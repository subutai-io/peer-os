package io.subutai.core.environment.impl;


import java.security.AccessControlException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.subutai.common.command.CommandException;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentCreationRef;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeerEventListener;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.relation.RelationInfoManager;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.RelationVerificationException;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfo;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.security.relation.model.RelationStatus;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.SecureEnvironmentManager;
import io.subutai.core.environment.api.ShareDto.ShareDto;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.adapter.BazaarEnvironment;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionListener;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.template.api.TemplateManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.bazaar.share.common.BazaaarAdapter;
import io.subutai.bazaar.share.common.BazaarEventListener;
import io.subutai.bazaar.share.quota.ContainerQuota;


@PermitAll
public class EnvironmentManagerSecureProxy extends HostListener
        implements EnvironmentManager, PeerActionListener, AlertListener, SecureEnvironmentManager, BazaarEventListener,
        LocalPeerEventListener
{
    private final EnvironmentManagerImpl environmentManager;
    private final IdentityManager identityManager;
    private final Tracker tracker;
    private RelationManager relationManager;


    public EnvironmentManagerSecureProxy( final TemplateManager templateManager, final PeerManager peerManager,
                                          SecurityManager securityManager, final IdentityManager identityManager,
                                          final Tracker tracker, final RelationManager relationManager,
                                          final BazaaarAdapter bazaaarAdapter, final EnvironmentService environmentService,
                                          final SystemManager systemManager )
    {
        Preconditions.checkNotNull( templateManager );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( relationManager );
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( tracker );
        Preconditions.checkNotNull( environmentService );
        Preconditions.checkNotNull( systemManager );

        this.relationManager = relationManager;
        this.tracker = tracker;
        this.identityManager = identityManager;
        this.environmentManager =
                getEnvironmentManager( templateManager, peerManager, securityManager, bazaaarAdapter, environmentService,
                        systemManager );
    }


    protected EnvironmentManagerImpl getEnvironmentManager( TemplateManager templateManager, PeerManager peerManager,
                                                            SecurityManager securityManager, BazaaarAdapter bazaaarAdapter,
                                                            EnvironmentService environmentService,
                                                            SystemManager systemManager )
    {
        return new EnvironmentManagerImpl( templateManager, peerManager, securityManager, identityManager, tracker,
                relationManager, bazaaarAdapter, environmentService, systemManager );
    }


    public void init()
    {
        environmentManager.init();
    }


    public void dispose()
    {
        environmentManager.dispose();
    }


    public void registerListener( final EnvironmentEventListener listener )
    {
        environmentManager.registerListener( listener );
    }


    public void unregisterListener( final EnvironmentEventListener listener )
    {
        environmentManager.unregisterListener( listener );
    }

    //security checks start


    protected Map<String, String> traitsBuilder( String traitCollection )
    {
        Map<String, String> keyValue = Maps.newHashMap();
        String[] traits = traitCollection.split( ";" );
        for ( final String trait : traits )
        {
            String[] pair = trait.split( "=" );
            keyValue.put( pair[0], pair[1] );
        }
        return keyValue;
    }


    protected void check( RelationLink source, RelationLink target, Map<String, String> traits )
            throws RelationVerificationException
    {
        RelationInfoMeta meta = new RelationInfoMeta();
        meta.setRelationTraits( traits );
        RelationInfoManager relationInfoManager = relationManager.getRelationInfoManager();
        if ( source == null )
        {
            relationInfoManager.checkRelation( target, meta, null );
        }
        else
        {
            relationInfoManager.checkRelation( source, target, meta, null );
        }
    }


    protected void check( RelationLink source, Collection<? extends RelationLink> targets, Map<String, String> traits )
    {

        for ( Iterator<?> it = targets.iterator(); it.hasNext(); )
        {
            RelationLink target = ( RelationLink ) it.next();
            try
            {
                check( source, target, traits );
            }
            catch ( RelationVerificationException ex )
            {
                it.remove();
            }
        }
    }

    //security checks end


    @Override
    public Set<Environment> getEnvironments()
    {
        Set<Environment> result = environmentManager.getEnvironments();

        // Environments created onbazaar doesn't have relation data on SS side. We have to add this in future.
        // Meantime, we just bypass the relation check.
        Set<Environment> bazaarEnvs = new HashSet<>();

        for ( Environment env : result )
        {
            if ( env instanceof BazaarEnvironment )
            {
                bazaarEnvs.add( env );
            }
        }

        check( null, result, traitsBuilder( "ownership=All;read=true" ) );

        result.addAll( bazaarEnvs );

        return result;
    }


    @PermitAll
    @Override
    public Set<Environment> getEnvironmentsByOwnerId( final long userId )
    {
        return environmentManager.getEnvironmentsByOwnerId( userId );
    }


    @Override
    @RolesAllowed( "Environment-Management|Write" )
    public EnvironmentCreationRef createEnvironment( final Topology topology, final boolean async )
            throws EnvironmentCreationException
    {
        //*********************************
        // Remove XSS vulnerability code
        topology.setEnvironmentName( StringUtil.removeHtmlAndSpecialChars( topology.getEnvironmentName(), true ) );
        //*********************************

        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topology.getEnvironmentName() ), "Invalid name" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        return environmentManager.createEnvironment( topology, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Write" )
    public EnvironmentCreationRef modifyEnvironment( final String environmentId, final Topology topology,
                                                     final Set<String> removedContainers,
                                                     final Map<String, ContainerQuota> changedContainers,
                                                     final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=Group;update=true" ) );

        return environmentManager
                .modifyEnvironment( environmentId, topology, removedContainers, changedContainers, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Write" )
    public Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                                          final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=Group;update=true" ) );

        return environmentManager.growEnvironment( environmentId, topology, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void addSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;update=true" ) );

        environmentManager.addSshKey( environmentId, sshKey, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void removeSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;update=true" ) );

        environmentManager.removeSshKey( environmentId, sshKey, async );
    }


    @Override
    public SshKeys getSshKeys( final String environmentId, final SshEncryptionType encType )
    {
        try
        {
            checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( EnvironmentNotFoundException e )
        {
            return null;
        }

        return environmentManager.getSshKeys( environmentId, encType );
    }


    @Override
    public SshKeys createSshKey( final String environmentId, final String hostname, final SshEncryptionType encType )
    {
        try
        {
            checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( EnvironmentNotFoundException e )
        {
            return null;
        }

        return environmentManager.createSshKey( environmentId, hostname, encType );
    }


    @Override
    @RolesAllowed( { "Environment-Management|Update", "System-Management|Write", "System-Management|Update" } )
    public void resetP2PSecretKey( final String environmentId, final String newP2pSecretKey,
                                   final long p2pSecretKeyTtlSec, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;update=true" ) );

        environmentManager.resetP2PSecretKey( environmentId, newP2pSecretKey, p2pSecretKeyTtlSec, async );
    }


    @Override
    @RolesAllowed( { "Environment-Management|Delete", "Tenant-Management|Delete" } )
    public void destroyEnvironment( final String environmentId, final boolean async )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Environment environment;

        try
        {
            environment = loadEnvironment( environmentId );

            // Environments created onbazaar doesn't have relation data on SS side. We have to add this in future.
            // Meantime, we just bypass the relation check.
            if ( !identityManager.isTenantManager() && !( environment instanceof BazaarEnvironment ) )
            {
                check( null, environment, traitsBuilder( "ownership=All;delete=true" ) );
            }
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        catch ( EnvironmentNotFoundException e )
        {
            //check if this is a remote environment
            environment = environmentManager.findRemoteEnvironment( environmentId );

            if ( environment == null )
            {
                throw e;
            }
        }

        environmentManager.destroyEnvironment( environmentId, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Delete" )
    public void destroyContainer( final String environmentId, final String containerId, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Environment environment = loadEnvironment( environmentId );

        // Environments created onbazaar doesn't have relation data on SS side. We have to add this in future.
        // Meantime, we just bypass the relation check.
        if ( environment instanceof BazaarEnvironment )
        {
            environmentManager.destroyContainer( environmentId, containerId, async );

            return;
        }

        try
        {
            checkContainerPermission( environmentId, containerId, traitsBuilder( "ownership=All;delete=true" ) );
        }
        catch ( ContainerHostNotFoundException e )
        {
            throw new EnvironmentModificationException( e );
        }


        environmentManager.destroyContainer( environmentId, containerId, async );
    }


    @Override
    public void cancelEnvironmentWorkflow( final String environmentId ) throws EnvironmentManagerException
    {
        environmentManager.cancelEnvironmentWorkflow( environmentId );
    }


    @Override
    public Map<String, CancellableWorkflow> getActiveWorkflows()
    {
        return environmentManager.getActiveWorkflows();
    }


    @Override
    @PermitAll
    public Environment loadEnvironment( final String environmentId ) throws EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );

        // Environment is frombazaar
        if ( environment instanceof BazaarEnvironment )
        {
            return environment;
        }

        // tenant manager can view any environment
        if ( !identityManager.isTenantManager() )
        {
            checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;read=true" ) );
        }

        return environment;
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void removeEnvironmentDomain( final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;update=true" ) );

        environmentManager.removeEnvironmentDomain( environmentId );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void assignEnvironmentDomain( final String environmentId, final String newDomain,
                                         final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;update=true" ) );

        environmentManager.assignEnvironmentDomain( environmentId, newDomain, proxyLoadBalanceStrategy, sslCertPath );
    }


    @Override
    @PermitAll
    public String getEnvironmentDomain( final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        checkEnvironmentPermission( environmentId, traitsBuilder( "ownership=All;read=true" ) );

        return environmentManager.getEnvironmentDomain( environmentId );
    }


    @Override
    @PermitAll
    public boolean isContainerInEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        try
        {
            checkContainerPermission( environmentId, containerHostId, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( ContainerHostNotFoundException e )
        {
            throw new EnvironmentManagerException( e.getMessage(), e );
        }

        return environmentManager.isContainerInEnvironmentDomain( containerHostId, environmentId );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void addContainerToEnvironmentDomain( final String containerHostId, final String environmentId,
                                                 final int port )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        try
        {
            checkContainerPermission( environmentId, containerHostId, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( ContainerHostNotFoundException e )
        {
            throw new ContainerHostNotFoundException( e.getMessage() );
        }

        environmentManager.addContainerToEnvironmentDomain( containerHostId, environmentId, port );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public SshTunnel setupSshTunnelForContainer( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        try
        {
            checkContainerPermission( environmentId, containerHostId, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( ContainerHostNotFoundException e )
        {
            throw new ContainerHostNotFoundException( e.getMessage() );
        }

        return environmentManager.setupSshTunnelForContainer( containerHostId, environmentId );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void removeContainerFromEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        try
        {
            checkContainerPermission( environmentId, containerHostId, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( ContainerHostNotFoundException e )
        {
            throw new ContainerHostNotFoundException( e.getMessage() );
        }

        environmentManager.removeContainerFromEnvironmentDomain( containerHostId, environmentId );
    }


    protected void checkEnvironmentPermission( String environmentId, Map<String, String> traits )
            throws EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );

        try
        {
            check( null, environment, traits );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
    }


    protected void checkContainerPermission( String environmentId, String containerId, Map<String, String> traits )
            throws EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );

        try
        {
            check( null, environment, traits );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }

        try
        {
            ContainerHost containerHost = environment.getContainerHostById( containerId );
            check( null, containerHost, traits );
        }
        catch ( ContainerHostNotFoundException | RelationVerificationException e )
        {
            throw new ContainerHostNotFoundException( e.getMessage() );
        }
    }


    @Override
    @PermitAll
    public void notifyOnContainerDestroyed( final Environment environment, final String containerId )
    {
        environmentManager.notifyOnContainerDestroyed( environment, containerId );
    }


    @PermitAll
    @Override
    public void notifyOnEnvironmentDestroyed( final String environmentId )
    {
        environmentManager.notifyOnEnvironmentDestroyed( environmentId );
    }


    @Override
    public void addAlertHandler( final AlertHandler alertHandler )
    {
        environmentManager.addAlertHandler( alertHandler );
    }


    @Override
    public void removeAlertHandler( final AlertHandler alertHandler )
    {
        environmentManager.removeAlertHandler( alertHandler );
    }


    @Override
    public Collection<AlertHandler> getRegisteredAlertHandlers()
    {
        return environmentManager.getRegisteredAlertHandlers();
    }


    @Override
    public EnvironmentAlertHandlers getEnvironmentAlertHandlers( final EnvironmentId environmentId )
            throws EnvironmentNotFoundException
    {
        return environmentManager.getEnvironmentAlertHandlers( environmentId );
    }


    @Override
    public List<ShareDto> getSharedUsers( final String objectId ) throws EnvironmentNotFoundException
    {
        Environment environment = loadEnvironment( objectId );

        List<Relation> relations = relationManager.getRelationsByObject( environment );
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


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void shareEnvironment( final ShareDto[] shareDto, final String environmentId )
    {
        try
        {
            Environment environment = loadEnvironment( environmentId );
            check( null, environment, traitsBuilder( "ownership=All;update=true" ) );

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
                Map<String, String> traits = relationInfoMeta.getRelationTraits();
                traits.put( "read", String.valueOf( dto.isRead() ) );
                traits.put( "write", String.valueOf( dto.isWrite() ) );
                traits.put( "update", String.valueOf( dto.isUpdate() ) );
                traits.put( "delete", String.valueOf( dto.isDelete() ) );
                traits.put( "ownership", Ownership.GROUP.getName() );

                RelationMeta relationMeta =
                        new RelationMeta( delegatedUser, targetDelegate, environment, delegatedUser.getId() );

                Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
                relation.setRelationStatus( RelationStatus.VERIFIED );
                relationManager.saveRelation( relation );
            }
        }
        catch ( RelationVerificationException | EnvironmentNotFoundException e )
        {
            throw new AccessControlException( e.getMessage() );
        }
    }


    @Override
    public void startMonitoring( final String handlerId, final AlertHandlerPriority handlerPriority,
                                 final String environmentId ) throws EnvironmentManagerException
    {
        environmentManager.startMonitoring( handlerId, handlerPriority, environmentId );
    }


    @Override
    public void stopMonitoring( final String handlerId, final AlertHandlerPriority handlerPriority,
                                final String environmentId ) throws EnvironmentManagerException
    {
        environmentManager.stopMonitoring( handlerId, handlerPriority, environmentId );
    }


    @Override
    public String getId()
    {
        return environmentManager.getId();
    }


    @Override
    public void onAlert( final AlertEvent alertEvent )
    {
        environmentManager.onAlert( alertEvent );
    }


    @Override
    public String getName()
    {
        return environmentManager.getName();
    }


    @Override
    public PeerActionResponse onPeerAction( final PeerAction peerAction )
    {
        return environmentManager.onPeerAction( peerAction );
    }


    @Override
    public void onRegistrationSucceeded()
    {
        environmentManager.onRegistrationSucceeded();
    }


    @Override
    public void onUnregister()
    {
        environmentManager.onUnregister();
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void changeContainerHostname( final ContainerId containerId, final String newHostname, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        environmentManager.changeContainerHostname( containerId, newHostname, async );
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void addSshKeyToEnvironmentEntity( final String environmentId, final String sshKey )
            throws EnvironmentNotFoundException
    {
        environmentManager.addSshKeyToEnvironmentEntity( environmentId, sshKey );
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void excludePeerFromEnvironment( final String environmentId, final String peerId )
            throws EnvironmentNotFoundException
    {
        environmentManager.excludePeerFromEnvironment( environmentId, peerId );
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void excludeContainerFromEnvironment( final String environmentId, final String containerId )
            throws EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        environmentManager.excludeContainerFromEnvironment( environmentId, containerId );
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void updateContainerHostname( final String environmentId, final String containerId, final String hostname )
            throws EnvironmentNotFoundException, PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );

        checkContainerPermission( environmentId, containerId, traitsBuilder( "ownership=All;update=true" ) );

        environmentManager.updateContainerHostname( environmentId, containerId,
                StringUtil.removeHtmlAndSpecialChars( hostname, true ) );
    }


    @Override
    @RolesAllowed( "Tenant-Management|Read" )
    public Set<EnvironmentDto> getTenantEnvironments()
    {
        return environmentManager.getTenantEnvironments();
    }


    @Override
    @PermitAll
    public boolean rhHasEnvironments( final String rhId )
    {
        return environmentManager.rhHasEnvironments( rhId );
    }


    @Override
    public String getEnvironmentOwnerName( final Environment environment )
    {
        return environmentManager.getEnvironmentOwnerName( environment );
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, final Set<QuotaAlertValue> alerts )
    {
        environmentManager.onHeartbeat( resourceHostInfo, alerts );
    }


    @Override
    public void onContainerStateChanged( final ContainerHostInfo containerInfo, final ContainerHostState previousState,
                                         final ContainerHostState currentState )
    {
        environmentManager.onContainerStateChanged( containerInfo, previousState, currentState );
    }


    @Override
    public void onContainerHostnameChanged( final ContainerHostInfo containerInfo, final String previousHostname,
                                            final String currentHostname )
    {
        environmentManager.onContainerHostnameChanged( containerInfo, previousHostname, currentHostname );
    }


    @Override
    public void onContainerCreated( final ContainerHostInfo containerInfo )
    {
        environmentManager.onContainerCreated( containerInfo );
    }


    @Override
    public void onContainerNetInterfaceChanged( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel oldNetInterface,
                                                final HostInterfaceModel newNetInterface )
    {
        environmentManager.onContainerNetInterfaceChanged( containerInfo, oldNetInterface, newNetInterface );
    }


    @Override
    public void onContainerNetInterfaceAdded( final ContainerHostInfo containerInfo,
                                              final HostInterfaceModel netInterface )
    {
        environmentManager.onContainerNetInterfaceAdded( containerInfo, netInterface );
    }


    @Override
    public void onContainerNetInterfaceRemoved( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel netInterface )
    {
        environmentManager.onContainerNetInterfaceRemoved( containerInfo, netInterface );
    }


    @Override
    public void onRhConnected( final ResourceHostInfo resourceHostInfo )
    {
        environmentManager.onRhConnected( resourceHostInfo );
    }


    @Override
    public void onRhDisconnected( final ResourceHostInfo resourceHostInfo )
    {
        environmentManager.onRhDisconnected( resourceHostInfo );
    }


    @Override
    public Set<String> getDeletedEnvironmentsFromBazaar()
    {
        return environmentManager.getDeletedEnvironmentsFromBazaar();
    }


    @Override
    public void placeEnvironmentInfoByContainerIp( final String containerIp ) throws PeerException, CommandException
    {
        environmentManager.placeEnvironmentInfoByContainerIp( containerIp );
    }


    @Override
    public void placeEnvironmentInfoByContainerId( final String environmentId, final String containerId )
            throws EnvironmentNotFoundException, ContainerHostNotFoundException, CommandException
    {
        environmentManager.placeEnvironmentInfoByContainerId( environmentId, containerId );
    }


    @Override
    public void createTemplate( final String environmentId, final String containerId, final String templateName,
                                final String version, final boolean privateTemplate )
            throws PeerException, EnvironmentNotFoundException
    {
        checkContainerPermission( environmentId, containerId, traitsBuilder( "ownership=All;read=true" ) );

        environmentManager.createTemplate( environmentId, containerId, templateName, version, privateTemplate );
    }


    @Override
    public void onContainerDestroyed( final ContainerHost containerHost )
    {
        environmentManager.onContainerDestroyed( containerHost );
    }


    @Override
    public Environment getEnvironment( final String environmentId )
    {
        return environmentManager.getEnvironment( environmentId );
    }
}
