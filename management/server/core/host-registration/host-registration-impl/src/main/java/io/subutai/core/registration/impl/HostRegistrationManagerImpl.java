package io.subutai.core.registration.impl;


import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.settings.Common;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.exception.HostRegistrationException;
import io.subutai.core.registration.api.service.ContainerInfo;
import io.subutai.core.registration.api.service.ContainerToken;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.impl.dao.ContainerTokenDataService;
import io.subutai.core.registration.impl.dao.RequestDataService;
import io.subutai.core.registration.impl.entity.ContainerTokenImpl;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;


//TODO add security annotation
public class HostRegistrationManagerImpl implements HostRegistrationManager, HostListener
{
    private static final Logger LOG = LoggerFactory.getLogger( HostRegistrationManagerImpl.class );
    private SecurityManager securityManager;
    protected RequestDataService requestDataService;
    protected ContainerTokenDataService containerTokenDataService;
    private DaoManager daoManager;
    protected ServiceLocator serviceLocator = new ServiceLocator();


    public HostRegistrationManagerImpl( final SecurityManager securityManager, final DaoManager daoManager )
    {
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( daoManager );

        this.securityManager = securityManager;
        this.daoManager = daoManager;
    }


    public void init()
    {
        containerTokenDataService = new ContainerTokenDataService( daoManager );
        requestDataService = new RequestDataService( daoManager );
    }


    protected RequestDataService getRequestDataService()
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

                if ( !Strings.isNullOrEmpty( requestedHost.getHostname() ) && !requestedHostImpl.getHostname()
                                                                                                .equalsIgnoreCase(
                                                                                                        requestedHost
                                                                                                                .getHostname() ) )
                {
                    requestedHostImpl.setHostname( requestedHost.getHostname() );

                    requestDataService.update( requestedHostImpl );
                }
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

            registrationRequest.setStatus( ResourceHostRegistrationStatus.APPROVED );

            requestDataService.update( registrationRequest );

            importHostPublicKey( registrationRequest.getId(), registrationRequest.getPublicKey(), true );

            importHostSslCert( registrationRequest.getId(), registrationRequest.getCert() );

            for ( final ContainerInfo containerInfo : registrationRequest.getHostInfos() )
            {
                importHostPublicKey( containerInfo.getId(), containerInfo.getPublicKey(), false );
            }
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
        try
        {
            RequestedHost requestedHost = requestDataService.find( requestId );

            if ( requestedHost != null )
            {
                LocalPeer localPeer = serviceLocator.getService( LocalPeer.class );

                localPeer.removeResourceHost( requestedHost.getId() );

                requestDataService.remove( requestedHost.getId() );
            }
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


    @Override
    public ContainerToken generateContainerTTLToken( final long ttlInMs ) throws HostRegistrationException
    {
        Preconditions.checkArgument( ttlInMs > 0, "Invalid ttl" );

        ContainerTokenImpl token =
                new ContainerTokenImpl( UUID.randomUUID().toString(), new Timestamp( System.currentTimeMillis() ),
                        ttlInMs );
        try
        {
            containerTokenDataService.persist( token );
        }
        catch ( Exception e )
        {
            LOG.error( "Error persisting container token", e );

            throw new HostRegistrationException( e );
        }

        return token;
    }


    @Override
    public ContainerToken verifyToken( final String token, String containerHostId, String publicKey )
            throws HostRegistrationException
    {

        ContainerTokenImpl containerToken = containerTokenDataService.find( token );

        if ( containerToken == null )
        {
            throw new HostRegistrationException( "Couldn't verify container token" );
        }

        if ( containerToken.getDateCreated().getTime() + containerToken.getTtl() < System.currentTimeMillis() )
        {
            throw new HostRegistrationException( "Container token expired" );
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

        return containerToken;
    }


    protected void importHostSslCert( String hostId, String cert )
    {
        securityManager.getKeyStoreManager().importCertAsTrusted( Common.DEFAULT_PUBLIC_SECURE_PORT, hostId, cert );
        securityManager.getHttpContextManager().reloadKeyStore();
    }


    protected void importHostPublicKey( String hostId, String publicKey, boolean rh )
    {
        KeyManager keyManager = securityManager.getKeyManager();
        keyManager.savePublicKeyRing( hostId,
                rh ? SecurityKeyType.RESOURCE_HOST_KEY.getId() : SecurityKeyType.CONTAINER_HOST_KEY.getId(),
                publicKey );
    }


    protected void checkManagement( RequestedHost requestedHost )
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


    protected boolean containsManagementContainer( Set<ContainerInfo> containers )
    {
        for ( HostInfo hostInfo : containers )
        {
            if ( Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( hostInfo.getHostname() ) )
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
                registrationRequest.setHostname( resourceHostInfo.getHostname() );
                registrationRequest.setInterfaces( resourceHostInfo.getHostInterfaces() );

                requestDataService.update( registrationRequest );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error updating host registration data", e );
        }
    }


    @Override
    public void onContainerStateChanged( final ContainerHostInfo containerInfo, final ContainerHostState previousState,
                                         final ContainerHostState currentState )
    {

    }


    @Override
    public void onContainerHostnameChanged( final ContainerHostInfo containerInfo, final String previousHostname,
                                            final String currentHostname )
    {

    }


    @Override
    public void onContainerCreated( final ContainerHostInfo containerInfo )
    {

    }


    @Override
    public void onContainerNetInterfaceChanged( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel oldNetInterface,
                                                final HostInterfaceModel newNetInterface )
    {

    }


    @Override
    public void onContainerNetInterfaceAdded( final ContainerHostInfo containerInfo,
                                              final HostInterfaceModel netInterface )
    {

    }


    @Override
    public void onContainerNetInterfaceRemoved( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel netInterface )
    {

    }
}
