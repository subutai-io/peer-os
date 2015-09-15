package io.subutai.core.registration.impl;


import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.util.RestUtil;
import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.ClientCredentials;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.exception.NodeRegistrationException;
import io.subutai.core.registration.api.service.ContainerToken;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.impl.dao.ContainerTokenDataService;
import io.subutai.core.registration.impl.dao.RequestDataService;
import io.subutai.core.registration.impl.entity.ContainerTokenImpl;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;
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
    private ContainerTokenDataService containerTokenDataService;
    private DaoManager daoManager;
    private Broker broker;
    private PeerManager peerManager;
    private NetworkManager networkManager;

    public static final String PEER_SUBNET_MASK = "255.255.255.0";


    public RegistrationManagerImpl( final SecurityManager securityManager, final DaoManager daoManager )
    {
        this.securityManager = securityManager;
        this.daoManager = daoManager;
    }


    public void init()
    {
        containerTokenDataService = new ContainerTokenDataService( daoManager );
        requestDataService = new RequestDataService( daoManager );

        //        Interface hostInterface = new HostInterface( "iName1", "ip", "mac" );
        //        Interface hostInterface1 = new HostInterface( "iName2", "ip", "mac" );
        //        HostInfo containerHostInfoModel =
        //                new ContainerHostInfoModel( UUID.randomUUID().toString(), "hostname", Sets.newHashSet(
        // hostInterface1 ),
        //                        HostArchitecture.AMD64 );
        //
        //        RequestedHostImpl requestedHost =
        //                new RequestedHostImpl( UUID.randomUUID().toString(), UUID.randomUUID().toString(),
        //                        HostArchitecture.AMD64, "secret", "publicKey", "restHook", RegistrationStatus
        // .REQUESTED,
        //                        Sets.newHashSet( hostInterface ), Sets.newHashSet( containerHostInfoModel ) );
        //
        //        requestDataService.update( requestedHost );
    }


    public Broker getBroker()
    {
        return broker;
    }


    public void setBroker( final Broker broker )
    {
        this.broker = broker;
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
    public void queueRequest( final RequestedHost requestedHost ) throws NodeRegistrationException
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
                            RegistrationStatus.REQUESTED, requestedHost.getNetInterfaces() );
            try
            {
                requestDataService.persist( temp );

                securityManager.getKeyManager().savePublicKeyRing( temp.getId(), ( short ) 2, temp.getPublicKey() );
            }
            catch ( Exception ex )
            {
                throw new NodeRegistrationException( "Failed adding resource host registration request to queue", ex );
            }
        }
    }


    @Override
    public void rejectRequest( final UUID requestId )
    {
        RequestedHostImpl registrationRequest = requestDataService.find( requestId );
        registrationRequest.setStatus( RegistrationStatus.REJECTED );
        requestDataService.update( registrationRequest );

        WebClient client = RestUtil.createWebClient( registrationRequest.getRestHook() );

        EncryptionTool encryptionTool = securityManager.getEncryptionTool();
        KeyManager keyManager = securityManager.getKeyManager();

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
        Form form = new Form();
        try
        {
            ClientCredentials clientCredentials = broker.createNewClientCredentials( requestId.toString() );
            form.set( "ca", clientCredentials.getCaCertificate() );
            form.set( "crt", clientCredentials.getClientCertificate() );
            form.set( "key", clientCredentials.getClientKey() );
            client.form( form );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error approving new connections request", e );
        }
    }


    @Override
    public void removeRequest( final UUID requestId )
    {
        requestDataService.remove( requestId );
    }


    @Override
    public ContainerToken generateContainerTTLToken( final Long ttl )
    {
        ContainerTokenImpl token =
                new ContainerTokenImpl( UUID.randomUUID().toString(), new Timestamp( System.currentTimeMillis() ),
                        ttl );
        try
        {
            containerTokenDataService.persist( token );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Error persisting container token", ex );
        }

        return token;
    }


    @Override
    public ContainerToken verifyToken( final String token, String containerHostId, String publicKey )
            throws NodeRegistrationException
    {

        ContainerTokenImpl containerToken = containerTokenDataService.find( token );
        if ( containerToken == null )
        {
            throw new NodeRegistrationException( "Couldn't verify container token" );
        }

        if ( containerToken.getDateCreated().getTime() + containerToken.getTtl() < System.currentTimeMillis() )
        {
            throw new NodeRegistrationException( "Container token expired" );
        }
        try
        {
            securityManager.getKeyManager().savePublicKeyRing( containerHostId, ( short ) 2, publicKey );
        }
        catch ( Exception ex )
        {
            throw new NodeRegistrationException( "Failed to store container pubkey", ex );
        }
        return containerToken;
    }


    @Override
    public void importEnvironment( final Environment environment, final List<ContainerHost> containerHosts )
            throws NodeRegistrationException
    {
        /**
         * steps to perform for fully functional environment and compliance work with the rest of the system
         * 1. setupN2n
         * 2. add environment peers
         * 3. create key pair
         * 4. find free vni
         * 5. reserve vni
         * 6. set vni
         */

        //        networkManager.setupN2NConnection( peerManager.getLocalPeer(). );
        //step 1
        List<N2NConfig> tunnels =
                Lists.newArrayList();//setupN2NConnection( Sets.newHashSet((Peer)peerManager.getLocalPeer()) );

        for ( N2NConfig config : tunnels )
        {
            //            final PeerConf p = new PeerConfImpl();
            //            p.setN2NConfig( config );
            //            environment.addEnvironmentPeer( p );
        }

        //step 2

    }
}
