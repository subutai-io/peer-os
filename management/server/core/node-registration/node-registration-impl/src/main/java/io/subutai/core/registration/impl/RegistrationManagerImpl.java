package io.subutai.core.registration.impl;


import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ManagementHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.PlacementStrategy;
import io.subutai.common.util.N2NUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.BrokerException;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.exception.NodeRegistrationException;
import io.subutai.core.registration.api.service.ContainerInfo;
import io.subutai.core.registration.api.service.ContainerToken;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.impl.dao.ContainerInfoDataService;
import io.subutai.core.registration.impl.dao.ContainerTokenDataService;
import io.subutai.core.registration.impl.dao.RequestDataService;
import io.subutai.core.registration.impl.entity.ContainerInfoImpl;
import io.subutai.core.registration.impl.entity.ContainerTokenImpl;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


public class RegistrationManagerImpl implements RegistrationManager, HostListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RegistrationManagerImpl.class );
    private SecurityManager securityManager;
    private RequestDataService requestDataService;
    private ContainerInfoDataService containerInfoDataService;
    private ContainerTokenDataService containerTokenDataService;
    private DaoManager daoManager;
    private Broker broker;
    private String domainName;
    private PeerManager peerManager;
    private EnvironmentManager environmentManager;
    private NetworkManager networkManager;


    public RegistrationManagerImpl( final SecurityManager securityManager, final DaoManager daoManager,
                                    String domainName )
    {
        this.securityManager = securityManager;
        this.daoManager = daoManager;
        this.domainName = domainName;
    }


    public void init()
    {
        containerTokenDataService = new ContainerTokenDataService( daoManager );
        requestDataService = new RequestDataService( daoManager );
        containerInfoDataService = new ContainerInfoDataService( daoManager );
    }


    public NetworkManager getNetworkManager()
    {
        return networkManager;
    }


    public void setNetworkManager( final NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
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
    public RequestedHost getRequest( final String requestId )
    {
        return requestDataService.find( requestId );
    }


    @Override
    public void queueRequest( final RequestedHost requestedHost ) throws NodeRegistrationException
    {
        if ( requestDataService.find( requestedHost.getId() ) != null )
        {
            LOGGER.info( "Already requested registration" );
        }
        else
        {
            RequestedHostImpl registrationRequest = new RequestedHostImpl( requestedHost );
            registrationRequest.setStatus( RegistrationStatus.REQUESTED );
            try
            {
                requestDataService.persist( registrationRequest );
            }
            catch ( Exception ex )
            {
                throw new NodeRegistrationException( "Failed adding resource host registration request to queue", ex );
            }
        }
    }


    @Override
    public void rejectRequest( final String requestId )
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


    private Set<String> getTunnelNetworks( final Set<Peer> peers )
    {
        Set<String> result = new HashSet<>();

        for ( Peer peer : peers )
        {
            Set<HostInterfaceModel> r = null;
            try
            {
                r = peer.getInterfaces().filterByIp( N2NUtil.N2N_INTERFACE_IP_PATTERN );
            }
            catch ( PeerException e )
            {
                e.printStackTrace();
            }

            Collection tunnels = CollectionUtils.collect( r, new Transformer()
            {
                @Override
                public Object transform( final Object o )
                {
                    HostInterface i = ( HostInterface ) o;
                    SubnetUtils u = new SubnetUtils( i.getIp(), N2NUtil.N2N_SUBNET_MASK );
                    return u.getInfo().getNetworkAddress();
                }
            } );

            result.addAll( tunnels );
        }

        return result;
    }


    @Override
    public void approveRequest( final String requestId )
    {
        RequestedHostImpl registrationRequest = requestDataService.find( requestId );

        if ( registrationRequest == null || !RegistrationStatus.REQUESTED.equals( registrationRequest.getStatus() ) )
        {
            return;
        }
        registrationRequest.setStatus( RegistrationStatus.APPROVED );
        requestDataService.update( registrationRequest );

        //todo sign RH key with Peer Key
        importHostPublicKey( registrationRequest.getId(), registrationRequest.getPublicKey() );

        importHostSslCert( registrationRequest.getId(), registrationRequest.getCert() );

        for ( final ContainerInfo containerInfo : registrationRequest.getHostInfos() )
        {
            importHostPublicKey( containerInfo.getId(), containerInfo.getPublicKey() );
        }

        processEnvironmentImport( registrationRequest );
    }


    private void importHostSslCert( String hostId, String cert )
    {
        try
        {
            broker.registerClientCertificate( hostId, cert );
        }
        catch ( BrokerException e )
        {
            LOGGER.error( "Error importing host SSL certificate", e );
        }
    }


    private void importHostPublicKey( String hostId, String publicKey )
    {
        try
        {
            KeyManager keyManager = securityManager.getKeyManager();
            keyManager.savePublicKeyRing( hostId, ( short ) 2, publicKey );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Error importing host public key", ex );
        }
    }


    private Map<Integer, Map<String, Set<ContainerInfo>>> groupContainersByVlan( Set<ContainerInfo> containerInfoSet )
    {
        Map<Integer, Map<String, Set<ContainerInfo>>> groupedContainersByVlan = Maps.newHashMap();

        for ( final ContainerInfo containerInfo : containerInfoSet )
        {
            //Group containers by environment relation
            // and group into node groups.
            Map<String, Set<ContainerInfo>> groupedContainers = groupedContainersByVlan.get( containerInfo.getVlan() );
            if ( groupedContainers == null )
            {
                groupedContainers = Maps.newHashMap();
            }

            //Group by container infos by container name
            Set<ContainerInfo> group = groupedContainers.get( containerInfo.getTemplateName() );
            if ( group != null )
            {
                group.add( containerInfo );
            }
            else
            {
                group = Sets.newHashSet( containerInfo );
            }


            if ( containerInfo.getVlan() != 0 )
            {
                groupedContainers.put( containerInfo.getTemplateName(), group );
                groupedContainersByVlan.put( containerInfo.getVlan(), groupedContainers );
            }
        }
        return groupedContainersByVlan;
    }


    private void processEnvironmentImport( RequestedHostImpl registrationRequest )
    {
        Map<Integer, Map<String, Set<ContainerInfo>>> groupedContainersByVlan =
                groupContainersByVlan( registrationRequest.getHostInfos() );

        LocalPeer localPeer = peerManager.getLocalPeer();

        for ( final Map.Entry<Integer, Map<String, Set<ContainerInfo>>> mapEntry : groupedContainersByVlan.entrySet() )
        {
            //TODO: check this run. Topology constructor changed
            Topology topology = new Topology( "Imported-environment", null, null, null );
            Map<String, Set<ContainerInfo>> rawNodeGroup = mapEntry.getValue();
            Map<NodeGroup, Set<ContainerHostInfo>> classification = Maps.newHashMap();

            for ( final Map.Entry<String, Set<ContainerInfo>> entry : rawNodeGroup.entrySet() )
            {
                //place where to create node groups
                String templateName = entry.getKey();
                NodeGroup nodeGroup =
                        new NodeGroup( String.format( "%s_group", templateName ), templateName, entry.getValue().size(),
                                1, 1, new PlacementStrategy( "ROUND_ROBIN" ), localPeer.getId() );
                topology.addNodeGroupPlacement( localPeer, nodeGroup );

                Set<ContainerHostInfo> converter = Sets.newHashSet();
                converter.addAll( entry.getValue() );
                classification.put( nodeGroup, converter );
            }
            //trigger environment import task
            try
            {
                Environment environment = environmentManager
                        .importEnvironment( String.format( "environment_%d", mapEntry.getKey() ), topology,
                                classification, "", mapEntry.getKey() );

                //Save container gateway from environment configuration to update container network configuration
                // later when it will be available
                SubnetUtils cidr;

                try
                {
                    cidr = new SubnetUtils( environment.getSubnetCidr() );
                }
                catch ( IllegalArgumentException e )
                {
                    throw new RuntimeException( "Failed to parse subnet CIDR", e );
                }

                String gateway = cidr.getInfo().getLowAddress();
                for ( final Set<ContainerHostInfo> infos : classification.values() )
                {
                    //TODO: sign CH key with PEK (identified by LocalPeerId+environment.getId())
                    for ( final ContainerHostInfo hostInfo : infos )
                    {
                        ContainerInfoImpl containerInfo = containerInfoDataService.find( hostInfo.getId() );
                        containerInfo.setGateway( gateway );
                        containerInfo.setStatus( RegistrationStatus.APPROVED );
                        containerInfoDataService.update( containerInfo );
                    }
                }
            }
            catch ( EnvironmentCreationException e )
            {
                LOGGER.error( "Error importing environment", e );
            }
        }
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, Set<QuotaAlertValue> alerts )
    {
        RequestedHostImpl requestedHost = requestDataService.find( resourceHostInfo.getId() );
        if ( requestedHost != null && requestedHost.getStatus() == RegistrationStatus.APPROVED )
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            try
            {
                ResourceHost resourceHost = localPeer.getResourceHostById( resourceHostInfo.getId() );
                Map<Integer, Set<ContainerHost>> containerHostList = Maps.newHashMap();
                for ( final ContainerInfo containerInfo : requestedHost.getHostInfos() )
                {
                    if ( RegistrationStatus.APPROVED.equals( containerInfo.getState() )
                            && containerInfo.getVlan() != 0 )
                    {

                        ContainerInfoImpl containerInfoImpl = containerInfoDataService.find( containerInfo.getId() );

                        ContainerHost containerHost = resourceHost.getContainerHostById( containerInfo.getId() );

                        containerInfoImpl.setStatus( RegistrationStatus.REGISTERED );
                        containerInfoDataService.update( containerInfoImpl );

                        //we assume that newly imported environment has always default sshGroupId=1, hostsGroupId=1

                        //configure hosts on each group | group containers by ssh group
                        Set<ContainerHost> containers = containerHostList.get( containerInfoImpl.getVlan() );
                        if ( containers == null )
                        {
                            containers = Sets.newHashSet();
                        }
                        containers.add( containerHost );
                    }
                }
                for ( final Map.Entry<Integer, Set<ContainerHost>> entry : containerHostList.entrySet() )
                {
                    configureHosts( entry.getValue() );
                }
            }
            catch ( HostNotFoundException e )
            {
                //ignore
            }
            catch ( NetworkManagerException e )
            {
                LOGGER.error( "Error configuring container hosts", e );
            }
        }
    }


    private void configureHosts( final Set<ContainerHost> containerHosts ) throws NetworkManagerException
    {
        //assume that inside one host group the domain name must be the same for all containers
        //so pick one container's domain name as the group domain name
        networkManager.registerHosts( containerHosts, domainName );

        networkManager.exchangeSshKeys( containerHosts );
    }


    @Override
    public void removeRequest( final String requestId )
    {
        requestDataService.remove( requestId );
    }


    @Override
    public void deployResourceHost( List<String> args ) throws NodeRegistrationException
    {
        Host managementHost = null;
        CommandResult result;

        try
        {
            managementHost = peerManager.getLocalPeer().getManagementHost();

            Set<Peer> peers = Sets.newHashSet( managementHost.getPeer() );

            Set<String> existingNetworks = getTunnelNetworks( peers );

            String freeTunnelNetwork = N2NUtil.findFreeTunnelNetwork( existingNetworks );
            args.add( "-I" );
            freeTunnelNetwork = freeTunnelNetwork.substring( 0, freeTunnelNetwork.length() - 1 ) + (
                    Integer.valueOf( freeTunnelNetwork.substring( freeTunnelNetwork.length() - 1 ) ) + 1 );
            args.add( freeTunnelNetwork );

            int ipOctet = ( Integer.valueOf( freeTunnelNetwork.substring( freeTunnelNetwork.length() - 1 ) ) + 1 );
            String ipRh = freeTunnelNetwork.substring( 0, freeTunnelNetwork.length() - 1 ) + ipOctet;
            args.add( "-i" );
            args.add( ipRh );

            String communityName = N2NUtil.generateCommunityName( freeTunnelNetwork );
            args.add( "-n" );
            args.add( communityName );

            String deviceName = N2NUtil.generateInterfaceName( freeTunnelNetwork );
            args.add( "-d" );
            args.add( deviceName );
            String runUser = "root";
            result = managementHost.execute(
                    new RequestBuilder( "/apps/subutai-mng/current/awsdeploy/awsdeploy" ).withCmdArgs( args )
                                                                                         .withRunAs( runUser )
                                                                                         .withTimeout( 600 ) );

            if ( result.getExitCode() != 0 )
            {
                throw new NodeRegistrationException( result.getStdErr() );
            }
        }
        catch ( HostNotFoundException | CommandException e )
        {
            e.printStackTrace();
        }
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
}
