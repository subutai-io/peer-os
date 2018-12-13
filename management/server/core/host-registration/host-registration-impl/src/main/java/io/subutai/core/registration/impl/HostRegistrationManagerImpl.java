package io.subutai.core.registration.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.settings.Common;
import io.subutai.common.util.IPUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.exception.HostRegistrationException;
import io.subutai.core.registration.api.service.ContainerInfo;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.impl.dao.RequestDataService;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;


//TODO add security annotation
public class HostRegistrationManagerImpl extends HostListener implements HostRegistrationManager
{
    private static final Logger LOG = LoggerFactory.getLogger( HostRegistrationManagerImpl.class );
    private SecurityManager securityManager;
    RequestDataService requestDataService;
    private DaoManager daoManager;
    ServiceLocator serviceLocator = new ServiceLocator();
    private ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    ExpiringCache<String, Boolean> tokenCache = new ExpiringCache<>();


    public HostRegistrationManagerImpl( final SecurityManager securityManager, final DaoManager daoManager )
    {
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( daoManager );

        this.securityManager = securityManager;
        this.daoManager = daoManager;
    }


    public void init()
    {
        requestDataService = new RequestDataService( daoManager );
        cleaner.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {
                for ( RequestedHostImpl requestedHost : requestDataService.getAll() )
                {
                    if ( requestedHost.getStatus() == ResourceHostRegistrationStatus.REQUESTED &&
                            System.currentTimeMillis() - ( requestedHost.getDateUpdated() == null ? 0L :
                                                           requestedHost.getDateUpdated() ) > TimeUnit.MINUTES
                                    .toMillis( 60 ) )
                    {
                        LOG.warn( "Deleting stale registration request {} : {}", requestedHost.getHostname(),
                                requestedHost.getAddress() );

                        requestDataService.remove( requestedHost.getId() );
                    }
                }
            }
        }, 3, 30, TimeUnit.MINUTES );
    }


    @Override
    public String generateContainerToken( final long ttlInMs )
    {
        Preconditions.checkArgument( ttlInMs > 0, "Invalid ttl" );

        String token = UUID.randomUUID().toString();

        tokenCache.put( token, true, ttlInMs );

        return token;
    }


    @Override
    public Boolean verifyTokenAndRegisterKey( final String token, String containerHostId, String publicKey )
            throws HostRegistrationException
    {

        if ( !tokenCache.keyExists( token ) )
        {
            return false;
        }

        try
        {
            securityManager.getKeyManager()
                           .savePublicKeyRing( containerHostId, SecurityKeyType.CONTAINER_HOST_KEY.getId(), publicKey );
        }
        catch ( Exception e )
        {
            LOG.error( "Error verifying token", e );

            throw new HostRegistrationException( "Failed to store container pubkey", e );
        }

        return true;
    }


    public void dispose()
    {
        cleaner.shutdown();
    }


    RequestDataService getRequestDataService()
    {
        return requestDataService;
    }


    @Override
    public void changeRhHostname( final String rhId, String hostname ) throws HostRegistrationException
    {
        try
        {
            LocalPeer localPeer = serviceLocator.getService( LocalPeer.class );

            localPeer.setRhHostname( rhId, hostname );
        }
        catch ( Exception e )
        {
            LOG.error( "Error changing RH hostname", e );

            throw new HostRegistrationException( e );
        }
    }


    @Override
    public List<RequestedHost> getRequests()
    {
        List<RequestedHost> requests = Lists.newArrayList();

        requests.addAll( requestDataService.getAll() );

        return requests;
    }


    @Override
    public RequestedHost getRequest( final String requestId )
    {
        return requestDataService.find( requestId );
    }


    @RolesAllowed( "Resource-Management|Write" )
    @Override
    public synchronized void queueRequest( final RequestedHost requestedHost ) throws HostRegistrationException
    {
        Preconditions.checkNotNull( requestedHost, "'Invalid registration request" );
        Preconditions
                .checkArgument( PGPKeyUtil.isValidPublicKeyring( requestedHost.getPublicKey() ), "Invalid public key" );

        Preconditions.checkArgument( !StringUtils.isBlank( requestedHost.getId() ), "Invalid host id" );

        try
        {
            RequestedHostImpl requestedHostImpl = requestDataService.find( requestedHost.getId() );

            if ( requestedHostImpl != null )
            {
                LOG.info( "Already requested registration" );

                //update hostname
                requestedHostImpl.setHostname( requestedHost.getHostname() );

                //update containers
                requestedHostImpl.setHostInfos( requestedHost.getHostInfos() );

                //update ip
                requestedHostImpl.setAddress( requestedHost.getAddress() );

                requestedHostImpl.refreshDateUpdated();

                requestDataService.update( requestedHostImpl );
            }
            else
            {
                RequestedHostImpl registrationRequest = new RequestedHostImpl( requestedHost );

                registrationRequest.setStatus( ResourceHostRegistrationStatus.REQUESTED );

                requestDataService.update( registrationRequest );

                checkManagement( registrationRequest );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error queueing registration request", e );

            throw new HostRegistrationException( e );
        }
    }


    @Override
    public void rejectRequest( final String requestId ) throws HostRegistrationException
    {
        try
        {
            RequestedHostImpl registrationRequest = requestDataService.find( requestId );
            registrationRequest.setStatus( ResourceHostRegistrationStatus.REJECTED );
            requestDataService.update( registrationRequest );
        }
        catch ( Exception e )
        {
            LOG.error( "Error rejecting registration request", e );

            throw new HostRegistrationException( e );
        }
    }


    @Override
    public void approveRequest( final String requestId ) throws HostRegistrationException
    {
        try
        {
            RequestedHostImpl registrationRequest = requestDataService.find( requestId );

            if ( registrationRequest == null || !ResourceHostRegistrationStatus.REQUESTED
                    .equals( registrationRequest.getStatus() ) )
            {
                return;
            }


            //check if request contains containers of other peer
            //we need to check each (not manually created) container for registration with local peer
            //manually created containers have 10.* subnet (env containers have 172.* subnet)
            //if RH has containers with 172.* and not registered with local peer, deny approval
            Set<ContainerHostInfo> alienContainers = Sets.newHashSet();
            LocalPeer localPeer = serviceLocator.getService( LocalPeer.class );
            for ( ContainerHostInfo containerInfo : registrationRequest.getContainers() )
            {
                HostInterfaceModel iface =
                        containerInfo.getHostInterfaces().findByName( Common.DEFAULT_CONTAINER_INTERFACE );
                if ( IPUtil.isIpValid( iface ) && iface.getIp().matches( "172.*" ) )
                {
                    //check this container
                    try
                    {
                        localPeer.getContainerHostById( containerInfo.getId() );
                    }
                    catch ( HostNotFoundException e )
                    {
                        //the container is not registered with us
                        alienContainers.add( containerInfo );
                    }
                }
            }

            if ( !alienContainers.isEmpty() )
            {
                StringBuilder errMsg = new StringBuilder( "Can not approve, the host has containers of other peer: " );
                for ( ContainerHostInfo containerHostInfo : alienContainers )
                {
                    errMsg.append( containerHostInfo.getContainerName() ).append( " " );
                }
                throw new HostRegistrationException( errMsg.toString() );
            }

            registrationRequest.setStatus( ResourceHostRegistrationStatus.APPROVED );

            requestDataService.update( registrationRequest );

            importHostPublicKey( registrationRequest.getId(), registrationRequest.getPublicKey(), true );

            importHostSslCert( registrationRequest.getId(), registrationRequest.getCert() );

            for ( final ContainerInfo containerInfo : registrationRequest.getHostInfos() )
            {
                importHostPublicKey( containerInfo.getId(), containerInfo.getPublicKey(), false );
            }

            //register resource host
            localPeer.registerResourceHost( registrationRequest );
        }
        catch ( Exception e )
        {
            LOG.error( "Error approving registration request", e );

            throw new HostRegistrationException( e );
        }
    }


    @Override
    public void removeRequest( final String requestId ) throws HostRegistrationException
    {
        EnvironmentManager environmentManager = serviceLocator.getService( EnvironmentManager.class );

        if ( environmentManager.rhHasEnvironments( requestId ) )
        {
            throw new HostRegistrationException( "There are environments on this host" );
        }

        try
        {
            RequestedHost requestedHost = requestDataService.find( requestId );

            if ( requestedHost == null )
            {
                return;
            }

            if ( requestedHost.getStatus() == ResourceHostRegistrationStatus.APPROVED )
            {
                requestDataService.remove( requestedHost.getId() );

                LocalPeer localPeer = serviceLocator.getService( LocalPeer.class );

                localPeer.removeResourceHost( requestedHost.getId() );
            }
            else if ( requestedHost.getStatus() == ResourceHostRegistrationStatus.REQUESTED )
            {
                requestDataService.remove( requestedHost.getId() );
            }
        }
        catch ( HostNotFoundException e )
        {
            LOG.warn( "Resource host {} not found in registered hosts", requestId );
        }
        catch ( Exception e )
        {
            LOG.error( "Error removing registration request", e );

            throw new HostRegistrationException( e );
        }
    }


    @Override
    public void unblockRequest( final String requestId ) throws HostRegistrationException
    {
        try
        {
            RequestedHost requestedHost = requestDataService.find( requestId );

            if ( requestedHost != null && requestedHost.getStatus() == ResourceHostRegistrationStatus.REJECTED )
            {
                requestDataService.remove( requestedHost.getId() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error unblocking registration request", e );

            throw new HostRegistrationException( e );
        }
    }


    void importHostSslCert( String hostId, String cert )
    {
        securityManager.getKeyStoreManager().importCertAsTrusted( Common.DEFAULT_PUBLIC_SECURE_PORT, hostId, cert );
        securityManager.getHttpContextManager().reloadKeyStore();
    }


    void importHostPublicKey( String hostId, String publicKey, boolean rh )
    {
        KeyManager keyManager = securityManager.getKeyManager();
        keyManager.savePublicKeyRing( hostId,
                rh ? SecurityKeyType.RESOURCE_HOST_KEY.getId() : SecurityKeyType.CONTAINER_HOST_KEY.getId(),
                publicKey );
    }


    void checkManagement( RequestedHost requestedHost )
    {
        try
        {
            if ( requestedHost.getStatus() == ResourceHostRegistrationStatus.REQUESTED && containsManagementContainer(
                    requestedHost.getHostInfos() ) )
            {
                boolean managementAlreadyApproved = false;

                for ( RequestedHostImpl requestedHostImpl : requestDataService.getAll() )
                {
                    if ( requestedHostImpl.getStatus() == ResourceHostRegistrationStatus.APPROVED
                            && containsManagementContainer( requestedHostImpl.getHostInfos() ) )
                    {
                        managementAlreadyApproved = true;
                        break;
                    }
                }

                if ( !managementAlreadyApproved )
                {
                    approveRequest( requestedHost.getId() );
                }
            }
        }
        catch ( Exception e )
        {
            // ignore
        }
    }


    boolean containsManagementContainer( Set<ContainerInfo> containers )
    {
        for ( ContainerHostInfo hostInfo : containers )
        {
            if ( Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( hostInfo.getContainerName() ) )
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, final Set<QuotaAlertValue> alerts )
    {
        try
        {
            RequestedHostImpl registrationRequest = requestDataService.find( resourceHostInfo.getId() );

            if ( registrationRequest != null )
            {
                //update hostname
                registrationRequest.setHostname( resourceHostInfo.getHostname() );

                //update ip
                registrationRequest.setAddress( resourceHostInfo.getAddress() );

                requestDataService.update( registrationRequest );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error updating host registration data", e );
        }
    }
}
