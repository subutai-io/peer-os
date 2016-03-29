package io.subutai.hub.share.dto.environment;


import java.util.HashSet;
import java.util.Set;

import io.subutai.hub.share.dto.PublicKeyContainer;


public class EnvironmentPeerDto
{
    public enum PeerState
    {
        EXCHANGE_INFO,
        SETUP_P2P,
        SETUP_TUNNEL,
        BUILD_CONTAINER,
        CONFIGURE_CONTAINER,
        START_CONTAINER,
        STOP_CONTAINER,
        DESTROY_CONTAINER,
        WAIT,
        READY
    }


    private String peerId;

    private EnvironmentInfoDto environmentInfo;

    private String ownerId;

    private PeerState requestState;

    private Set<String> usedIPs = new HashSet<>();

    private Set<Long> vnis = new HashSet<>();

    private Set<String> gateways = new HashSet<>();

    private PublicKeyContainer publicKey;

    private String interfaceName;

    private String communityName;

    private String tunnelAddress;

    private String p2pSecretKey;

    private Boolean setupTunnel;


    public EnvironmentPeerDto()
    {
    }


    public EnvironmentPeerDto( PeerState requestState )
    {
        this.requestState = requestState;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public PeerState getState()
    {
        return requestState;
    }


    public void setState( final PeerState requestState )
    {
        this.requestState = requestState;
    }


    public EnvironmentInfoDto getEnvironmentInfo()
    {
        return environmentInfo;
    }


    public void setEnvironmentInfo( final EnvironmentInfoDto environmentInfo )
    {
        this.environmentInfo = environmentInfo;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    public Set<String> getUsedIPs()
    {
        return usedIPs;
    }


    public void setUsedIPs( final Set<String> usedIPs )
    {
        this.usedIPs = usedIPs;
    }


    public void addUsedIP( final String usedId )
    {
        this.usedIPs.add( usedId );
    }


    public void removeUsedIP( final String usedId )
    {
        this.usedIPs.remove( usedId );
    }


    public Set<Long> getVnis()
    {
        return vnis;
    }


    public void setVnis( final Set<Long> vnis )
    {
        this.vnis = vnis;
    }


    public void addVni( final Long vni )
    {
        this.vnis.add( vni );
    }


    public Set<String> getGateways()
    {
        return gateways;
    }


    public void setGateways( final Set<String> gateways )
    {
        this.gateways = gateways;
    }

    public void addGateway( final String gateway )
    {
        this.gateways.add( gateway );
    }


    public PublicKeyContainer getPublicKey()
    {
        return publicKey;
    }


    public void setPublicKey( final PublicKeyContainer publicKey )
    {
        this.publicKey = publicKey;
    }


    public String getInterfaceName()
    {
        return interfaceName;
    }


    public void setInterfaceName( final String interfaceName )
    {
        this.interfaceName = interfaceName;
    }


    public String getCommunityName()
    {
        return communityName;
    }


    public void setCommunityName( final String communityName )
    {
        this.communityName = communityName;
    }


    public String getTunnelAddress()
    {
        return tunnelAddress;
    }


    public void setTunnelAddress( final String tunnelAddress )
    {
        this.tunnelAddress = tunnelAddress;
    }


    public String getP2pSecretKey()
    {
        return p2pSecretKey;
    }


    public void setP2pSecretKey( final String p2pSecretKey )
    {
        this.p2pSecretKey = p2pSecretKey;
    }


    public Boolean getSetupTunnel()
    {
        return setupTunnel;
    }


    public void setSetupTunnel( final Boolean setupTunnel )
    {
        this.setupTunnel = setupTunnel;
    }
}
