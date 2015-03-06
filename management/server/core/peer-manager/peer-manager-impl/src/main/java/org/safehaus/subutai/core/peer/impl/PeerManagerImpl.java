package org.safehaus.subutai.core.peer.impl;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.peer.PeerPolicy;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.key.api.KeyInfo;
import org.safehaus.subutai.core.key.api.KeyManager;
import org.safehaus.subutai.core.key.api.KeyManagerException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.peer.api.EnvironmentContext;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.impl.command.CommandRequestListener;
import org.safehaus.subutai.core.peer.impl.command.CommandResponseListener;
import org.safehaus.subutai.core.peer.impl.container.CreateContainerGroupRequestListener;
import org.safehaus.subutai.core.peer.impl.container.DestroyEnvironmentContainersRequestListener;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.peer.impl.request.MessageRequestListener;
import org.safehaus.subutai.core.peer.impl.request.MessageResponseListener;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.ssl.manager.api.CustomSslContextFactory;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * PeerManager implementation
 */
public class PeerManagerImpl implements PeerManager
{

    private static final Logger LOG = LoggerFactory.getLogger( PeerManagerImpl.class.getName() );
    private static final String SOURCE_REMOTE_PEER = "PEER_REMOTE";
    private static final String SOURCE_LOCAL_PEER = "PEER_LOCAL";
    private static final String PEER_GROUP = "PEER_GROUP";
    private PeerDAO peerDAO;
    private QuotaManager quotaManager;
    private Monitor monitor;
    private TemplateRegistry templateRegistry;
    private CommandExecutor commandExecutor;
    private LocalPeerImpl localPeer;
    private StrategyManager strategyManager;
    private PeerInfo peerInfo;
    private Messenger messenger;
    private CommandResponseListener commandResponseListener;
    private Set<RequestListener> requestListeners = Sets.newHashSet();
    private MessageResponseListener messageResponseListener;
    private HostRegistry hostRegistry;
    private DaoManager daoManager;
    private KeyManager keyManager;
    private IdentityManager identityManager;
    private CustomSslContextFactory sslContextFactory;


    public void setSslContextFactory( final CustomSslContextFactory sslContextFactory )
    {
        this.sslContextFactory = sslContextFactory;
    }


    public PeerManagerImpl( final Messenger messenger )
    {
        this.messenger = messenger;
    }


    public void setHostRegistry( final HostRegistry hostRegistry )
    {
        this.hostRegistry = hostRegistry;
    }


    public DaoManager getDaoManager()
    {
        return daoManager;
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void setKeyManager( final KeyManager keyManager )
    {
        this.keyManager = keyManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public EnvironmentContext prepareEnvironment( final UUID environmentId, String email )
    {
        EnvironmentContext environmentContext = new EnvironmentContext();
        try
        {
            ManagementHost managementHost = localPeer.getManagementHost();
            KeyInfo keyInfo = keyManager.generateKey( managementHost, environmentId.toString(), email );
            keyInfo.getPublicKeyId();
            String gpgPublicKey = keyManager.readKey( managementHost, keyInfo.getPublicKeyId() );
        }
        catch ( KeyManagerException | HostNotFoundException e )
        {
            LOG.error( e.toString(), e );
        }
        return null;
    }


    @Override
    public EntityManagerFactory getEntityManagerFactory()
    {
        return daoManager.getEntityManagerFactory();
    }


    public void init()
    {
        try
        {
            this.peerDAO = new PeerDAO( daoManager );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }

        List<PeerInfo> result = peerDAO.getInfo( SOURCE_LOCAL_PEER, PeerInfo.class );
        if ( result.isEmpty() )
        {
            peerInfo = new PeerInfo();
            //TODO generate peer id based on owner/system information
            peerInfo.setId( UUID.randomUUID() );
            peerInfo.setName( "Local Subutai server" );
            //TODO get ownerId from persistent storage
            peerInfo.setOwnerId( UUID.randomUUID() );

            try
            {
                Enumeration<InetAddress> addressEnumeration = NetworkInterface.getByName( "eth1" ).getInetAddresses();
                while ( addressEnumeration.hasMoreElements() )
                {
                    InetAddress address = addressEnumeration.nextElement();
                    if ( ( address instanceof Inet4Address ) )
                    {
                        peerInfo.setIp( address.getHostAddress() );
                    }
                }
            }
            catch ( SocketException e )
            {
                LOG.error( "Error getting network interfaces", e );
            }
            peerInfo.setName( String.format( "Peer on %s", peerInfo.getIp() ) );

            peerDAO.saveInfo( SOURCE_LOCAL_PEER, peerInfo.getId().toString(), peerInfo );
        }
        else
        {
            peerInfo = result.get( 0 );
        }
        localPeer = new LocalPeerImpl( this, templateRegistry, quotaManager, strategyManager, requestListeners,
                commandExecutor, hostRegistry, monitor, identityManager );
        localPeer.setSslContextFactory( sslContextFactory );
        localPeer.init();

        //add command request listener
        addRequestListener( new CommandRequestListener( localPeer, this ) );
        //add command response listener
        commandResponseListener = new CommandResponseListener();
        addRequestListener( commandResponseListener );
        //subscribe to peer message requests
        messenger.addMessageListener( new MessageRequestListener( this, messenger, requestListeners ) );
        //subscribe to peer message responses
        messageResponseListener = new MessageResponseListener( messenger );
        messenger.addMessageListener( messageResponseListener );
        //add create container requests listener
        addRequestListener( new CreateContainerGroupRequestListener( localPeer ) );
        //add destroy environment containers requests listener
        addRequestListener( new DestroyEnvironmentContainersRequestListener( localPeer ) );
        //add echo listener
        addRequestListener( new EchoRequestListener() );
    }


    public void destroy()
    {
        localPeer.shutdown();
    }


    public void setCommandExecutor( final CommandExecutor commandExecutor )
    {
        this.commandExecutor = commandExecutor;
    }


    public void setStrategyManager( final StrategyManager strategyManager )
    {
        this.strategyManager = strategyManager;
    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public void setQuotaManager( final QuotaManager quotaManager )
    {
        this.quotaManager = quotaManager;
    }


    public void setMonitor( final Monitor monitor )
    {
        this.monitor = monitor;
    }


    @Override
    public boolean trustRequest( final UUID peerId, final String root_server_px1 ) throws PeerException
    {
        return false;
    }


    @Override
    public boolean trustResponse( final UUID peerId, final String root_server_px1, final short status )
            throws PeerException
    {
        return false;
    }


    @Override
    public boolean register( final PeerInfo peerInfo ) throws PeerException
    {
        ManagementHost managementHost = getLocalPeer().getManagementHost();
        managementHost.addAptSource( peerInfo.getId().toString(), peerInfo.getIp() );

        return peerDAO.saveInfo( SOURCE_REMOTE_PEER, peerInfo.getId().toString(), peerInfo );
    }


    @Override
    public boolean unregister( final String uuid ) throws PeerException
    {
        ManagementHost managementHost = getLocalPeer().getManagementHost();
        UUID remotePeerId = UUID.fromString( uuid );
        PeerInfo p = getPeerInfo( remotePeerId );
        managementHost.removeAptSource( p.getId().toString(), p.getIp() );
        PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( remotePeerId );
        // Remove peer policy of the target remote peer from the local peer
        if ( peerPolicy != null )
        {
            localPeer.getPeerInfo().getPeerPolicies().remove( peerPolicy );
            peerDAO.saveInfo( SOURCE_LOCAL_PEER, localPeer.getId().toString(), localPeer );
        }
        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, uuid );
    }


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
        return peerDAO.saveInfo( source, peerInfo.getId().toString(), peerInfo );
    }


    @Override
    public List<PeerInfo> peers()
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
    public PeerInfo getPeerInfo( UUID uuid )
    {
        String source;
        if ( uuid.compareTo( localPeer.getId() ) == 0 )
        {
            source = SOURCE_LOCAL_PEER;
        }
        else
        {
            source = SOURCE_REMOTE_PEER;
        }
        return peerDAO.getInfo( source, uuid.toString(), PeerInfo.class );
    }


    @Override
    public Peer getPeer( final UUID peerId )
    {
        if ( peerInfo.getId().equals( peerId ) )
        {
            return localPeer;
        }

        PeerInfo pi = getPeerInfo( peerId );

        if ( pi != null )
        {
            return new RemotePeerImpl( localPeer, pi, messenger, commandResponseListener, messageResponseListener );
        }
        return null;
    }


    @Override
    public Peer getPeer( final String peerId )
    {
        return getPeer( UUID.fromString( peerId ) );
    }


    @Override
    public LocalPeer getLocalPeer()
    {
        return localPeer;
    }


    @Override
    public PeerInfo getLocalPeerInfo()
    {
        return peerInfo;
    }


    @Override
    public void addRequestListener( RequestListener listener )
    {
        if ( listener != null )
        {
            requestListeners.add( listener );
        }
    }


    @Override
    public void removeRequestListener( RequestListener listener )
    {
        if ( listener != null )
        {
            requestListeners.add( listener );
        }
    }
}

