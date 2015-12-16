package io.subutai.core.peer.impl;


import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.time.DateUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.peer.Encrypted;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.util.SecurityUtilities;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionListener;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerActionType;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.RegistrationClient;
import io.subutai.core.peer.impl.command.CommandResponseListener;
import io.subutai.core.peer.impl.dao.PeerDataService;
import io.subutai.core.peer.impl.entity.PeerData;
import io.subutai.core.peer.impl.request.MessageResponseListener;
import io.subutai.core.security.api.SecurityManager;


/**
 * PeerManager implementation
 */
@PermitAll
public class PeerManagerImpl implements PeerManager
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerManagerImpl.class );
    private static final String KURJUN_URL_PATTERN = "https://%s:%s/rest/kurjun/templates/public";
    private static final String DEFAULT_EXTERNAL_INTERFACE_NAME = "eth1";
    final int MAX_CONTAINER_LIMIT = 20;
    final int MAX_ENVIRONMENT_LIMIT = 20;


    //    protected PeerDAO peerDAO;
    protected PeerDataService peerDataService;
    protected LocalPeer localPeer;
    protected Messenger messenger;
    protected CommandResponseListener commandResponseListener;
    private MessageResponseListener messageResponseListener;
    private DaoManager daoManager;
    private SecurityManager securityManager;
    private Object provider;
    private Map<String, RegistrationData> registrationRequests = new ConcurrentHashMap<>();
    private List<PeerActionListener> peerActionListeners = new CopyOnWriteArrayList<>();
    private TemplateManager templateManager;
    private IdentityManager identityManager;
    private Map<String, Peer> peers = new ConcurrentHashMap<>();
    private Map<String, PeerPolicy> policies = new ConcurrentHashMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private String externalIpInterface = DEFAULT_EXTERNAL_INTERFACE_NAME;
    private String localPeerId;
    private RegistrationClient registrationClient;


    public PeerManagerImpl( final Messenger messenger, LocalPeer localPeer, DaoManager daoManager,
                            MessageResponseListener messageResponseListener, SecurityManager securityManager,
                            TemplateManager templateManager, IdentityManager identityManager, Object provider )
    {
        this.messenger = messenger;
        this.localPeer = localPeer;
        this.daoManager = daoManager;
        this.messageResponseListener = messageResponseListener;
        this.securityManager = securityManager;
        this.templateManager = templateManager;
        this.identityManager = identityManager;
        this.provider = provider;
        //todo expose CommandResponseListener as service "RequestListener" and inject here
        commandResponseListener = new CommandResponseListener();
        localPeer.addRequestListener( commandResponseListener );
        registrationClient = new RegistrationClientImpl( provider );
    }


    public void setExternalIpInterface( final String externalIpInterface )
    {
        this.externalIpInterface = externalIpInterface;
    }


    public void init() throws PeerException
    {
        try
        {
            this.peerDataService = new PeerDataService( daoManager.getEntityManagerFactory() );

            localPeerId = securityManager.getKeyManager().getPeerId();

            // check local peer instance
            PeerData localPeerData = peerDataService.find( localPeerId );
            PeerInfo localPeerInfo = new PeerInfo();

            if ( localPeerData == null )
            {
                localPeerInfo.setId( localPeerId );
                localPeerInfo.setOwnerId( securityManager.getKeyManager().getOwnerId() );
                localPeerInfo.setIp( getLocalPeerIp() );
                localPeerInfo.setName( String.format( "Peer %s %s", localPeerId, getLocalPeerIp() ) );
                PeerPolicy policy = getDefaultPeerPolicy( localPeerId );

                PeerData peerData =
                        new PeerData( localPeerInfo.getId(), toJson( localPeerInfo ), "", toJson( policy ) );

                updatePeerData( peerData );
            }

            for ( PeerData peerData : this.peerDataService.getAll() )
            {
                //                PeerInfo peerInfo = mapper.readValue( peerData.getInfo(), PeerInfo.class );
                Peer peer = createPeer( peerData );
                addPeer( peer );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Could not initialize peer manager", e );
        }
    }


    public void destroy()
    {
        commandResponseListener.dispose();
    }


    private String getLocalPeerIp() throws PeerException
    {
        String result = null;
        try
        {
            Enumeration<InetAddress> addressEnumeration =
                    NetworkInterface.getByName( externalIpInterface ).getInetAddresses();
            while ( addressEnumeration.hasMoreElements() )
            {
                InetAddress address = addressEnumeration.nextElement();
                if ( address instanceof Inet4Address )
                {
                    result = address.getHostAddress();
                }
            }
        }
        catch ( SocketException e )
        {
            LOG.error( "Error getting network interfaces", e );
        }
        if ( result == null )
        {
            throw new PeerException( "Could not determine IP address of peer." );
        }
        return result;
    }


    public PeerPolicy getDefaultPeerPolicy( String peerId )
    {
        //TODO: make values configurable
        return new PeerPolicy( peerId, 10, 10, 10, 10, 10, 10 );
    }


    @Override
    public void registerPeerActionListener( PeerActionListener peerActionListener )
    {
        Preconditions.checkNotNull( peerActionListener );
        LOG.info( "Registering peer action listener: " + peerActionListener.getName() );
        this.peerActionListeners.add( peerActionListener );
    }


    @Override
    public void unregisterPeerActionListener( PeerActionListener peerActionListener )
    {
        Preconditions.checkNotNull( peerActionListener );
        LOG.info( "Unregistering peer action listener: " + peerActionListener.getName() );
        this.peerActionListeners.remove( peerActionListener );
    }


    private PeerActionResponses notifyPeerActionListeners( PeerAction action )
    {
        PeerActionResponses result = new PeerActionResponses();
        for ( PeerActionListener peerActionListener : peerActionListeners )
        {
            PeerActionResponse response = peerActionListener.onPeerAction( action );
            result.add( response );
        }
        return result;
    }


    private void register( final String keyPhrase, final RegistrationData registrationData ) throws PeerException
    {
        Preconditions.checkNotNull( keyPhrase, "Key phrase could not be null." );
        Preconditions.checkArgument( !keyPhrase.isEmpty(), "Key phrase could not be empty" );

        if ( !notifyPeerActionListeners( new PeerAction( PeerActionType.REGISTER ) ).succeeded() )
        {
            throw new PeerException( "Could not register peer." );
        }

        Encrypted encryptedData = registrationData.getData();
        try
        {
            byte[] key = SecurityUtilities.generateKey( keyPhrase.getBytes( "UTF-8" ) );
            String decryptedCert = encryptedData.decrypt( key, String.class );
            securityManager.getKeyStoreManager()
                           .importCertAsTrusted( ChannelSettings.SECURE_PORT_X2, registrationData.getPeerInfo().getId(),
                                   decryptedCert );
            securityManager.getHttpContextManager().reloadKeyStore();

            PeerPolicy policy = getDefaultPeerPolicy( registrationData.getPeerInfo().getId() );

            PeerData peerData
                    = new PeerData( registrationData.getPeerInfo().getId(), toJson( registrationData.getPeerInfo() ),
                            keyPhrase, toJson( policy ) );
            updatePeerData( peerData );

            Peer newPeer = createPeer( peerData );

            addPeer( newPeer );
            
             templateManager.addRemoteRepository( new URL(
                    String.format( KURJUN_URL_PATTERN, registrationData.getPeerInfo().getIp(),
                            ChannelSettings.SECURE_PORT_X1 ) ), registrationData.getToken() );
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage(), e );
            throw new PeerException( "Could not register peer." );
        }
    }

    private String generateActiveUserToken() throws PeerException
    {
        try
        {
            User user = identityManager.getActiveUser();

            Date date = DateUtils.addMonths( new Date(), 10 );

            UserToken userToken = identityManager.createUserToken( user, "", "", "",
                    TokenType.Permanent.getId(), date );

            return userToken.getFullToken();
        }
        catch ( Exception e )
        {
            throw new PeerException( "Failed to generate active user token.", e );
        }
    }

    private <T> T fromJson( String value, Class<T> type ) throws IOException
    {
        return mapper.readValue( value, type );
    }


    private String toJson( Object value ) throws IOException
    {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString( value );
    }


    protected void addPeer( final Peer peer ) throws PeerException
    {
        if ( peer == null )
        {
            throw new IllegalArgumentException( "Peer could not be null." );
        }
        this.peers.put( peer.getId(), peer );
    }


    private void removePeer( String id )
    {
        this.peers.remove( id );
    }


    private PeerData loadPeerData( final String id )
    {
        return peerDataService.find( id );
    }


    private void updatePeerData( final PeerData peerData ) throws PeerException
    {
        if ( peerData == null )
        {
            throw new IllegalArgumentException( "Peer data could not be null." );
        }
        this.peerDataService.saveOrUpdate( peerData );
    }


    private void removePeerData( String id )
    {
        this.peerDataService.remove( id );
    }


    /**
     * Creates the peer instance by provided peer data
     *
     * @param peerData peer data
     *
     * @return peer instance
     */
    private Peer createPeer( final PeerData peerData ) throws PeerException
    {
        if ( peerData == null )
        {
            throw new IllegalArgumentException( "Peer info could not be null." );
        }
        try
        {
            PeerInfo peerInfo = fromJson( peerData.getInfo(), PeerInfo.class );
            if ( localPeerId.equals( peerData.getId() ) )
            {
                localPeer.setPeerInfo( peerInfo );
                return localPeer;
            }

            return new RemotePeerImpl( localPeerId, securityManager, peerInfo, messenger, commandResponseListener,
                    messageResponseListener, provider );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not create peer instance.", e );
        }
    }


    //TODO:Remove x509 cert from keystore
    private void unregister( final RegistrationData registrationData ) throws PeerException
    {

        if ( !notifyPeerActionListeners(
                new PeerAction( PeerActionType.UNREGISTER, registrationData.getPeerInfo().getId() ) ).succeeded() )
        {
            throw new PeerException( "Could not unregister peer." );
        }
        //        isPeerUsed( registrationData );

        //        ManagementHost mgmHost = getLocalPeer().getManagementHost();
        //        PeerInfo p = getPeerInfo( registrationData.getPeerInfo().getId() );
        //
        //        PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( p.getId() );
        //        // Remove peer policy of the target remote peer from the local peer
        //        if ( peerPolicy != null )
        //        {
        //            localPeer.getPeerInfo().getPeerPolicies().remove( peerPolicy );
        //            peerDAO.saveInfo( SOURCE_LOCAL_PEER, localPeer.getId(), localPeer );
        //        }

        //*********Remove Security Relationship  ****************************
        securityManager.getKeyManager().removePublicKeyRing( registrationData.getPeerInfo().getId() );
        //*******************************************************************

        try
        {
            //            mgmHost.removeRepository( p.getId(), p.getIp() );
            templateManager.removeRemoteRepository( new URL(
                    String.format( KURJUN_URL_PATTERN, registrationData.getPeerInfo().getIp(),
                            ChannelSettings.SECURE_PORT_X1 ) ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not unregister peer.", e );
        }
        //        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, p.getId() );
        removePeerData( registrationData.getPeerInfo().getId() );
        removePeer( registrationData.getPeerInfo().getId() );
    }


    //    private void isPeerUsed( final RegistrationData registrationData ) throws PeerException
    //    {
    //        if ( localPeer.isPeerUsed( registrationData.getPeerInfo().getId() ) )
    //        {
    //            throw new PeerException( "Could not unregister peer. Peer still used." );
    //        }
    //    }


    //    @RolesAllowed( { "Peer-Management|A|Write", "Peer-Management|A|Update" } )
    //    @Override
    //    public boolean update( final PeerInfo peerInfo )
    //    {
    //        String source;
    //        if ( peerInfo.getId().compareTo( localPeer.getId() ) == 0 )
    //        {
    //            source = SOURCE_LOCAL_PEER;
    //        }
    //        else
    //        {
    //            source = SOURCE_REMOTE_PEER;
    //        }
    //        return peerDAO.saveInfo( source, peerInfo.getId(), peerInfo );
    //    }


    //    @Override
    //    public List<PeerInfo> getPeerInfos()
    //    {
    //        return peerDAO.getInfo( SOURCE_REMOTE_PEER, PeerInfo.class );
    //    }


    @Override
    public List<Peer> getPeers()
    {
        //        List<PeerInfo> peerInfoList = peerDAO.getInfo( SOURCE_REMOTE_PEER, PeerInfo.class );
        //        List<Peer> result = Lists.newArrayList();
        //        result.add( getLocalPeer() );
        //        for ( PeerInfo info : peerInfoList )
        //        {
        //            result.add( getPeer( info.getId() ) );
        //        }

        return new ArrayList<>( this.peers.values() );
    }


    private List<PeerPolicy> getPolicies()
    {
        List<PeerPolicy> result = new ArrayList<>();
        for ( PeerData peerData : peerDataService.getAll() )
        {
            try
            {
                PeerPolicy peerPolicy = fromJson( peerData.getPolicy(), PeerPolicy.class );
                result.add( peerPolicy );
            }
            catch ( IOException e )
            {
                //ignore
            }
        }
        return result;
    }


    //
    //    @Override
    //    public PeerInfo getPeerInfo( String id )
    //    {
    //        String source;
    //        if ( id.compareTo( localPeer.getId() ) == 0 )
    //        {
    //            source = SOURCE_LOCAL_PEER;
    //        }
    //        else
    //        {
    //            source = SOURCE_REMOTE_PEER;
    //        }
    //        return peerDAO.getInfo( source, id, PeerInfo.class );
    //    }


    @Override
    public Peer getPeer( final String peerId )
    {
        return this.peers.get( peerId );
    }


    @Override
    public LocalPeer getLocalPeer()
    {
        return localPeer;
    }


    private RegistrationData getRequest( final String id )
    {
        return this.registrationRequests.get( id );
    }


    private void addRequest( final RegistrationData registrationData )
    {
        this.registrationRequests.put( registrationData.getPeerInfo().getId(), registrationData );
    }


    private void removeRequest( final String id )
    {
        this.registrationRequests.remove( id );
    }


    @RolesAllowed( { "Peer-Management|A|Write", "Peer-Management|A|Update" } )
    @Override
    public RegistrationData processRegistrationRequest( final RegistrationData registrationData ) throws PeerException
    {
        addRequest( registrationData );
        return new RegistrationData( localPeer.getPeerInfo(), registrationData.getKeyPhrase(),
                RegistrationStatus.WAIT );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void processUnregisterRequest( final RegistrationData registrationData ) throws PeerException
    {
        // Check peer consumers. This remote peer is used?
        if ( !notifyPeerActionListeners(
                new PeerAction( PeerActionType.UNREGISTER, registrationData.getPeerInfo().getId() ) ).succeeded() )
        {
            throw new PeerException( "Could not unregister peer. Peer in used." );
        }
        Peer p = getPeer( registrationData.getPeerInfo().getId() );
        if ( p == null )
        {
            LOG.warn( "Peer not found to unregister: " + registrationData.getPeerInfo().getId() );
            return;
        }

        Encrypted encryptedData = registrationData.getData();
        try
        {
            final String keyPhrase = loadPeerData( registrationData.getPeerInfo().getId() ).getKeyPhrase();
            byte[] decrypted = encryptedData.decrypt( SecurityUtilities.generateKey( keyPhrase.getBytes( "UTF-8" ) ) );
            if ( !keyPhrase.equals( new String( decrypted, "UTF-8" ) ) )
            {
                throw new PeerException( "Could not unregister peer." );
            }

            unregister( registrationData );
            removeRequest( registrationData.getPeerInfo().getId() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not unregister peer.", e );
        }
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void processRejectRequest( final RegistrationData registrationData ) throws PeerException
    {
        removeRequest( registrationData.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void processCancelRequest( final RegistrationData registrationData ) throws PeerException
    {
        removeRequest( registrationData.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|A|Write", "Peer-Management|A|Update" } )
    @Override
    public void processApproveRequest( final RegistrationData registrationData ) throws PeerException
    {
        RegistrationData initRequest = getRequest( registrationData.getPeerInfo().getId() );
        if ( initRequest == null )
        {
            throw new PeerException( "Registration request not found." );
        }
        register( initRequest.getKeyPhrase(), registrationData );
        removeRequest( registrationData.getPeerInfo().getId() );
    }


    private RegistrationData buildRegistrationData( final String keyPhrase, RegistrationStatus status )
    {
        RegistrationData result = new RegistrationData( localPeer.getPeerInfo(), keyPhrase, status );
        switch ( status )
        {
            case REQUESTED:
            case APPROVED:
                String cert =
                        securityManager.getKeyStoreManager().exportCertificate( ChannelSettings.SECURE_PORT_X2, "" );
                try
                {
                    byte[] key = SecurityUtilities.generateKey( keyPhrase.getBytes( "UTF-8" ) );
                    Encrypted encryptedData = new Encrypted( cert, key );
                    result.setData( encryptedData );
                }
                catch ( Exception e )
                {
                    LOG.warn( e.getMessage(), e );
                }
                break;
            case UNREGISTERED:
                String ip =
                        securityManager.getKeyStoreManager().exportCertificate( ChannelSettings.SECURE_PORT_X2, "" );
                try
                {
                    byte[] key = SecurityUtilities.generateKey( keyPhrase.getBytes( "UTF-8" ) );
                    Encrypted encryptedData = new Encrypted( keyPhrase, key );
                    result.setData( encryptedData );
                }
                catch ( Exception e )
                {
                    LOG.warn( e.getMessage(), e );
                }
                //                result.setCert( cert );
                break;
        }
        return result;
    }


    protected PeerInfo getRemotePeerInfo( String destinationHost ) throws PeerException
    {
        return registrationClient.getPeerInfo( destinationHost );
    }


    @RolesAllowed( { "Peer-Management|A|Write", "Peer-Management|A|Update" } )
    @Override
    public void doRegistrationRequest( final String destinationHost, final String keyPhrase ) throws PeerException
    {
        Preconditions.checkNotNull( destinationHost );
        Preconditions.checkNotNull( keyPhrase );

        PeerInfo peerInfo = getRemotePeerInfo( destinationHost );

        if ( getRequest( peerInfo.getId() ) != null )
        {
            throw new PeerException( "Registration record already exists." );
        }

        final RegistrationData registrationData = buildRegistrationData( keyPhrase, RegistrationStatus.REQUESTED );

        registrationData.setToken( generateActiveUserToken() );

        RegistrationData result = registrationClient.sendInitRequest( destinationHost, registrationData );
        
        result.setKeyPhrase( keyPhrase );
        addRequest( result );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void doCancelRequest( final RegistrationData request ) throws PeerException
    {
        getRemotePeerInfo( request.getPeerInfo().getIp() );

        registrationClient.sendCancelRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( request.getKeyPhrase(), RegistrationStatus.CANCELLED ) );

        removeRequest( request.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|A|Write", "Peer-Management|A|Update" } )
    @Override
    public void doApproveRequest( final String keyPhrase, final RegistrationData request ) throws PeerException
    {
        getRemotePeerInfo( request.getPeerInfo().getIp() );
        
        RegistrationData response = buildRegistrationData( keyPhrase, RegistrationStatus.APPROVED );
                
        response.setToken( generateActiveUserToken() );

        registrationClient.sendApproveRequest( request.getPeerInfo().getIp(), response );

        register( keyPhrase, request );

        removeRequest( request.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void doRejectRequest( final RegistrationData request ) throws PeerException
    {
        getRemotePeerInfo( request.getPeerInfo().getIp() );

        registrationClient.sendRejectRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( request.getKeyPhrase(), RegistrationStatus.REJECTED ) );

        removeRequest( request.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void doUnregisterRequest( final RegistrationData request ) throws PeerException
    {
        getRemotePeerInfo( request.getPeerInfo().getIp() );

        if ( !notifyPeerActionListeners( new PeerAction( PeerActionType.UNREGISTER, request.getPeerInfo().getId() ) )
                .succeeded() )
        {
            throw new PeerException( "Could not unregister peer. Peer in use." );
        }

        RegistrationClient registrationClient = new RegistrationClientImpl( provider );
        PeerData peerData = loadPeerData( request.getPeerInfo().getId() );
        registrationClient.sendUnregisterRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( peerData.getKeyPhrase(), RegistrationStatus.UNREGISTERED ) );

        unregister( request );

        removeRequest( request.getPeerInfo().getId() );
    }


    @Override
    public List<RegistrationData> getRegistrationRequests()
    {
        List<RegistrationData> r = new ArrayList<>( registrationRequests.values() );
        for ( Peer peer : getPeers() )
        {
            if ( !peer.getId().equals( localPeer.getId() ) )
            {
                r.add( new RegistrationData( peer.getPeerInfo(), RegistrationStatus.APPROVED ) );
            }
        }

        return r;
    }


    //    @Override
    //    public PeerInfo getLocalPeerInfo()
    //    {
    //        return localPeer.getPeerInfo();
    //    }


    @Override
    public String getPeerIdByIp( final String ip ) throws PeerException
    {
        Preconditions.checkNotNull( ip );

        String result = null;

        for ( Peer peer : this.peers.values() )
        {
            PeerInfo peerInfo = peer.getPeerInfo();
            if ( ip.equals( peerInfo.getIp() ) )
            {
                result = peerInfo.getId();
            }
        }

        if ( result == null )
        {
            throw new PeerException( "Peer not found by IP: " + ip );
        }
        return result;
    }


    @Override
    public PeerPolicy getAvailablePolicy()
    {
        PeerPolicy result = new PeerPolicy( getLocalPeer().getId(), 0, 0, 0, 0, 0, 0 );
        int diskUsageLimit = 100;
        int cpuUsageLimit = 100;
        int memoryUsageLimit = 100;
        int networkUsageLimit = 100;
        int containerLimit = MAX_CONTAINER_LIMIT;
        int environmentLimit = MAX_ENVIRONMENT_LIMIT;
        for ( PeerPolicy peerPolicy : getPolicies() )
        {
            if ( peerPolicy != null )
            {
                diskUsageLimit -= peerPolicy.getDiskUsageLimit();
                cpuUsageLimit -= peerPolicy.getCpuUsageLimit();
                memoryUsageLimit -= peerPolicy.getMemoryUsageLimit();
                networkUsageLimit -= peerPolicy.getNetworkUsageLimit();
                containerLimit -= peerPolicy.getContainerLimit();
                environmentLimit -= peerPolicy.getEnvironmentLimit();
            }
        }

        try
        {
            result.setDiskUsageLimit( diskUsageLimit < 0 ? 0 : diskUsageLimit );
            result.setCpuUsageLimit( cpuUsageLimit < 0 ? 0 : cpuUsageLimit );
            result.setMemoryUsageLimit( memoryUsageLimit < 0 ? 0 : memoryUsageLimit );
            result.setNetworkUsageLimit( networkUsageLimit < 0 ? 0 : networkUsageLimit );
            result.setContainerLimit( containerLimit < 0 ? 0 : containerLimit );
            result.setEnvironmentLimit( environmentLimit < 0 ? 0 : environmentLimit );
        }
        catch ( Exception e )
        {
            // ignore
        }
        return result;
    }


    @Override
    public PeerPolicy getPolicy( final String peerId )
    {
        try
        {
            PeerPolicy peerPolicy = fromJson( peerDataService.find( peerId ).getPolicy(), PeerPolicy.class );
            return peerPolicy;
        }
        catch ( IOException e )
        {
            // ignore
        }
        return null;
    }


    public void setPolicy( String peerId, PeerPolicy peerPolicy ) throws PeerException
    {
        Peer peer = getPeer( peerId );

        if ( peer == null )
        {
            throw new PeerException( "No such registered peer: " + peerId );
        }

        PeerPolicy availablePolicy = getAvailablePolicy();
        try
        {
            Preconditions.checkArgument( peerPolicy.getContainerLimit() <= availablePolicy.getContainerLimit(),
                    "Container limit exceeded." );
            Preconditions.checkArgument( peerPolicy.getEnvironmentLimit() <= availablePolicy.getEnvironmentLimit(),
                    "Environment limit exceeded." );
            Preconditions.checkArgument( peerPolicy.getDiskUsageLimit() <= availablePolicy.getDiskUsageLimit(),
                    "Disk limit exceeded." );
            Preconditions.checkArgument( peerPolicy.getCpuUsageLimit() <= availablePolicy.getCpuUsageLimit(),
                    "CPU limit exceeded." );
            Preconditions.checkArgument( peerPolicy.getMemoryUsageLimit() <= availablePolicy.getMemoryUsageLimit(),
                    "RAM limit exceeded." );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Invalid policy: " + e.getMessage(), e );
        }


        PeerData peerData = loadPeerData( peerId );

        if ( peerData != null )
        {
            try
            {
                peerData.setPolicy( toJson( peerPolicy ) );
                updatePeerData( peerData );
            }
            catch ( IOException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
    }


    private class PeerActionResponses extends ArrayList<PeerActionResponse>
    {
        boolean succeeded()
        {
            boolean result = true;
            for ( Iterator<PeerActionResponse> i = iterator(); i.hasNext() && result; )
            {
                PeerActionResponse r = i.next();
                if ( !r.isOk() )
                {
                    result = false;
                }
            }
            return result;
        }
    }
}

