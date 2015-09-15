package io.subutai.core.peer.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.Interface;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.util.N2NUtil;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.command.CommandRequestListener;
import io.subutai.core.peer.impl.command.CommandResponseListener;
import io.subutai.core.peer.impl.container.CreateContainerGroupRequestListener;
import io.subutai.core.peer.impl.container.CreateEnvironmentContainersRequestListener;
import io.subutai.core.peer.impl.container.DestroyEnvironmentContainersRequestListener;
import io.subutai.core.peer.impl.dao.PeerDAO;
import io.subutai.core.peer.impl.entity.ManagementHostEntity;
import io.subutai.core.peer.impl.request.MessageResponseListener;
import io.subutai.core.security.api.SecurityManager;


/**
 * PeerManager implementation
 */
public class PeerManagerImpl implements PeerManager
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerManagerImpl.class.getName() );

    public static final String PEER_SUBNET_MASK = "255.255.255.0";
    private static final int N2N_PORT = 5000;

    protected PeerDAO peerDAO;
    protected LocalPeer localPeer;
    protected Messenger messenger;
    protected CommandResponseListener commandResponseListener;
    private MessageResponseListener messageResponseListener;
    private DaoManager daoManager;
    private SecurityManager securityManager;


    public PeerManagerImpl( final Messenger messenger, LocalPeer localPeer, DaoManager daoManager,
                            MessageResponseListener messageResponseListener, SecurityManager securityManager )
    {
        this.messenger = messenger;
        this.localPeer = localPeer;
        this.daoManager = daoManager;
        this.messageResponseListener = messageResponseListener;
        this.securityManager = securityManager;
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

        //add command request listener
        addRequestListener( new CommandRequestListener( localPeer, this ) );
        //add command response listener
        commandResponseListener = new CommandResponseListener();
        addRequestListener( commandResponseListener );
        //add create container requests listener
        addRequestListener( new CreateContainerGroupRequestListener( localPeer ) );
        addRequestListener( new CreateEnvironmentContainersRequestListener( localPeer ) );
        //add destroy environment containers requests listener
        addRequestListener( new DestroyEnvironmentContainersRequestListener( localPeer ) );
        //add echo listener
        addRequestListener( new EchoRequestListener() );
    }


    public void destroy()
    {
        commandResponseListener.dispose();
    }


    public SecurityManager getSecurityManager()
    {
        return this.securityManager;
    }


    @Override
    public boolean register( final PeerInfo peerInfo ) throws PeerException
    {
        return peerDAO.saveInfo( SOURCE_REMOTE_PEER, peerInfo.getId().toString(), peerInfo );
    }


    @Override
    public boolean unregister( final String remotePeerId ) throws PeerException
    {
        ManagementHost mgmHost = getLocalPeer().getManagementHost();
        ManagementHostEntity managementHost = ( ManagementHostEntity ) mgmHost;
        PeerInfo p = getPeerInfo( remotePeerId );
        managementHost.removeRepository( p.getId().toString(), p.getIp() );
        //        managementHost.removeTunnel( p.getIp() );

        PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( remotePeerId );
        // Remove peer policy of the target remote peer from the local peer
        if ( peerPolicy != null )
        {
            localPeer.getPeerInfo().getPeerPolicies().remove( peerPolicy );
            peerDAO.saveInfo( SOURCE_LOCAL_PEER, localPeer.getId().toString(), localPeer );
        }

        //*********Remove Security Relationship  ****************************
        securityManager.getKeyManager().removePublicKeyRing( remotePeerId );
        //*******************************************************************

        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, remotePeerId );
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
            return new RemotePeerImpl( localPeer, pi, messenger, commandResponseListener, messageResponseListener );
        }
        return null;
    }


    @Override
    public LocalPeer getLocalPeer()
    {
        return localPeer;
    }


    @Override
    public List<N2NConfig> setupN2NConnection( final Set<Peer> peers ) throws PeerException
    {
        Set<String> usedN2NSubnets = getN2NSubnets( peers );
        LOG.debug( String.format( "Found %d n2n subnets:", usedN2NSubnets.size() ) );
        for ( String s : usedN2NSubnets )
        {
            LOG.debug( s );
        }

        String freeSubnet = N2NUtil.findFreeSubnet( usedN2NSubnets );

        LOG.debug( String.format( "Free subnet for peer: %s", freeSubnet ) );
        try
        {
            if ( freeSubnet == null )
            {
                throw new IllegalStateException( "Could not calculate subnet." );
            }
            String superNodeIp = getLocalPeer().getManagementHost().getExternalIp();
            String interfaceName = N2NUtil.generateInterfaceName( freeSubnet );
            String communityName = N2NUtil.generateCommunityName( freeSubnet );
            String sharedKey = UUID.randomUUID().toString();
            SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( freeSubnet, N2NUtil.N2N_SUBNET_MASK ).getInfo();
            final String[] addresses = subnetInfo.getAllAddresses();
            int counter = 0;

            ExecutorService taskExecutor = Executors.newFixedThreadPool( peers.size() );

            ExecutorCompletionService<N2NConfig> executorCompletionService =
                    new ExecutorCompletionService<>( taskExecutor );


            List<N2NConfig> result = new ArrayList<>( peers.size() );
            for ( Peer peer : peers )
            {
                N2NConfig config = new N2NConfig( peer.getId(), superNodeIp, N2N_PORT, interfaceName, communityName,
                        addresses[counter], sharedKey );
                executorCompletionService.submit( new SetupN2NConnectionTask( peer, config ) );
                counter++;
            }

            for ( Peer peer : peers )
            {
                final Future<N2NConfig> f = executorCompletionService.take();
                N2NConfig config = f.get();
                result.add( config );
                counter++;
            }

            taskExecutor.shutdown();

            return result;
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not create n2n tunnel.", e );
        }
    }


    /**
     * Returns set of currently used n2n subnets of given peers.
     *
     * @param peers set of peers
     *
     * @return set of currently used n2n subnets.
     */
    private Set<String> getN2NSubnets( final Set<Peer> peers )
    {
        Set<String> result = new HashSet<>();

        for ( Peer peer : peers )
        {
            Set<Interface> r = peer.getNetworkInterfaces( N2NUtil.N2N_SUBNET_INTERFACES_PATTERN );

            Collection peerSubnets = CollectionUtils.collect( r, new Transformer()
            {
                @Override
                public Object transform( final Object o )
                {
                    Interface i = ( Interface ) o;
                    SubnetUtils u = new SubnetUtils( i.getIp(), PEER_SUBNET_MASK );
                    return u.getInfo().getNetworkAddress();
                }
            } );

            result.addAll( peerSubnets );
        }

        return result;
    }


    private class SetupN2NConnectionTask implements Callable<N2NConfig>
    {
        private Peer peer;
        private N2NConfig n2NConfig;


        public SetupN2NConnectionTask( final Peer peer, final N2NConfig config )
        {
            this.peer = peer;
            this.n2NConfig = config;
        }


        @Override
        public N2NConfig call() throws Exception
        {
            peer.setupN2NConnection( n2NConfig );
            return n2NConfig;
        }
    }


    @Override
    public PeerInfo getLocalPeerInfo()
    {
        return localPeer.getPeerInfo();
    }


    public void addRequestListener( RequestListener listener )
    {
        localPeer.addRequestListener( listener );
    }


    public void removeRequestListener( RequestListener listener )
    {
        localPeer.removeRequestListener( listener );
    }
}

