package org.safehaus.subutai.core.peer.impl;


import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.util.HttpUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.Common;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.peer.api.message.PeerMessageListener;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerImpl implements PeerManager {

    private final static Logger LOG = Logger.getLogger( PeerImpl.class.getName() );
    private final Queue<PeerMessageListener> peerMessageListeners = new ConcurrentLinkedQueue<>();


    private final String SOURCE = "PEER_MANAGER";
    private final HttpUtil httpUtil;
    private UUID id;
    private DbManager dbManager;
    private PeerDAO peerDAO;


    public PeerImpl()
    {
        this.httpUtil = new HttpUtil();
    }


    public void setId( final String id )
    {
        this.id = UUIDUtil.generateStringUUID( id );
    }


    public void init()
    {
        LOG.info( "SUBUTAID ID: " + id );
        peerDAO = new PeerDAO( dbManager );
    }


    public void destroy()
    {
        httpUtil.dispose();
    }


    public void setDbManager( final DbManager dbManager )
    {
        this.dbManager = dbManager;
    }


    @Override
    public String register( final Peer peer )
    {

        try
        {
            peerDAO.saveInfo( SOURCE, peer.getId().toString(), peer );
            return peer.getId().toString();
        }
        catch ( DBException e )
        {
            LOG.info( e.getMessage() );
        }
        return null;
    }


    @Override
    public UUID getSiteId()
    {
        return id;
    }


    @Override
    public List<Peer> peers()
    {
        List<Peer> peers = null;
        try
        {
            peers = peerDAO.getInfo( SOURCE, Peer.class );
        }
        catch ( DBException e )
        {
            LOG.info( e.getMessage() );
        }
        return peers;
    }


    @Override
    public boolean unregister( final String uuid )
    {
        try
        {
            peerDAO.deleteInfo( SOURCE, uuid );
            return true;
        }
        catch ( DBException e )
        {
            LOG.info( e.getMessage() );
        }
        return false;
    }


    @Override
    public Peer getPeerByUUID( UUID uuid )
    {
        if ( getSiteId().compareTo( uuid ) == 0 )
        {
            Peer peer = new Peer();
            peer.setId( uuid );
            peer.setIp( getLocalIp() );
            peer.setName( "Me" );
            return peer;
        }

        try
        {
            return peerDAO.getInfo( SOURCE, uuid.toString(), Peer.class );
        }
        catch ( DBException e )
        {
            LOG.info( e.getMessage() );
        }
        return null;
    }


    @Override
    public void addPeerMessageListener( PeerMessageListener listener )
    {
        try
        {
            if ( !peerMessageListeners.contains( listener ) )
            {
                peerMessageListeners.add( listener );
            }
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in addPeerMessageListener", ex );
        }
    }


    @Override
    public void removePeerMessageListener( PeerMessageListener listener )
    {
        try
        {
            peerMessageListeners.remove( listener );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in removePeerMessageListener", ex );
        }
    }


    @Override
    public String sendPeerMessage( final Peer peer, String recipient, final String message ) throws PeerMessageException
    {

        String ip = peer.getIp();

        Map<String, String> params = new HashMap<>();
        params.put( Common.RECIPIENT_PARAM_NAME, recipient );
        params.put( Common.PEER_ID_PARAM_NAME, getSiteId().toString() );
        params.put( Common.MESSAGE_PARAM_NAME, message );
        try
        {
            return httpUtil.post( String.format( Common.MESSAGE_REQUEST_URL, ip ), params );
        }
        catch ( IOException e )
        {
            LOG.log( Level.SEVERE, "Error in sendPeerMessage", e );
            throw new PeerMessageException( e.getMessage() );
        }
    }


    @Override
    public String processPeerMessage( final String peerId, final String recipient, final String message )
            throws PeerMessageException
    {

        try
        {
            UUID peerUUID = UUID.fromString( peerId );
            Peer senderPeer = getPeerByUUID( peerUUID );
            if ( senderPeer != null )
            {

                for ( PeerMessageListener listener : peerMessageListeners )
                {
                    try
                    {
                        if ( listener.getName().equalsIgnoreCase( recipient ) )
                        {
                            try
                            {
                                return listener.onMessage( senderPeer, message );
                            }
                            catch ( Exception e )
                            {
                                LOG.log( Level.SEVERE, "Error in processPeerMessage", e );
                                throw new PeerMessageException( e.getMessage() );
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                        LOG.log( Level.SEVERE, "Error in processPeerMessage", e );
                        throw e;
                    }
                    //                break;
                }
                String err = String.format( "Recipient %s not found", recipient );
                LOG.log( Level.SEVERE, "Error in processPeerMessage", err );
                throw new PeerMessageException( err );
            }
            else
            {
                String err = String.format( "Peer %s not found", peerId );
                LOG.log( Level.SEVERE, "Error in processPeerMessage", err );
                throw new PeerMessageException( err );
            }
        }
        catch ( IllegalArgumentException e )
        {
            LOG.log( Level.SEVERE, "Error in processPeerMessage", e );
            throw new PeerMessageException( e.getMessage() );
        }
    }


    @Override
    public void processPeerMessage( final String peerId, final String peerMessage ) throws PeerMessageException
    {

    }


    @Override
    public void createContainers( final UUID envId, final String template, final int numberOfNodes,
                                  final String Strategy, final List<String> criteria )
    {

    }


    private String getLocalIp()
    {
        Enumeration<NetworkInterface> n;
        try
        {
            n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); )
            {
                NetworkInterface e = n.nextElement();

                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); )
                {
                    InetAddress addr = a.nextElement();
                    if ( addr.getHostAddress().startsWith( "172" ) )
                    {
                        return addr.getHostAddress();
                    }
                }
            }
        }
        catch ( SocketException e )
        {
            LOG.severe( e.getMessage() );
        }


        return "127.0.0.1";
    }


    public Collection<PeerMessageListener> getPeerMessageListeners()
    {
        return Collections.unmodifiableCollection( peerMessageListeners );
    }
}
