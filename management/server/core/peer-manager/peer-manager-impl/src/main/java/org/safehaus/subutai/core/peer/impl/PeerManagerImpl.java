package org.safehaus.subutai.core.peer.impl;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.peer.PeerPolicy;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.impl.command.CommandRequestListener;
import org.safehaus.subutai.core.peer.impl.command.CommandResponseListener;
import org.safehaus.subutai.core.peer.impl.container.CreateContainerGroupRequestListener;
import org.safehaus.subutai.core.peer.impl.container.DestroyEnvironmentContainersRequestListener;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.peer.impl.entity.ManagementHostEntity;
import org.safehaus.subutai.core.peer.impl.request.MessageResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


/**
 * PeerManager implementation
 */
public class PeerManagerImpl implements PeerManager
{

    private static final Logger LOG = LoggerFactory.getLogger( PeerManagerImpl.class.getName() );


    protected PeerDAO peerDAO;
    protected LocalPeer localPeer;
    protected Messenger messenger;
    protected CommandResponseListener commandResponseListener;
    private MessageResponseListener messageResponseListener;
    private DaoManager daoManager;


    public PeerManagerImpl( final Messenger messenger, LocalPeer localPeer, DaoManager daoManager,
                            MessageResponseListener messageResponseListener )
    {
        this.messenger = messenger;
        this.localPeer = localPeer;
        this.daoManager = daoManager;
        this.messageResponseListener = messageResponseListener;
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

        //add command request listener
        addRequestListener( new CommandRequestListener( localPeer, this ) );
        //add command response listener
        commandResponseListener = new CommandResponseListener();
        addRequestListener( commandResponseListener );
        //subscribe to peer message requests
        //subscribe to peer message responses
        //add create container requests listener
        addRequestListener( new CreateContainerGroupRequestListener( localPeer ) );
        //add destroy environment containers requests listener
        addRequestListener( new DestroyEnvironmentContainersRequestListener( localPeer ) );
        //add echo listener
        addRequestListener( new EchoRequestListener() );
    }


    public void destroy()
    {
        commandResponseListener.dispose();
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
        ManagementHost mgmHost = getLocalPeer().getManagementHost();
        if ( !( mgmHost instanceof ManagementHostEntity ) )
        {
            return false;
        }
        ManagementHostEntity managementHost = ( ManagementHostEntity ) mgmHost;
        UUID remotePeerId = UUID.fromString( uuid );
        PeerInfo p = getPeerInfo( remotePeerId );
        managementHost.removeAptSource( p.getId().toString(), p.getIp() );
        managementHost.removeTunnel( p.getIp() );

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
        if ( localPeer.getId().equals( peerId ) )
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
        return localPeer.getPeerInfo();
    }


    @Override
    public void addRequestListener( RequestListener listener )
    {
        localPeer.addRequestListener( listener );
    }


    @Override
    public void removeRequestListener( RequestListener listener )
    {
        localPeer.removeRequestListener( listener );
    }
}

