package io.subutai.core.environment.impl;


import java.security.AccessControlException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.EnvConnectivityState;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.protocol.ReverseProxyConfig;
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
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.SecureEnvironmentManager;
import io.subutai.core.environment.api.ShareDto.ShareDto;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.adapter.ProxyEnvironment;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.hubadapter.api.HubAdapter;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionListener;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.hub.share.common.HubEventListener;
import io.subutai.hub.share.dto.PeerProductDataDto;


public class EnvironmentManagerSecureProxy
        implements EnvironmentManager, PeerActionListener, AlertListener, SecureEnvironmentManager, HubEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerSecureProxy.class );
    private final EnvironmentManagerImpl environmentManager;
    private final IdentityManager identityManager;
    private final Tracker tracker;
    private RelationManager relationManager;


    public EnvironmentManagerSecureProxy( final PeerManager peerManager, SecurityManager securityManager,
                                          final IdentityManager identityManager, final Tracker tracker,
                                          final RelationManager relationManager, HubAdapter hubAdapter,
                                          final EnvironmentService environmentService )
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( relationManager );
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( tracker );

        this.relationManager = relationManager;
        this.tracker = tracker;
        this.identityManager = identityManager;
        this.environmentManager =
                new EnvironmentManagerImpl( peerManager, securityManager, identityManager, tracker, relationManager,
                        hubAdapter, environmentService );
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


    private Map<String, String> traitsBuilder( String traitCollection )
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


    private void check( RelationLink source, RelationLink target, Map<String, String> traits )
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


    private void check( RelationLink source, Collection<? extends RelationLink> targets, Map<String, String> traits )
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


    @Override
    public Set<Environment> getEnvironments()
    {
        Set<Environment> result = environmentManager.getEnvironments();

        // Environments created on Hub doesn't have relation data on SS side. We have to add this in future.
        // Meantime, we just bypass the relation check.
        Set<Environment> hubEnvs = new HashSet<>();

        for ( Environment env : result )
        {
            if ( env instanceof ProxyEnvironment )
            {
                hubEnvs.add( env );
            }
        }

        check( null, result, traitsBuilder( "ownership=All;read=true" ) );

        result.addAll( hubEnvs );

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
    public Environment createEnvironment( final Topology topology, final boolean async )
            throws EnvironmentCreationException
    {
        return environmentManager.createEnvironment( topology, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Write" )
    public UUID createEnvironmentAndGetTrackerID( final Topology topology, final boolean async )
            throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topology.getEnvironmentName() ), "Invalid name" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        //create operation tracker
        TrackerOperation operationTracker = tracker.createTrackerOperation( EnvironmentManagerImpl.MODULE_NAME,
                String.format( "Creating environment %s ", topology.getEnvironmentName() ) );

        environmentManager.createEnvironment( topology, async, operationTracker );

        return operationTracker.getId();
    }


    @Override
    @RolesAllowed( "Environment-Management|Write" )
    public Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                                          final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=Group;update=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }

        return environmentManager.growEnvironment( environmentId, topology, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Write" )
    public UUID modifyEnvironmentAndGetTrackerID( final String environmentId, final Topology topology,
                                                  final List<String> removedContainers, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=Group;update=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        return environmentManager.modifyEnvironmentAndGetTrackerID( environmentId, topology, removedContainers, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void addSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        environmentManager.addSshKey( environmentId, sshKey, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void removeSshKey( final String environmentId, final String sshKey, final boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        environmentManager.removeSshKey( environmentId, sshKey, async );
    }


    @Override
    public SshKeys getSshKeys( final String environmentId, final SshEncryptionType encType )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );
            check( null, environment, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( RelationVerificationException | EnvironmentNotFoundException e )
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
            Environment environment = environmentManager.loadEnvironment( environmentId );
            check( null, environment, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( RelationVerificationException | EnvironmentNotFoundException e )
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
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        environmentManager.resetP2PSecretKey( environmentId, newP2pSecretKey, p2pSecretKeyTtlSec, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Delete" )
    public void destroyEnvironment( final String environmentId, final boolean async )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );

        try
        {
            // Environments created on Hub doesn't have relation data on SS side. We have to add this in future.
            // Meantime, we just bypass the relation check.
            if ( !( environment instanceof ProxyEnvironment ) )
            {
                check( null, environment, traitsBuilder( "ownership=All;delete=true" ) );
            }
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }

        environmentManager.destroyEnvironment( environmentId, async );
    }


    @Override
    @RolesAllowed( "Environment-Management|Delete" )
    public void destroyContainer( final String environmentId, final String containerId, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );

        // Environments created on Hub doesn't have relation data on SS side. We have to add this in future.
        // Meantime, we just bypass the relation check.
        if ( environment instanceof ProxyEnvironment )
        {
            environmentManager.destroyContainer( environmentId, containerId, async );

            return;
        }

        try
        {
            check( null, environment, traitsBuilder( "ownership=All;delete=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }

        try
        {
            EnvironmentContainerHost containerHost = environment.getContainerHostById( containerId );
            check( null, containerHost, traitsBuilder( "ownership=All;delete=true" ) );
        }
        catch ( RelationVerificationException | ContainerHostNotFoundException e )
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

        // Environment is from Hub
        if ( environment instanceof ProxyEnvironment )
        {
            return environment;
        }

        try
        {
            check( null, environment, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }

        return environment;
    }


    @Override
    public String getDefaultDomainName()
    {
        return environmentManager.getDefaultDomainName();
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void removeEnvironmentDomain( final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );

        try
        {
            check( null, environment, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }

        environmentManager.removeEnvironmentDomain( environmentId );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void assignEnvironmentDomain( final String environmentId, final String newDomain,
                                         final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy,
                                         final String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        environmentManager.assignEnvironmentDomain( environmentId, newDomain, proxyLoadBalanceStrategy, sslCertPath );
    }


    @Override
    @PermitAll
    public String getEnvironmentDomain( final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        return environmentManager.getEnvironmentDomain( environmentId );
    }


    @Override
    @PermitAll
    public boolean isContainerInEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        try
        {
            ContainerHost containerHost = environment.getContainerHostById( containerHostId );
            check( null, containerHost, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( ContainerHostNotFoundException | RelationVerificationException e )
        {
            throw new EnvironmentManagerException( e.getMessage(), e );
        }
        return environmentManager.isContainerInEnvironmentDomain( containerHostId, environmentId );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public void addContainerToEnvironmentDomain( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        try
        {
            ContainerHost containerHost = environment.getContainerHostById( containerHostId );
            check( null, containerHost, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( ContainerHostNotFoundException | RelationVerificationException e )
        {
            throw new ContainerHostNotFoundException( e.getMessage() );
        }
        environmentManager.addContainerToEnvironmentDomain( containerHostId, environmentId );
    }


    @Override
    @RolesAllowed( "Environment-Management|Update" )
    public SshTunnel setupSshTunnelForContainer( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        try
        {
            ContainerHost containerHost = environment.getContainerHostById( containerHostId );
            check( null, containerHost, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( ContainerHostNotFoundException | RelationVerificationException e )
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
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;read=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        try
        {
            ContainerHost containerHost = environment.getContainerHostById( containerHostId );
            check( null, containerHost, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( ContainerHostNotFoundException | RelationVerificationException e )
        {
            throw new ContainerHostNotFoundException( e.getMessage() );
        }
        environmentManager.removeContainerFromEnvironmentDomain( containerHostId, environmentId );
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
            Environment environment = environmentManager.loadEnvironment( environmentId );
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
    public void addReverseProxy( final Environment environment, final ReverseProxyConfig reverseProxyConfig )
            throws EnvironmentModificationException
    {
        environmentManager.addReverseProxy( environment, reverseProxyConfig );
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
    public void onPluginEvent( final String pluginUid, final PeerProductDataDto.State state )
    {
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public EnvConnectivityState checkEnvironmentConnectivity( final String environmentId )
            throws EnvironmentNotFoundException, EnvironmentManagerException
    {
        return environmentManager.checkEnvironmentConnectivity( environmentId );
    }
}
