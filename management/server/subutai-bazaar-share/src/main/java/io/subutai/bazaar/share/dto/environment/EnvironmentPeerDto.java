package io.subutai.bazaar.share.dto.environment;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.subutai.bazaar.share.dto.PublicKeyContainer;
import io.subutai.bazaar.share.dto.UserTokenDto;
import io.subutai.bazaar.share.dto.ansible.AnsibleDto;


public class EnvironmentPeerDto
{
    public enum PeerState
    {
        EXCHANGE_INFO, RESERVE_NETWORK, SETUP_TUNNEL, BUILD_CONTAINER, CONFIGURE_CONTAINER, CONFIGURE_DOMAIN,
        CHANGE_CONTAINER_STATE, CONFIGURE_ENVIRONMENT, CHECK_NETWORK, DELETE_PEER, WAIT, READY, ERROR
    }


    private String peerId;

    private EnvironmentInfoDto environmentInfo;

    private String ownerId;

    private Long ssUserId;

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

    private UserTokenDto userToken;

    private String message;

    @JsonInclude( JsonInclude.Include.NON_EMPTY )
    private Integer vlan;

    private String ansible;

    private String playbook;

    //TODO rename to cdnToken
    private String kurjunToken;

    private AnsibleDto ansibleDto;

    private EnvironmentTelemetryDto environmentTelemetryDto;

    private Set<P2PStatusDto> p2pStatuses = new HashSet<>();


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


    public Long getSsUserId()
    {
        return ssUserId;
    }


    public void setSsUserId( Long ssUserId )
    {
        this.ssUserId = ssUserId;
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


    public UserTokenDto getUserToken()
    {
        return userToken;
    }


    public void setUserToken( final UserTokenDto userToken )
    {
        this.userToken = userToken;
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


    public String getAnsible()
    {
        return ansible;
    }


    public void setAnsible( final String ansible )
    {
        this.ansible = ansible;
    }


    public String getPlaybook()
    {
        return playbook;
    }


    public void setPlaybook( final String playbook )
    {
        this.playbook = playbook;
    }


    public String getKurjunToken()
    {
        return kurjunToken;
    }


    public void setKurjunToken( final String kurjunToken )
    {
        this.kurjunToken = kurjunToken;
    }


    public AnsibleDto getAnsibleDto()
    {
        return ansibleDto;
    }


    public void setAnsibleDto( final AnsibleDto ansibleDto )
    {
        this.ansibleDto = ansibleDto;
    }


    public EnvironmentTelemetryDto getEnvironmentTelemetryDto()
    {
        return environmentTelemetryDto;
    }


    public void setEnvironmentTelemetryDto( final EnvironmentTelemetryDto environmentTelemetryDto )
    {
        this.environmentTelemetryDto = environmentTelemetryDto;
    }


    public Set<P2PStatusDto> getP2pStatuses()
    {
        return p2pStatuses;
    }


    public void setP2pStatuses( final Set<P2PStatusDto> p2pStatuses )
    {
        this.p2pStatuses = p2pStatuses;
    }


    public void setError( String message )
    {
        setState( PeerState.ERROR );
        setMessage( message );
    }
}
