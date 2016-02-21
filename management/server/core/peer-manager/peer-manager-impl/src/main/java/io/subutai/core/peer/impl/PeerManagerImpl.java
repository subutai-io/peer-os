package io.subutai.core.peer.impl;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.peer.Encrypted;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.protocol.ControlNetworkConfig;
import io.subutai.common.protocol.PingDistances;
import io.subutai.common.resource.PeerGroupResources;
import io.subutai.common.resource.PeerResources;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.ControlNetworkUtil;
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
    final static int CONTROL_NETWORK_TTL_IN_MIN = 10;

    private static final Logger LOG = LoggerFactory.getLogger( PeerManagerImpl.class );
    private static final String KURJUN_URL_PATTERN = "https://%s:%s/rest/kurjun/templates/public";
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
    //    private Map<String, PeerPolicy> policies = new ConcurrentHashMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private String localPeerId;
    private String ownerId;
    private RegistrationClient registrationClient;
    protected ScheduledExecutorService backgroundTasksExecutorService;
    private String publicUrl;

    private String controlNetwork;
    private long controlNetworkTtl = 0;
    private String externalInterfaceName;
    private PingDistances distances;


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
        backgroundTasksExecutorService = Executors.newScheduledThreadPool( 1 );
        backgroundTasksExecutorService.scheduleWithFixedDelay( new BackgroundTasksRunner(), 10, 60, TimeUnit.SECONDS );
    }


    public void init() throws PeerException
    {
        try
        {
            this.peerDataService = new PeerDataService( daoManager.getEntityManagerFactory() );

            localPeerId = securityManager.getKeyManager().getPeerId();
            ownerId = securityManager.getKeyManager().getPeerOwnerId();

            PeerData localPeerData = peerDataService.find( localPeerId );


            if ( localPeerData == null )
            {
                PeerInfo localPeerInfo = new PeerInfo();
                localPeerInfo.setId( localPeerId );
                localPeerInfo.setOwnerId( ownerId );

                if ( StringUtils.isEmpty( SystemSettings.getPublicUrl() ) )
                {
                    localPeerInfo.setName( String.format( "Peer %s ", localPeerId ) );
                }
                else
                {
                    localPeerInfo.setPublicUrl( SystemSettings.getPublicUrl() );
                    localPeerInfo
                            .setName( String.format( "Peer %s on %s", localPeerId, SystemSettings.getPublicUrl() ) );
                }

                PeerPolicy policy = getDefaultPeerPolicy( localPeerId );

                PeerData peerData =
                        new PeerData( localPeerInfo.getId(), toJson( localPeerInfo ), "", toJson( policy ), 1 );

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
        backgroundTasksExecutorService.shutdown();
    }


    public void setPublicUrl( final String publicUrl )
    {
        this.publicUrl = publicUrl;
    }


    public void setExternalInterfaceName( final String externalInterfaceName )
    {
        this.externalInterfaceName = externalInterfaceName;
    }


    public PeerPolicy getDefaultPeerPolicy( String peerId )
    {
        //TODO: make values configurable
        return new PeerPolicy( peerId, 90, 50, 90, 90, 3, 10 );
    }


    @Override
    public void registerPeerActionListener( PeerActionListener peerActionListener )
    {
        if ( peerActionListener != null )
        {
            LOG.info( "Registering peer action listener: " + peerActionListener.getName() );
            this.peerActionListeners.add( peerActionListener );
        }
    }


    @Override
    public void unregisterPeerActionListener( PeerActionListener peerActionListener )
    {
        if ( peerActionListener != null )
        {
            LOG.info( "Unregistering peer action listener: " + peerActionListener.getName() );
            this.peerActionListeners.remove( peerActionListener );
        }
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
            securityManager.getKeyStoreManager().importCertAsTrusted( SystemSettings.getSecurePortX2(),
                    registrationData.getPeerInfo().getId(), decryptedCert );
            securityManager.getHttpContextManager().reloadKeyStore();

            PeerPolicy policy = getDefaultPeerPolicy( registrationData.getPeerInfo().getId() );

            final Integer order = getMaxOrder() + 1;
            PeerData peerData =
                    new PeerData( registrationData.getPeerInfo().getId(), toJson( registrationData.getPeerInfo() ),
                            keyPhrase, toJson( policy ), order );
            updatePeerData( peerData );

            Peer newPeer = createPeer( peerData );

            addPeer( newPeer );

            templateManager.addRemoteRepository( new URL(
                    String.format( KURJUN_URL_PATTERN, registrationData.getPeerInfo().getIp(),
                            SystemSettings.getSecurePortX1() ) ), registrationData.getToken() );
        }
        catch ( GeneralSecurityException e )
        {
            throw new PeerException( "Invalid keyphrase or general security exception." );
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

            UserToken userToken =
                    identityManager.createUserToken( user, "", "", "", TokenType.Permanent.getId(), date );

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


    protected void removePeer( String id )
    {
        Peer peer = this.peers.get( id );
        if ( peer != null )
        {
            this.peers.remove( id );
        }
    }


    protected PeerData loadPeerData( final String id )
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

        //*********Remove Security Relationship  ****************************
        securityManager.getKeyManager().removePublicKeyRing( registrationData.getPeerInfo().getId() );
        //*******************************************************************

        try
        {
            //            mgmHost.removeRepository( p.getId(), p.getIp() );
            templateManager.removeRemoteRepository( new URL(
                    String.format( KURJUN_URL_PATTERN, registrationData.getPeerInfo().getIp(),
                            SystemSettings.getSecurePortX1() ) ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not unregister peer.", e );
        }
        //        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, p.getId() );
        removePeerData( registrationData.getPeerInfo().getId() );
        removePeer( registrationData.getPeerInfo().getId() );
    }


    @Override
    public List<Peer> getPeers()
    {
        return new ArrayList<>( this.peers.values() );
    }


    @Override
    public Set<Peer> resolve( final Set<String> peers ) throws PeerException
    {
        Set<Peer> result = new HashSet<>();
        for ( String peerId : peers )
        {

            result.add( getPeer( peerId ) );
        }
        return result;
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


    @Override
    public Peer getPeer( final String peerId ) throws PeerException
    {
        Peer result = this.peers.get( peerId );
        if ( result == null )
        {
            throw new PeerException( "Peer not found: " + peerId );
        }
        return result;
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


    @RolesAllowed( { "Peer-Management|Write", "Peer-Management|Update" } )
    @Override
    public RegistrationData processRegistrationRequest( final RegistrationData registrationData ) throws PeerException
    {
        try
        {
            PeerInfo p = getRemotePeerInfo( registrationData.getPeerInfo().getPublicUrl() );
        }
        catch ( PeerException e )
        {
            throw new PeerException( String.format( "Registration request rejected. Provided URL %s not accessable.",
                    registrationData.getPeerInfo().getPublicUrl() ) );
        }

        addRequest( registrationData );
        return new RegistrationData( localPeer.getPeerInfo(), registrationData.getKeyPhrase(),
                RegistrationStatus.WAIT );
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
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


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public void processRejectRequest( final RegistrationData registrationData ) throws PeerException
    {
        removeRequest( registrationData.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public void processCancelRequest( final RegistrationData registrationData ) throws PeerException
    {
        removeRequest( registrationData.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|Write", "Peer-Management|Update" } )
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
        securityManager.getKeyManager().getRemoteHostPublicKey( registrationData.getPeerInfo().getId(),
                registrationData.getPeerInfo().getIp() );
    }


    private RegistrationData buildRegistrationData( final String keyPhrase, RegistrationStatus status )
            throws PeerException
    {
        RegistrationData result = new RegistrationData( localPeer.getPeerInfo(), keyPhrase, status );
        switch ( status )
        {
            case REQUESTED:
            case APPROVED:
                String cert =
                        securityManager.getKeyStoreManager().exportCertificate( SystemSettings.getSecurePortX2(), "" );
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
                        securityManager.getKeyStoreManager().exportCertificate( SystemSettings.getSecurePortX2(), "" );
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


    @RolesAllowed( { "Peer-Management|Write", "Peer-Management|Update" } )
    @Override
    public void doRegistrationRequest( final String destinationHost, final String keyPhrase ) throws PeerException
    {
        Preconditions.checkNotNull( destinationHost );
        Preconditions.checkNotNull( keyPhrase );

        String destinationUrl = buildDestinationUrl( destinationHost );
        PeerInfo peerInfo = getRemotePeerInfo( destinationUrl );

        if ( getRequest( peerInfo.getId() ) != null )
        {
            throw new PeerException( "Registration record already exists." );
        }

        try
        {
            final RegistrationData registrationData = buildRegistrationData( keyPhrase, RegistrationStatus.REQUESTED );

            registrationData.setToken( generateActiveUserToken() );

            RegistrationData result = registrationClient.sendInitRequest( destinationUrl, registrationData );

            result.setKeyPhrase( keyPhrase );
            addRequest( result );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( e.getMessage() );
        }
    }


    private String buildDestinationUrl( final String destinationHost )
    {
        try
        {
            URL url = new URL( destinationHost );
            return destinationHost;
        }
        catch ( MalformedURLException e )
        {
            return String.format( "https://%s:%d/", destinationHost, SystemSettings.getSecurePortX1() );
        }
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public void doCancelRequest( final RegistrationData request ) throws PeerException
    {
        getRemotePeerInfo( request.getPeerInfo().getPublicUrl() );

        try
        {
            registrationClient.sendCancelRequest( request.getPeerInfo().getPublicUrl(),
                    buildRegistrationData( request.getKeyPhrase(), RegistrationStatus.CANCELLED ) );

            removeRequest( request.getPeerInfo().getId() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( e.getMessage() );
        }
    }


    @RolesAllowed( { "Peer-Management|Write", "Peer-Management|Update" } )
    @Override
    public void doApproveRequest( final String keyPhrase, final RegistrationData request ) throws PeerException
    {
        getRemotePeerInfo( request.getPeerInfo().getPublicUrl() );
        try
        {
            RegistrationData response = buildRegistrationData( keyPhrase, RegistrationStatus.APPROVED );

            response.setToken( generateActiveUserToken() );

            registrationClient.sendApproveRequest( request.getPeerInfo().getPublicUrl(), response );

            register( keyPhrase, request );

            removeRequest( request.getPeerInfo().getId() );
            securityManager.getKeyManager()
                           .getRemoteHostPublicKey( request.getPeerInfo().getId(), request.getPeerInfo().getIp() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( e.getMessage() );
        }
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public void doRejectRequest( final RegistrationData request ) throws PeerException
    {
        getRemotePeerInfo( request.getPeerInfo().getPublicUrl() );
        try
        {
            registrationClient.sendRejectRequest( request.getPeerInfo().getPublicUrl(),
                    buildRegistrationData( request.getKeyPhrase(), RegistrationStatus.REJECTED ) );

            //            removeRequest( request.getPeerInfo().getId() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( e.getMessage() );
        }

        removeRequest( request.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public void doUnregisterRequest( final RegistrationData request ) throws PeerException
    {
        getRemotePeerInfo( request.getPeerInfo().getPublicUrl() );

        if ( !notifyPeerActionListeners( new PeerAction( PeerActionType.UNREGISTER, request.getPeerInfo().getId() ) )
                .succeeded() )
        {
            throw new PeerException( "Could not unregister peer. Peer in use." );
        }

        try
        {
            RegistrationClient registrationClient = new RegistrationClientImpl( provider );
            PeerData peerData = loadPeerData( request.getPeerInfo().getId() );
            registrationClient.sendUnregisterRequest( request.getPeerInfo().getPublicUrl(),
                    buildRegistrationData( peerData.getKeyPhrase(), RegistrationStatus.UNREGISTERED ) );
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage(), e );
            //            throw new PeerException( e.getMessage() );
        }

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
                try
                {
                    r.add( new RegistrationData( peer.getPeerInfo(), RegistrationStatus.APPROVED ) );
                }
                catch ( PeerException e )
                {
                    LOG.warn( String.format( "Could not get peer info from %s. %s", peer.getId(), e.getMessage() ) );
                }
            }
        }

        return r;
    }


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
    public PeerGroupResources getPeerGroupResources() throws PeerException
    {
        final List<PeerResources> resources = new ArrayList<>();
        for ( final Peer peer : getPeers() )
        {
            PeerResources peerResources = getPeer( peer.getId() ).getResourceLimits( localPeerId );
            resources.add( peerResources );
        }

        return new PeerGroupResources( resources, getCommunityDistances() );
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
            return fromJson( peerDataService.find( peerId ).getPolicy(), PeerPolicy.class );
        }
        catch ( IOException e )
        {
            // ignore
        }
        return null;
    }


    @Override
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


    protected synchronized Integer getMaxOrder() throws PeerException
    {
        try
        {
            int result = 0;
            for ( PeerData peerData : peerDataService.getAll() )
            {
                if ( peerData.getOrder() > result )
                {
                    result = peerData.getOrder();
                }
            }
            return result;
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not get peer order." );
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


    private class BackgroundTasksRunner implements Runnable
    {
        @Override
        public void run()
        {
            LOG.debug( "Peer background tasks started..." );

            checkPeers();
            checkNetwork();

            LOG.debug( "Peer background tasks finished." );
        }


        private void checkNetwork()
        {
            try
            {
                updateControlNetwork();
            }
            catch ( Exception e )
            {
                LOG.warn( e.getMessage() );
            }
        }


        private void checkPeers()
        {
            try
            {
                for ( Peer peer : getPeers() )
                {
                    try
                    {
                        if ( peer.isLocal() )
                        {
                            LocalPeer localPeer = ( LocalPeer ) peer;
                            if ( "UNKNOWN".equals( localPeer.getPeerInfo().getIp() ) )
                            {
                                setDefaultPublicUrl( localPeer );
                            }
                        }
                        else
                        {
                            //todo: check remote peer.
                        }
                    }
                    catch ( Exception e )
                    {
                        LOG.warn( e.getMessage() );
                    }
                }
            }
            catch ( Exception e )
            {
                LOG.warn( e.getMessage() );
            }
        }


        private void setDefaultPublicUrl( final LocalPeer localPeer ) throws PeerException, IOException
        {

            PeerData peerData = peerDataService.find( localPeer.getPeerInfo().getId() );

            PeerInfo peerInfo = fromJson( peerData.getInfo(), PeerInfo.class );

            String publicIp = getExternalIp( localPeer );

            peerInfo.setPublicUrl( publicIp );

            peerInfo.setName( String.format( "Peer %s on %s", localPeerId, publicIp ) );

            peerData.setInfo( toJson( peerInfo ) );

            updatePeerData( peerData );

            Peer newPeer = createPeer( peerData );

            addPeer( newPeer );
        }


        private String getExternalIp( final LocalPeer localPeer ) throws HostNotFoundException
        {
            Host rh = localPeer.getResourceHostByContainerName( "management" );

            return rh.getInterfaceByName( SystemSettings.getExternalIpInterface() ).getIp();
        }
    }


    @Override
    public PingDistances getCommunityDistances()
    {
        if ( distances != null )
        {
            return distances;
        }

        distances = new PingDistances();

        final List<Peer> peers = getPeers();
        ExecutorService pool = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<PingDistances> completionService = new ExecutorCompletionService<>( pool );
        for ( Peer peer : peers )
        {
            completionService.submit( new CommunityDistanceTask( peer ) );
        }

        pool.shutdown();

        for ( int i = 0; i < peers.size(); i++ )
        {
            try
            {
                final Future<PingDistances> f = completionService.take();
                PingDistances r = f.get();
                distances.addAll( r.getAll() );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                LOG.warn( "Could not get distances : " + e.getMessage() );
            }
        }
        return distances;
    }


    @Override
    public void setPublicUrl( final String peerId, final String publicUrl, final int securePort ) throws PeerException
    {
        Preconditions.checkNotNull( peerId );

        PeerData peerData = peerDataService.find( peerId );
        if ( peerData == null )
        {
            throw new PeerException( "Peer not found." );
        }
        try
        {
            PeerInfo peerInfo = fromJson( peerData.getInfo(), PeerInfo.class );
            peerInfo.setPublicUrl( publicUrl );
            peerInfo.setPort(securePort);
            peerData.setInfo( toJson( peerInfo ) );
            peerDataService.saveOrUpdate( peerData );
            Peer peer = createPeer( peerData );
            addPeer( peer );
        }
        catch ( IOException e )
        {
            throw new PeerException( "Unexpected error: " + e.getMessage() );
        }
    }


    @Override
    public void updateControlNetwork()
    {
        if ( getPeers().size() < 2 )
        {
            // standalone peer
            return;
        }

        try
        {
            if ( controlNetwork == null )
            {
                controlNetwork = localPeer.getCurrentControlNetwork();
                selectControlNetwork();
            }
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
        }


        boolean isOk = false;
        byte[] key = null;
        while ( !isOk )
        {
            if ( controlNetworkTtl <= System.currentTimeMillis() )
            {
                controlNetworkTtl =
                        System.currentTimeMillis() + TimeUnit.MINUTES.toMillis( CONTROL_NETWORK_TTL_IN_MIN );
                key = DigestUtils.md5( UUID.randomUUID().toString() );
                this.distances = null;
            }
            String[] addresses =
                    new SubnetUtils( controlNetwork, ControlNetworkUtil.NETWORK_MASK ).getInfo().getAllAddresses();
            List<UpdateControlNetworkTask> updateTasks = new ArrayList<>();
            for ( Peer peer : getPeers() )
            {
                PeerData data = loadPeerData( peer.getId() );
                if ( data.getOrder() == null )
                {
                    continue;
                }
                ControlNetworkConfig config = new ControlNetworkConfig( peer.getId(), addresses[data.getOrder() - 1],
                        localPeerId.toLowerCase(), key, controlNetworkTtl );
                updateTasks.add( new UpdateControlNetworkTask( config ) );
            }
            final ExecutorService pool = Executors.newFixedThreadPool( updateTasks.size() );
            final ExecutorCompletionService<Boolean> executor = new ExecutorCompletionService<Boolean>( pool );
            for ( UpdateControlNetworkTask task : updateTasks )
            {
                executor.submit( task );
            }

            pool.shutdown();

            for ( UpdateControlNetworkTask task : updateTasks )
            {
                try
                {
                    final Future<Boolean> result = executor.take();
                    isOk = result.get();
                    if ( !isOk )
                    {
                        // Network conflict. Skipping other result. Needs renew network.
                        break;
                    }
                }
                catch ( InterruptedException | ExecutionException e )
                {
                    // ignore
                }
            }

            if ( !isOk )
            {
                LOG.warn( "Control network config conflict. Selecting another network." );
                selectControlNetwork();
                LOG.warn( "New control network: " + controlNetwork );
            }
            else if ( key != null )
            {
                Arrays.fill( key, ( byte ) 0 );
            }
        }
    }


    private class UpdateControlNetworkTask implements Callable<Boolean>
    {
        private ControlNetworkConfig config;


        public UpdateControlNetworkTask( final ControlNetworkConfig config )
        {
            this.config = config;
        }


        @Override
        public Boolean call() throws Exception
        {
            try
            {
                return getPeer( config.getPeerId() ).updateControlNetworkConfig( config );
            }
            catch ( Exception e )
            {
                // Ignoring any exception.
                return true;
            }
        }
    }


    protected void selectControlNetwork()
    {
        List<ControlNetworkConfig> configs = new ArrayList<>();
        for ( Peer peer : getPeers() )
        {
            try
            {
                configs.add( peer.getControlNetworkConfig( localPeer.getId() ) );
            }
            catch ( PeerException e )
            {
                //ignore
            }
        }
        controlNetwork = ControlNetworkUtil.findFreeNetwork( configs );
        controlNetworkTtl = 0;
    }


    private class CommunityDistanceTask implements Callable<PingDistances>
    {
        private Peer peer;


        public CommunityDistanceTask( final Peer peer )
        {
            this.peer = peer;
        }


        @Override
        public PingDistances call() throws Exception
        {
            return peer.getCommunityDistances( getLocalPeer().getId(), getMaxOrder() );
        }
    }
}

