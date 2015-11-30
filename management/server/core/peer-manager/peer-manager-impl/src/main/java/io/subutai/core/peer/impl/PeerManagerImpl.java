package io.subutai.core.peer.impl;


import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.peer.Encrypted;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ManagementHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.util.SecurityUtilities;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionListener;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerActionType;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.RegistrationClient;
import io.subutai.core.peer.impl.command.CommandResponseListener;
import io.subutai.core.peer.impl.dao.PeerDAO;
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


    protected PeerDAO peerDAO;
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


    public PeerManagerImpl( final Messenger messenger, LocalPeer localPeer, DaoManager daoManager,
                            MessageResponseListener messageResponseListener, SecurityManager securityManager,
                            TemplateManager templateManager, Object provider )
    {
        this.messenger = messenger;
        this.localPeer = localPeer;
        this.daoManager = daoManager;
        this.messageResponseListener = messageResponseListener;
        this.securityManager = securityManager;
        this.templateManager = templateManager;
        this.provider = provider;
        //todo expose CommandResponseListener as service "RequestListener" and inject here
        commandResponseListener = new CommandResponseListener();
        localPeer.addRequestListener( commandResponseListener );
    }


    public void init()
    {
        try
        {
            this.peerDAO = new PeerDAO( daoManager );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error initializing peer dao", e );
        }
    }


    public void destroy()
    {
        commandResponseListener.dispose();
    }


    @Override
    public void registerPeerActionListener( PeerActionListener peerActionListener )
    {
        Preconditions.checkNotNull( peerActionListener );

        this.peerActionListeners.add( peerActionListener );
    }


    @Override
    public void unregisterPeerActionListener( PeerActionListener peerActionListener )
    {
        Preconditions.checkNotNull( peerActionListener );

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

            registrationData.getPeerInfo().setKeyPhrase( keyPhrase );
            peerDAO.saveInfo( SOURCE_REMOTE_PEER, registrationData.getPeerInfo().getId(),
                    registrationData.getPeerInfo() );

            ManagementHost mgmHost = getLocalPeer().getManagementHost();
            try
            {
                //                mgmHost.addRepository( registrationData.getPeerInfo().getIp() );
                templateManager.addRemoteRepository( new URL(
                        String.format( KURJUN_URL_PATTERN, registrationData.getPeerInfo().getIp(),
                                ChannelSettings.SECURE_PORT_X1 ) ) );
            }
            catch ( Exception ignore )
            {
                // ignore
            }
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage(), e );
            throw new PeerException( "Could not register peer." );
        }
    }


    //TODO:Remove x509 cert from keystore
    private boolean unregister( final RegistrationData registrationData ) throws PeerException
    {

        if ( !notifyPeerActionListeners(
                new PeerAction( PeerActionType.UNREGISTER, registrationData.getPeerInfo().getId() ) ).succeeded() )
        {
            throw new PeerException( "Could not register peer." );
        }
        //        isPeerUsed( registrationData );

        ManagementHost mgmHost = getLocalPeer().getManagementHost();
        PeerInfo p = getPeerInfo( registrationData.getPeerInfo().getId() );

        PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( p.getId() );
        // Remove peer policy of the target remote peer from the local peer
        if ( peerPolicy != null )
        {
            localPeer.getPeerInfo().getPeerPolicies().remove( peerPolicy );
            peerDAO.saveInfo( SOURCE_LOCAL_PEER, localPeer.getId(), localPeer );
        }

        //*********Remove Security Relationship  ****************************
        securityManager.getKeyManager().removePublicKeyRing( p.getId() );
        //*******************************************************************

        try
        {
            //            mgmHost.removeRepository( p.getId(), p.getIp() );
            templateManager.removeRemoteRepository( new URL(
                    String.format( KURJUN_URL_PATTERN, registrationData.getPeerInfo().getIp(),
                            ChannelSettings.SECURE_PORT_X1 ) ) );
        }
        catch ( Exception ignore )
        {
            // ignore
        }
        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, p.getId() );
    }


    //    private void isPeerUsed( final RegistrationData registrationData ) throws PeerException
    //    {
    //        if ( localPeer.isPeerUsed( registrationData.getPeerInfo().getId() ) )
    //        {
    //            throw new PeerException( "Could not unregister peer. Peer still used." );
    //        }
    //    }


    @RolesAllowed( { "Peer-Management|A|Write", "Peer-Management|A|Update" } )
    @Override
    public boolean update( final PeerInfo peerInfo )
    {
        String source;
        if ( peerInfo.getId().compareTo( localPeer.getId() ) == 0 )
        {
            source = SOURCE_LOCAL_PEER;
        }
        else
        {
            source = SOURCE_REMOTE_PEER;
        }
        return peerDAO.saveInfo( source, peerInfo.getId(), peerInfo );
    }


    @Override
    public List<PeerInfo> getPeerInfos()
    {
        return peerDAO.getInfo( SOURCE_REMOTE_PEER, PeerInfo.class );
    }


    @Override
    public List<Peer> getPeers()
    {
        List<PeerInfo> peerInfoList = peerDAO.getInfo( SOURCE_REMOTE_PEER, PeerInfo.class );
        List<Peer> result = Lists.newArrayList();
        result.add( getLocalPeer() );
        for ( PeerInfo info : peerInfoList )
        {
            result.add( getPeer( info.getId() ) );
        }

        return result;
    }


    @Override
    public PeerInfo getPeerInfo( String id )
    {
        String source;
        if ( id.compareTo( localPeer.getId() ) == 0 )
        {
            source = SOURCE_LOCAL_PEER;
        }
        else
        {
            source = SOURCE_REMOTE_PEER;
        }
        return peerDAO.getInfo( source, id, PeerInfo.class );
    }


    @Override
    public Peer getPeer( final String peerId )
    {
        if ( localPeer.getId().equals( peerId ) )
        {
            return localPeer;
        }

        PeerInfo pi = getPeerInfo( peerId );

        if ( pi != null )
        {
            return new RemotePeerImpl( localPeer, securityManager, pi, messenger, commandResponseListener,
                    messageResponseListener, provider );
        }
        return null;
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
        return new RegistrationData( getLocalPeerInfo(), registrationData.getKeyPhrase(), RegistrationStatus.WAIT );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void processUnregisterRequest( final RegistrationData registrationData ) throws PeerException
    {
        if ( !notifyPeerActionListeners(
                new PeerAction( PeerActionType.UNREGISTER, registrationData.getPeerInfo().getId() ) ).succeeded() )
        {
            throw new PeerException( "Could not unregister peer. Peer in used." );
        }
        PeerInfo p = getPeerInfo( registrationData.getPeerInfo().getId() );
        if ( p == null )
        {
            LOG.warn( "Registration info not found for peer: " + registrationData.getPeerInfo().getId() );
            return;
        }

        Encrypted encryptedData = registrationData.getData();
        try
        {
            final String keyPhrase = p.getKeyPhrase();
            byte[] decrypted = encryptedData.decrypt( SecurityUtilities.generateKey( keyPhrase.getBytes( "UTF-8" ) ) );
            if ( !keyPhrase.equals( new String( decrypted, "UTF-8" ) ) )
            {
                throw new PeerException( "Could not unregister peer." );
            }

            removeRequest( registrationData.getPeerInfo().getId() );
            unregister( registrationData );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not unregister peer." );
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
        register( initRequest.getKeyPhrase(), registrationData );
        removeRequest( registrationData.getPeerInfo().getId() );
    }


    private RegistrationData buildRegistrationData( final String keyPhrase, RegistrationStatus status )
    {
        RegistrationData result = new RegistrationData( getLocalPeerInfo(), keyPhrase, status );
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


    @RolesAllowed( { "Peer-Management|A|Write", "Peer-Management|A|Update" } )
    @Override
    public void doRegistrationRequest( final String destinationHost, final String keyPhrase ) throws PeerException
    {
        Preconditions.checkNotNull( destinationHost );
        Preconditions.checkNotNull( keyPhrase );

        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        RegistrationData result = registrationClient
                .sendInitRequest( destinationHost, buildRegistrationData( keyPhrase, RegistrationStatus.REQUESTED ) );

        result.setKeyPhrase( keyPhrase );
        addRequest( result );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void doCancelRequest( final RegistrationData request ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        registrationClient.sendCancelRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( request.getKeyPhrase(), RegistrationStatus.CANCELLED ) );

        removeRequest( request.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|A|Write", "Peer-Management|A|Update" } )
    @Override
    public void doApproveRequest( final String keyPhrase, final RegistrationData request ) throws PeerException
    {
        register( keyPhrase, request );

        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        registrationClient.sendApproveRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( keyPhrase, RegistrationStatus.APPROVED ) );

        removeRequest( request.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void doRejectRequest( final RegistrationData request ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        registrationClient.sendRejectRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( request.getKeyPhrase(), RegistrationStatus.REJECTED ) );

        removeRequest( request.getPeerInfo().getId() );
    }


    @RolesAllowed( { "Peer-Management|A|Delete", "Peer-Management|A|Update" } )
    @Override
    public void doUnregisterRequest( final RegistrationData request ) throws PeerException
    {
        if ( !notifyPeerActionListeners( new PeerAction( PeerActionType.UNREGISTER, request.getPeerInfo().getId() ) )
                .succeeded() )
        {
            throw new PeerException( "Could not unregister peer. Peer in use." );
        }

        RegistrationClient registrationClient = new RegistrationClientImpl( provider );
        registrationClient.sendUnregisterRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( request.getPeerInfo().getKeyPhrase(), RegistrationStatus.UNREGISTERED ) );

        unregister( request );

        removeRequest( request.getPeerInfo().getId() );
    }


    @Override
    public List<RegistrationData> getRegistrationRequests()
    {
        List<RegistrationData> r = new ArrayList<>( registrationRequests.values() );
        for ( Peer peer : getPeers() )
        {
            if ( !peer.getId().equals( getLocalPeerInfo().getId() ) )
            {
                r.add( new RegistrationData( peer.getPeerInfo(), RegistrationStatus.APPROVED ) );
            }
        }

        return r;
    }


    @Override
    public PeerInfo getLocalPeerInfo()
    {
        return localPeer.getPeerInfo();
    }


    @Override
    public String getPeerIdByIp( final String ip ) throws PeerException
    {
        Preconditions.checkNotNull( ip );

        String result = null;

        for ( Iterator<PeerInfo> i = getPeerInfos().iterator(); result == null && i.hasNext(); )
        {
            PeerInfo peerInfo = i.next();
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

