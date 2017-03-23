package io.subutai.hub.share.dto.environment;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.subutai.hub.share.dto.PublicKeyContainer;


public class EnvironmentPeerDto
{
    public enum PeerState
    {
        EXCHANGE_INFO,
        RESERVE_NETWORK,
        SETUP_TUNNEL,
        BUILD_CONTAINER,
        CONFIGURE_CONTAINER,
        CONFIGURE_DOMAIN,
        CHANGE_CONTAINER_STATE,
        DELETE_PEER,
        WAIT,
        READY,
        ERROR
    }


    private String peerId;

    private EnvironmentInfoDto environmentInfo;

    private String ownerId;

    private PeerState requestState;

    private Set<String> p2pSubnets = new HashSet<>();

    private Set<Long> vnis = new HashSet<>();

    private Set<String> containerSubnets = new HashSet<>();

    private PublicKeyContainer publicKey;

    private String interfaceName;

    private String communityName;

    private String tunnelAddress;

    private String p2pSecretKey;

    private Boolean setupTunnel;

    private Set<EnvironmentPeerRHDto> rhs = new HashSet<>();

    private String peerToken;

    private String peerTokenId;

    private String envOwnerToken;

    private String envOwnerTokenId;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer vlan;


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


    public Set<String> getP2pSubnets()
    {
        return p2pSubnets;
    }


    public void setP2pSubnets( final Set<String> p2pSubnets )
    {
        this.p2pSubnets = p2pSubnets;
    }


    public void addP2pSubnet( final String p2pSubnet )
    {
        this.p2pSubnets.add( p2pSubnet );
    }


    public void removeP2pSubnet( final String p2pSubnet )
    {
        this.p2pSubnets.remove( p2pSubnet );
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


    public Set<String> getContainerSubnets()
    {
        return containerSubnets;
    }


    public void setContainerSubnets( final Set<String> containerSubnets )
    {
        this.containerSubnets = containerSubnets;
    }


    public void addContainerSubnet( final String containerSubnet )
    {
        this.containerSubnets.add( containerSubnet );
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


    public Set<EnvironmentPeerRHDto> getRhs()
    {
        return rhs;
    }


    public void setRhs( final Set<EnvironmentPeerRHDto> rhs )
    {
        this.rhs = rhs;
    }


    public void addRH( final EnvironmentPeerRHDto rh )
    {
        this.rhs.add( rh );
    }


    public String getPeerToken()
    {
        return peerToken;
    }


    public void setPeerToken( final String peerToken )
    {
        this.peerToken = peerToken;
    }


    public String getEnvOwnerToken()
    {
        return envOwnerToken;
    }


    public void setEnvOwnerToken( final String envOwnerToken )
    {
        this.envOwnerToken = envOwnerToken;
    }


    public String getPeerTokenId()
    {
        return peerTokenId;
    }


    public void setPeerTokenId( final String peerTokenId )
    {
        this.peerTokenId = peerTokenId;
    }


    public String getEnvOwnerTokenId()
    {
        return envOwnerTokenId;
    }


    public void setEnvOwnerTokenId( final String envOwnerTokenId )
    {
        this.envOwnerTokenId = envOwnerTokenId;
    }


    public String getMessage()
    {
        return message;
    }


    public void setMessage( String message )
    {
        this.message = message;
    }


    public Integer getVlan()
    {
        return vlan;
    }


    public void setVlan( final Integer vlan )
    {
        this.vlan = vlan;
    }


    public void setError( String message )
    {
        setState( PeerState.ERROR );
        setMessage( message );
    }
}
