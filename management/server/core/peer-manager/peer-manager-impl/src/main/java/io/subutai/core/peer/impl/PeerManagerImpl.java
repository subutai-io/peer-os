package io.subutai.core.peer.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.Interface;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationRequest;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.util.N2NUtil;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.RegistrationClient;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.command.CommandRequestListener;
import io.subutai.core.peer.impl.command.CommandResponseListener;
import io.subutai.core.peer.impl.container.CreateEnvironmentContainerGroupRequestListener;
import io.subutai.core.peer.impl.container.DestroyEnvironmentContainerGroupRequestListener;
import io.subutai.core.peer.impl.dao.PeerDAO;
import io.subutai.core.peer.impl.request.MessageResponseListener;
import io.subutai.core.security.api.SecurityManager;


/**
 * PeerManager implementation
 */
public class PeerManagerImpl implements PeerManager
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerManagerImpl.class );

    public static final String PEER_SUBNET_MASK = "255.255.255.0";
    private static final int N2N_PORT = 5000;

    protected PeerDAO peerDAO;
    protected LocalPeer localPeer;
    protected Messenger messenger;
    protected CommandResponseListener commandResponseListener;
    private MessageResponseListener messageResponseListener;
    private DaoManager daoManager;
    private SecurityManager securityManager;
    private Object provider;
    private Map<String, RegistrationRequest> registrationRequests = new ConcurrentHashMap<>();


    public PeerManagerImpl( final Messenger messenger, LocalPeer localPeer, DaoManager daoManager,
                            MessageResponseListener messageResponseListener, SecurityManager securityManager,
                            Object provider )
    {
        this.messenger = messenger;
        this.localPeer = localPeer;
        this.daoManager = daoManager;
        this.messageResponseListener = messageResponseListener;
        this.securityManager = securityManager;
        this.provider = provider;
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
        addRequestListener( new CreateEnvironmentContainerGroupRequestListener( localPeer ) );
        //add destroy environment containers requests listener
        addRequestListener( new DestroyEnvironmentContainerGroupRequestListener( localPeer ) );
    }


    public void destroy()
    {
        commandResponseListener.dispose();
    }


    @Override
    public boolean register( final PeerInfo peerInfo )
    {
        return peerDAO.saveInfo( SOURCE_REMOTE_PEER, peerInfo.getId(), peerInfo );
    }


    private boolean register( final RegistrationRequest registrationRequest )
    {
        //TODO: handle x509 certificate

        registrationRequest.getPeerInfo().setKeyPhrase( registrationRequest.getKeyPhrase() );
        return peerDAO.saveInfo( SOURCE_REMOTE_PEER, registrationRequest.getPeerInfo().getId(),
                registrationRequest.getPeerInfo() );
    }


    @Override
    public boolean unregister( final PeerInfo peerInfo, String keyPhrase ) throws PeerException
    {
        ManagementHost mgmHost = getLocalPeer().getManagementHost();
        PeerInfo p = getPeerInfo( peerInfo.getId() );

        if ( !p.getKeyPhrase().equals( keyPhrase ) )
        {
            return false;
        }

        mgmHost.removeRepository( p.getId(), p.getIp() );

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
        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, p.getId() );
    }


    @Override
    public boolean unregister( final String id ) throws PeerException
    {
        ManagementHost mgmHost = getLocalPeer().getManagementHost();
        PeerInfo p = getPeerInfo( id );

        mgmHost.removeRepository( p.getId(), p.getIp() );

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
        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, p.getId() );
    }


    public boolean unregister( final RegistrationRequest registrationRequest ) throws PeerException
    {
        ManagementHost mgmHost = getLocalPeer().getManagementHost();
        PeerInfo p = getPeerInfo( registrationRequest.getPeerInfo().getId() );
        //
        //        if ( !p.getKeyPhrase().equals( registrationRequest.getPeerInfo().getKeyPhrase() ) )
        //        {
        //            return false;
        //        }

        try
        {
            mgmHost.removeRepository( p.getId(), p.getIp() );
        }
        catch ( Exception ignore )
        {
            // ignore
        }
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

        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, p.getId() );
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
        return peerDAO.saveInfo( source, peerInfo.getId(), peerInfo );
    }


    @Override
    public List<PeerInfo> getPeerInfos()
    {
        return peerDAO.getInfo( SOURCE_REMOTE_PEER, PeerInfo.class );
    }


    @Override
    @RolesAllowed( "admin" )
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
            return new RemotePeerImpl( localPeer, pi, messenger, commandResponseListener, messageResponseListener,
                    provider );
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

        String freeSubnet = N2NUtil.findFreeTunnelNetwork( usedN2NSubnets );

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

            for ( Peer ignored : peers )
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
            HostInterfaces intfs = peer.getInterfaces();

            Set<HostInterface> r = intfs.filterByIp( N2NUtil.N2N_INTERFACE_IP_PATTERN );

            Collection peerSubnets = CollectionUtils.<String>collect( r, new Transformer()
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


    private void addRequest( final RegistrationRequest registrationRequest )
    {
        this.registrationRequests.put( registrationRequest.getPeerInfo().getId(), registrationRequest );
    }


    private void removeRequest( final RegistrationRequest registrationRequest )
    {
        this.registrationRequests.remove( registrationRequest.getPeerInfo().getId() );
    }


    @Override
    public RegistrationRequest processRegistrationRequest( final RegistrationRequest registrationRequest )
            throws PeerException
    {
        addRequest( registrationRequest );
        return new RegistrationRequest( getLocalPeerInfo(), registrationRequest.getKeyPhrase(),
                RegistrationStatus.WAIT );
    }


    @Override
    public void processUnregisterRequest( final RegistrationRequest registrationRequest ) throws PeerException
    {
        removeRequest( registrationRequest );
        unregister( registrationRequest );
    }


    @Override
    public void processRejectRequest( final RegistrationRequest registrationRequest ) throws PeerException
    {
        removeRequest( registrationRequest );
    }


    @Override
    public void processCancelRequest( final RegistrationRequest registrationRequest ) throws PeerException
    {
        removeRequest( registrationRequest );
    }


    @Override
    public RegistrationRequest processApproveRequest( final RegistrationRequest registrationRequest )
            throws PeerException
    {
        //todo: import x509 cert
        final RegistrationRequest result =
                new RegistrationRequest( getLocalPeerInfo(), registrationRequest.getKeyPhrase(),
                        RegistrationStatus.APPROVED );
        //todo:add certs
        register( registrationRequest );
        removeRequest( registrationRequest );
        return result;
    }


    @Override
    public void doRegistrationRequest( final String destinationHost, final String keyPhrase ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );
        final RegistrationRequest registrationRequest =
                new RegistrationRequest( localPeer.getPeerInfo(), keyPhrase, RegistrationStatus.REQUESTED );
        RegistrationRequest result = registrationClient.sendInitRequest( destinationHost, registrationRequest );

        addRequest( result );
    }


    @Override
    public void doCancelRequest( final RegistrationRequest request ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        RegistrationRequest r = new RegistrationRequest( localPeer.getPeerInfo(), request.getKeyPhrase(),
                RegistrationStatus.CANCELLED );
        registrationClient.sendCancelRequest( request.getPeerInfo().getIp(), r );

        removeRequest( request );
    }


    @Override
    public void doApproveRequest( final RegistrationRequest request ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );
        final RegistrationRequest approveRequest =
                new RegistrationRequest( localPeer.getPeerInfo(), request.getKeyPhrase(), RegistrationStatus.APPROVED );

        //TODO: attache local x509 sert
        RegistrationRequest response =
                registrationClient.sendApproveRequest( request.getPeerInfo().getIp(), approveRequest );

        register( response );
        removeRequest( request );
    }


    @Override
    public void doRejectRequest( final RegistrationRequest request ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        final RegistrationRequest rejectRequest =
                new RegistrationRequest( localPeer.getPeerInfo(), request.getKeyPhrase(), RegistrationStatus.REJECTED );
        registrationClient.sendRejectRequest( request.getPeerInfo().getIp(), rejectRequest );

        removeRequest( request );
    }


    @Override
    public void doUnregisterRequest( final RegistrationRequest request ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );
        final RegistrationRequest unregisterRequest =
                new RegistrationRequest( localPeer.getPeerInfo(), request.getKeyPhrase(),
                        RegistrationStatus.UNREGISTERED );
        registrationClient.sendUnregisterRequest( request.getPeerInfo().getIp(), unregisterRequest );

        unregister( request );

        removeRequest( request );
    }


    @Override
    public List<RegistrationRequest> getRegistrationRequests()
    {
        List<RegistrationRequest> r = new ArrayList<>( registrationRequests.values() );
        for ( Peer peer : getPeers() )
        {
            if ( !peer.getId().equals( getLocalPeerInfo().getId() ) )
            {
                r.add( new RegistrationRequest( peer.getPeerInfo(), peer.getPeerInfo().getKeyPhrase(),
                        RegistrationStatus.APPROVED ) );
            }
        }

        return r;
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

