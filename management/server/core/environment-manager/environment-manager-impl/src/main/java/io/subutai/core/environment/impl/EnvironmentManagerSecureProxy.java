package io.subutai.core.environment.impl;


import java.security.AccessControlException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.DomainLoadBalanceStrategy;
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
import io.subutai.common.security.relation.RelationInfoManager;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.RelationVerificationException;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.ShareDto.ShareDto;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.hubadapter.api.HubAdapter;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionListener;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.tracker.api.Tracker;


public class EnvironmentManagerSecureProxy implements EnvironmentManager, PeerActionListener, AlertListener
{
    private final EnvironmentManagerImpl environmentManager;
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

        check( null, result, traitsBuilder( "ownership=All;read=true" ) );

        return result;
    }


    @Override
    public Set<Environment> getEnvironmentsByOwnerId( final long userId )
    {
        return environmentManager.getEnvironmentsByOwnerId( userId );
    }


    @Override
    public Environment createEnvironment( final Topology topology, final boolean async )
            throws EnvironmentCreationException
    {
        return environmentManager.createEnvironment( topology, async );
    }


    @Override
    public UUID createEnvironmentAndGetTrackerID( final Topology topology, final boolean async )
            throws EnvironmentCreationException
    {
        return environmentManager.createEnvironmentAndGetTrackerID( topology, async );
    }


    @Override
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
    public void destroyEnvironment( final String environmentId, final boolean async )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
        try
        {
            check( null, environment, traitsBuilder( "ownership=All;delete=true" ) );
        }
        catch ( RelationVerificationException e )
        {
            throw new EnvironmentNotFoundException();
        }
        environmentManager.destroyEnvironment( environmentId, async );
    }


    @Override
    public void destroyContainer( final String environmentId, final String containerId, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );
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
    public Environment loadEnvironment( final String environmentId ) throws EnvironmentNotFoundException
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

        return environment;
    }


    @Override
    public String getDefaultDomainName()
    {
        return environmentManager.getDefaultDomainName();
    }


    @Override
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
    public void assignEnvironmentDomain( final String environmentId, final String newDomain,
                                         final DomainLoadBalanceStrategy domainLoadBalanceStrategy,
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
        environmentManager.assignEnvironmentDomain( environmentId, newDomain, domainLoadBalanceStrategy, sslCertPath );
    }


    @Override
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
        return environmentManager.isContainerInEnvironmentDomain( environmentId, containerHostId );
    }


    @Override
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
    public int setupSshTunnelForContainer( final String containerHostId, final String environmentId )
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
    public void notifyOnContainerDestroyed( final Environment environment, final String containerId )
    {
        environmentManager.notifyOnContainerDestroyed( environment, containerId );
    }


    @Override
    public void notifyOnContainerStateChanged( final Environment environment, final ContainerHost containerHost )
    {
        environmentManager.notifyOnContainerStateChanged( environment, containerHost );
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
        return environmentManager.getSharedUsers( objectId );
    }


    @Override
    public void shareEnvironment( final ShareDto[] shareDto, final String environmentId )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );
            check( null, environment, traitsBuilder( "ownership=All;update=true" ) );
        }
        catch ( RelationVerificationException | EnvironmentNotFoundException e )
        {
            throw new AccessControlException( e.getMessage() );
        }
        environmentManager.shareEnvironment( shareDto, environmentId );
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
}
