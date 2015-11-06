package io.subutai.core.peer.impl;


import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.Interface;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerGateway;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.Encrypted;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.util.N2NUtil;
import io.subutai.common.util.SecurityUtilities;
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
@PermitAll
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
    private Map<String, RegistrationData> registrationRequests = new ConcurrentHashMap<>();


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


    @RolesAllowed( "Peer-Management|A|Write" )
    @Override
    public boolean register( final PeerInfo peerInfo )
    {
        return peerDAO.saveInfo( SOURCE_REMOTE_PEER, peerInfo.getId(), peerInfo );
    }

    private void register( final String keyPhrase, final RegistrationData registrationData ) throws PeerException
    {
        Preconditions.checkNotNull( keyPhrase, "Key phrase could not be null." );
        Preconditions.checkArgument( !keyPhrase.isEmpty(), "Key phrase could not be empty" );


        Encrypted encryptedData = registrationData.getData();
        try
        {
            byte[] key = SecurityUtilities.generateKey( keyPhrase.getBytes( "UTF-8" ) );
            String decryptedCert = encryptedData.decrypt( key, String.class );
            securityManager.getKeyStoreManager()
                           .importCertAsTrusted( ChannelSettings.SECURE_PORT_X2, registrationData.getPeerInfo().getId(),
                                   decryptedCert );

            registrationData.getPeerInfo().setKeyPhrase( keyPhrase );
            peerDAO.saveInfo( SOURCE_REMOTE_PEER, registrationData.getPeerInfo().getId(),
                    registrationData.getPeerInfo() );
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage(), e );
            throw new PeerException( "Could not register peer." );
        }
    }


//    @Override
//    public boolean unregister( final PeerInfo peerInfo, String keyPhrase ) throws PeerException
//    {
//        ManagementHost mgmHost = getLocalPeer().getManagementHost();
//        PeerInfo p = getPeerInfo( peerInfo.getId() );
//
//        if ( !p.getKeyPhrase().equals( keyPhrase ) )
//        {
//            return false;
//        }
//
//        mgmHost.removeRepository( p.getId(), p.getIp() );
//
//        PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( p.getId() );
//        // Remove peer policy of the target remote peer from the local peer
//        if ( peerPolicy != null )
//        {
//            localPeer.getPeerInfo().getPeerPolicies().remove( peerPolicy );
//            peerDAO.saveInfo( SOURCE_LOCAL_PEER, localPeer.getId(), localPeer );
//        }
//
//        //*********Remove Security Relationship  ****************************
//        securityManager.getKeyManager().removePublicKeyRing( p.getId() );
//        //*******************************************************************
//        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, p.getId() );
//    }

//
//    @Override
//    public boolean unregister( final String id ) throws PeerException
//    {
//        ManagementHost mgmHost = getLocalPeer().getManagementHost();
//        PeerInfo p = getPeerInfo( id );
//
//        mgmHost.removeRepository( p.getId(), p.getIp() );
//
//        PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( p.getId() );
//        // Remove peer policy of the target remote peer from the local peer
//        if ( peerPolicy != null )
//        {
//            localPeer.getPeerInfo().getPeerPolicies().remove( peerPolicy );
//            peerDAO.saveInfo( SOURCE_LOCAL_PEER, localPeer.getId(), localPeer );
//        }
//
//        //*********Remove Security Relationship  ****************************
//        securityManager.getKeyManager().removePublicKeyRing( p.getId() );
//        //*******************************************************************
//        return peerDAO.deleteInfo( SOURCE_REMOTE_PEER, p.getId() );
//    }


    private boolean unregister( final RegistrationData registrationData ) throws PeerException
    {
        ManagementHost mgmHost = getLocalPeer().getManagementHost();
        PeerInfo p = getPeerInfo( registrationData.getPeerInfo().getId() );

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
    public List<N2NConfig> setupN2NConnection( final String environmentId, final Set<Peer> peers ) throws PeerException
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
                N2NConfig config =
                        new N2NConfig( peer.getId(), environmentId, superNodeIp, N2N_PORT, interfaceName, communityName,
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


    @Override
    public RegistrationData processRegistrationRequest( final RegistrationData registrationData ) throws PeerException
    {
        addRequest( registrationData );
        return new RegistrationData( getLocalPeerInfo(), registrationData.getKeyPhrase(), RegistrationStatus.WAIT );
    }


    @Override
    public void processUnregisterRequest( final RegistrationData registrationData ) throws PeerException
    {
        PeerInfo p = getPeerInfo( registrationData.getPeerInfo().getId() );

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


    @Override
    public void processRejectRequest( final RegistrationData registrationData ) throws PeerException
    {
        removeRequest( registrationData.getPeerInfo().getId() );
    }


    @Override
    public void processCancelRequest( final RegistrationData registrationData ) throws PeerException
    {
        removeRequest( registrationData.getPeerInfo().getId() );
    }


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


    @Override
    public void doRegistrationRequest( final String destinationHost, final String keyPhrase ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        RegistrationData result = registrationClient
                .sendInitRequest( destinationHost, buildRegistrationData( keyPhrase, RegistrationStatus.REQUESTED ) );

        result.setKeyPhrase( keyPhrase );
        addRequest( result );
    }


    @Override
    public void doCancelRequest( final RegistrationData request ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        registrationClient.sendCancelRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( request.getKeyPhrase(), RegistrationStatus.CANCELLED ) );

        removeRequest( request.getPeerInfo().getId() );
    }


    @Override
    public void doApproveRequest( final String keyPhrase, final RegistrationData request ) throws PeerException
    {
        register( keyPhrase, request );

        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        registrationClient.sendApproveRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( keyPhrase, RegistrationStatus.APPROVED ) );

        removeRequest( request.getPeerInfo().getId() );
    }


    @Override
    public void doRejectRequest( final RegistrationData request ) throws PeerException
    {
        RegistrationClient registrationClient = new RegistrationClientImpl( provider );

        registrationClient.sendRejectRequest( request.getPeerInfo().getIp(),
                buildRegistrationData( request.getKeyPhrase(), RegistrationStatus.REJECTED ) );

        removeRequest( request.getPeerInfo().getId() );
    }


    @Override
    public void doUnregisterRequest( final RegistrationData request ) throws PeerException
    {
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
                r.add( new RegistrationData( peer.getPeerInfo(), /*peer.getPeerInfo().getKeyPhrase(),*/
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


    @Override
    public void startContainer( final ContainerId containerId ) throws PeerException
    {
        localPeer.startContainer( containerId );
    }


    @Override
    public void stopContainer( final ContainerId containerId ) throws PeerException
    {
        localPeer.stopContainer( containerId );
    }


    @Override
    public void destroyContainer( final ContainerId containerId ) throws PeerException
    {
        localPeer.destroyContainer( containerId );
    }


    @Override
    public ContainerHostState getContainerState( ContainerId containerId )
    {
        return localPeer.getContainerState( containerId );
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


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        return localPeer.getProcessResourceUsage( containerId, pid );
    }


    @Override
    public PublicKeyContainer createEnvironmentKeyPair( final EnvironmentId environmentId ) throws PeerException
    {
        return localPeer.createEnvironmentKeyPair( environmentId );
    }


    @Override
    public void removeEnvironmentKeyPair( final EnvironmentId environmentId ) throws PeerException
    {
        localPeer.removeEnvironmentKeyPair( environmentId );
    }


    @Override
    public Set<Gateway> getGateways() throws PeerException
    {
        return localPeer.getGateways();
    }

    @Override
    public Set<Vni> getReservedVnis() throws PeerException
    {
        return localPeer.getReservedVnis();
    }


    @Override
    public Vni reserveVni( final Vni vni ) throws PeerException
    {
        return localPeer.reserveVni( vni );
    }


    @Override
    public void cleanupEnvironmentNetworkSettings( final EnvironmentId environmentId ) throws PeerException
    {
        localPeer.cleanupEnvironmentNetworkSettings( environmentId );
    }


    @Override
    public void removeN2NConnection( final EnvironmentId environmentId ) throws PeerException
    {
        localPeer.cleanupEnvironmentNetworkSettings( environmentId );
    }


    @Override
    public void addToTunnel( final N2NConfig config ) throws PeerException
    {
        localPeer.setupN2NConnection( config );
    }


    @Override
    public ResourceHostMetrics getResourceHostMetrics()
    {
        return localPeer.getResourceHostMetrics();
    }


    @Override
    public HostInterfaces getInterfaces()
    {
        return localPeer.getInterfaces();
    }
}

