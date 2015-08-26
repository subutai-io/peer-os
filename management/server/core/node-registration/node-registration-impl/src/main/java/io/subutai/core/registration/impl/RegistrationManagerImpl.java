package io.subutai.core.registration.impl;


import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.resource.host.RequestedHost;
import io.subutai.core.registration.impl.resource.RequestDataService;
import io.subutai.core.registration.impl.resource.entity.RequestedHostImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * Created by talas on 8/24/15.
 */
public class RegistrationManagerImpl implements RegistrationManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RegistrationManagerImpl.class );
    private RequestDataService requestDataService;
    private SecurityManager securityManager;


    public RegistrationManagerImpl( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public void init()
    {
        //        RequestedHostImpl temp =
        //                new RequestedHostImpl( UUID.randomUUID().toString(), "hostname", HostArchitecture.AMD64,
        // "some key",
        //                        "some rest hook", RegistrationStatus.REQUESTED );
        //        InterfaceModel interfaceModel = new InterfaceModel();
        //        interfaceModel.setMac( UUID.randomUUID().toString() );
        //        interfaceModel.setIp( "Some ip" );
        //        interfaceModel.setInterfaceName( "Some i-name" );
        //        temp.setInterfaces( Sets.newHashSet( interfaceModel ) );
        //
        //        requestDataService.persist( temp );
        //        LOGGER.info( "Started RegistrationManagerImpl" );
        //        List<RequestedHostImpl> requestedHosts = ( List<RequestedHostImpl> ) requestDataService.getAll();
        //        for ( final RequestedHostImpl requestedHost : requestedHosts )
        //        {
        //            LOGGER.error( requestedHost.toString() );
        //        }
    }


    public RequestDataService getRequestDataService()
    {
        return requestDataService;
    }


    public void setRequestDataService( final RequestDataService requestDataService )
    {
        Preconditions.checkNotNull( requestDataService, "RequestDataService shouldn't be null." );

        this.requestDataService = requestDataService;
    }


    @Override
    public List<RequestedHost> getRequests()
    {
        List<RequestedHost> temp = Lists.newArrayList();
        temp.addAll( requestDataService.getAll() );
        return temp;
    }


    @Override
    public RequestedHost getRequest( final UUID requestId )
    {
        return requestDataService.find( requestId );
    }


    @Override
    public void queueRequest( final RequestedHost requestedHost )
    {
        if ( requestDataService.find( UUID.fromString( requestedHost.getId() ) ) != null )
        {
            LOGGER.info( "Already requested registration" );
        }
        else
        {
            RequestedHostImpl temp =
                    new RequestedHostImpl( requestedHost.getId(), requestedHost.getHostname(), requestedHost.getArch(),
                            requestedHost.getSecret(), requestedHost.getPublicKey(), requestedHost.getRestHook(),
                            RegistrationStatus.REQUESTED, requestedHost.getInterfaces() );
            requestDataService.persist( temp );
        }
    }


    @Override
    public void rejectRequest( final UUID requestId )
    {
        RequestedHostImpl registrationRequest = requestDataService.find( requestId );
        registrationRequest.setStatus( RegistrationStatus.REJECTED );

        WebClient client = RestUtil.createWebClient( registrationRequest.getRestHook() );

        EncryptionTool encryptionTool = securityManager.getEncryptionTool();
        KeyManager keyManager = securityManager.getKeyManager();
        InputStream secretKey = PGPEncryptionUtil.getFileInputStream( keyManager.getSecretKeyringFile() );

        String message = RegistrationStatus.REJECTED.name();
        PGPPublicKey publicKey = keyManager.getPublicKey( registrationRequest.getId() );
        byte[] encodedArray = encryptionTool.encrypt( message.getBytes(), publicKey, true );
        String encoded = message;
        try
        {
            encoded = new String( encodedArray, "UTF-8" );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error approving new connections request", e );
        }
        client.query( "Message", encoded ).delete();
    }


    @Override
    public void approveRequest( final UUID requestId )
    {
        RequestedHostImpl registrationRequest = requestDataService.find( requestId );
        registrationRequest.setStatus( RegistrationStatus.APPROVED );
        requestDataService.update( registrationRequest );

        WebClient client = RestUtil.createWebClient( registrationRequest.getRestHook() );

        EncryptionTool encryptionTool = securityManager.getEncryptionTool();
        KeyManager keyManager = securityManager.getKeyManager();
        InputStream secretKey = PGPEncryptionUtil.getFileInputStream( keyManager.getSecretKeyringFile() );

        String message = RegistrationStatus.APPROVED.name();
        PGPPublicKey publicKey = keyManager.getPublicKey( registrationRequest.getId() );
        byte[] encodedArray = encryptionTool.encrypt( message.getBytes(), publicKey, true );
        String encoded = message;
        try
        {
            encoded = new String( encodedArray, "UTF-8" );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error approving new connections request", e );
        }
        client.post( encoded );
    }


    @Override
    public void removeRequest( final UUID requestId )
    {
        requestDataService.remove( requestId );
    }
}
