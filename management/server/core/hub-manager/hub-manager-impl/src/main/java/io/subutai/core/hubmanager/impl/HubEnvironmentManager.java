package io.subutai.core.hubmanager.impl;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Gateways;
import io.subutai.common.network.Vni;
import io.subutai.common.network.Vnis;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.dto.PublicKeyContainer;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.network.GatewayDto;
import io.subutai.hub.share.dto.network.VniDto;


public class HubEnvironmentManager
{
    private static final Logger LOG = LoggerFactory.getLogger( HubEnvironmentManager.class.getName() );

    private SecurityManager securityManager;
    private PeerManager peerManager;
    private ConfigManager configManager;
    private IdentityManager identityManager;
    private EnvironmentManager environmentManager;


    public HubEnvironmentManager( final EnvironmentManager environmentManager, final ConfigManager hConfigManager,
                                  final PeerManager peerManager, final IdentityManager identityManager )
    {
        this.environmentManager = environmentManager;
        this.configManager = hConfigManager;
        this.peerManager = peerManager;
        this.identityManager = identityManager;
    }


    public Set<VniDto> getReservedVnis( EnvironmentPeerDto peerDto )
    {
        Set<VniDto> vniDtos = new HashSet<>();
        try
        {
            Vnis vnis = peerManager.getLocalPeer().getReservedVnis();
            for ( Vni vni : vnis.list() )
            {
                VniDto vniDto = new VniDto();
                vniDto.setPeerId( peerManager.getLocalPeer().getId() );
                vniDto.setEnvironmentId( peerDto.getEnvironmentInfo().getId() );
                vniDto.setVni( vni.getVni() );
                vniDto.setVlan( vni.getVlan() );
                vniDtos.add( vniDto );
            }
            return vniDtos;
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not get local peer reserved vnis" );
        }
        return null;
    }


    public Set<String> getTunnelNetworks()
    {
        Set<String> usedInterfaces = new HashSet<>();
        try
        {
            Set<HostInterfaceModel> r =
                    peerManager.getLocalPeer().getInterfaces().filterByIp( P2PUtil.P2P_INTERFACE_IP_PATTERN );


            Collection tunnels = CollectionUtils.collect( r, new Transformer()
            {
                @Override
                public Object transform( final Object o )
                {
                    HostInterface i = ( HostInterface ) o;
                    SubnetUtils u = new SubnetUtils( i.getIp(), P2PUtil.P2P_SUBNET_MASK );
                    return u.getInfo().getNetworkAddress();
                }
            } );

            usedInterfaces.addAll( tunnels );
            return usedInterfaces;
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not get local peer used interfaces" );
        }
        return null;
    }


    public Set<GatewayDto> getReservedGateways( EnvironmentPeerDto peerDto )
    {
        Set<GatewayDto> gatewayDtos = new HashSet<>();
        try
        {
            Gateways gateways = peerManager.getLocalPeer().getGateways();
            for ( Gateway gateway : gateways.list() )
            {
                GatewayDto gatewayDto = new GatewayDto();
                gatewayDto.setIp( gateway.getIp() );
                gatewayDto.setVlan( gateway.getVlan() );
                gatewayDto.setPeerId( peerManager.getLocalPeer().getId() );
                gatewayDto.setEnvironmentId( peerDto.getEnvironmentInfo().getId() );
                gatewayDtos.add( gatewayDto );
            }
            return gatewayDtos;
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not get local peer used interfaces" );
        }
        return null;
    }


    public PublicKeyContainer createPeerEnvironmentKeyPair( EnvironmentId environmentId )
    {
        try
        {
            io.subutai.common.security.PublicKeyContainer publicKeyContainer =
                    peerManager.getLocalPeer().createPeerEnvironmentKeyPair( environmentId );

            PublicKeyContainer keyContainer = new PublicKeyContainer();
            keyContainer.setKey( publicKeyContainer.getKey() );
            keyContainer.setHostId( publicKeyContainer.getHostId() );
            keyContainer.setFingerprint( publicKeyContainer.getFingerprint() );

            return keyContainer;
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not create local peer PEK" );
        }
        return null;
    }
}
