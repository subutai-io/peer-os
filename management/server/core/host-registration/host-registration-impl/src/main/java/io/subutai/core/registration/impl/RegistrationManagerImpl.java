package io.subutai.core.registration.impl;


import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.HostInfo;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.RegistrationStatus;
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


public class RegistrationManagerImpl implements RegistrationManager
{
    private static final Logger LOG = LoggerFactory.getLogger( RegistrationManagerImpl.class );
    private SecurityManager securityManager;
    private RequestDataService requestDataService;
    private ContainerTokenDataService containerTokenDataService;
    private DaoManager daoManager;
    protected ServiceLocator serviceLocator = new ServiceLocator();


    public RegistrationManagerImpl( final SecurityManager securityManager, final DaoManager daoManager )
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


    protected void setRequestDataService( final RequestDataService requestDataService )
    {
        Preconditions.checkNotNull( requestDataService, "RequestDataService shouldn't be null." );

        this.requestDataService = requestDataService;
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


    @Override
    public synchronized void queueRequest( final RequestedHost requestedHost ) throws HostRegistrationException
    {
        Preconditions.checkNotNull( requestedHost, "'Invalid registration request" );
        Preconditions
                .checkArgument( PGPKeyUtil.isValidPublicKeyring( requestedHost.getPublicKey() ), "Invalid public key" );

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

                registrationRequest.setStatus( RegistrationStatus.REQUESTED );

                requestDataService.update( registrationRequest );

                checkManagement( registrationRequest );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error queueing agent registration request", e );

            throw new HostRegistrationException( e );
        }
    }


    @Override
    public void rejectRequest( final String requestId ) throws HostRegistrationException
    {
        try
        {
            RequestedHostImpl registrationRequest = requestDataService.find( requestId );
            registrationRequest.setStatus( RegistrationStatus.REJECTED );
            requestDataService.update( registrationRequest );
        }
        catch ( Exception e )
        {
            LOG.error( "Error rejecting agent registration request", e );

            throw new HostRegistrationException( e );
        }
    }


    @Override
    public void approveRequest( final String requestId ) throws HostRegistrationException
    {
        try
        {
            RequestedHostImpl registrationRequest = requestDataService.find( requestId );

            if ( registrationRequest == null || !RegistrationStatus.REQUESTED
                    .equals( registrationRequest.getStatus() ) )
            {
                return;
            }

            registrationRequest.setStatus( RegistrationStatus.APPROVED );

            requestDataService.update( registrationRequest );

            importHostPublicKey( registrationRequest.getId(), registrationRequest.getPublicKey() );

            importHostSslCert( registrationRequest.getId(), registrationRequest.getCert() );

            for ( final ContainerInfo containerInfo : registrationRequest.getHostInfos() )
            {
                importHostPublicKey( containerInfo.getId(), containerInfo.getPublicKey() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error approving agent registration request", e );

            throw new HostRegistrationException( e );
        }
    }


    @Override
    public void removeRequest( final String requestId ) throws HostRegistrationException
    {
        try
        {
            RequestedHost requestedHost = requestDataService.find( requestId );

            if ( requestedHost == null )
            {
                return;
            }

            requestDataService.remove( requestedHost.getId() );

            LocalPeer localPeer = serviceLocator.getService( LocalPeer.class );

            localPeer.removeResourceHost( requestedHost.getId() );
        }
        catch ( Exception e )
        {
            LOG.error( "Error removing agent registration request", e );

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
        try
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
                securityManager.getKeyManager().savePublicKeyRing( containerHostId, ( short ) 2, publicKey );
            }
            catch ( Exception ex )
            {
                throw new HostRegistrationException( "Failed to store container pubkey", ex );
            }
            return containerToken;
        }
        catch ( HostRegistrationException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            LOG.error( "Error verifying token", e );

            throw new HostRegistrationException( e );
        }
    }


    private void importHostSslCert( String hostId, String cert )
    {
        securityManager.getKeyStoreManager().importCertAsTrusted( Common.DEFAULT_PUBLIC_SECURE_PORT, hostId, cert );
        securityManager.getHttpContextManager().reloadKeyStore();
    }


    private void importHostPublicKey( String hostId, String publicKey )
    {
        KeyManager keyManager = securityManager.getKeyManager();
        keyManager.savePublicKeyRing( hostId, ( short ) 2, publicKey );
    }


    private void checkManagement( RequestedHost requestedHost )
    {
        try
        {
            if ( requestedHost.getStatus() == RegistrationStatus.REQUESTED && containsManagementContainer(
                    requestedHost.getHostInfos() ) )
            {
                boolean managementAlreadyApproved = false;

                for ( RequestedHostImpl requestedHostImpl : requestDataService.getAll() )
                {
                    if ( requestedHostImpl.getStatus() == RegistrationStatus.APPROVED && containsManagementContainer(
                            requestedHostImpl.getHostInfos() ) )
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


    private boolean containsManagementContainer( Set<ContainerInfo> containers )
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
}
