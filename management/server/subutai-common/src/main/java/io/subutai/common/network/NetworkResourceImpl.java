package io.subutai.common.network;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class NetworkResourceImpl implements NetworkResource
{
    @JsonProperty( "environmentId" )
    private String environmentId;
    @JsonProperty( "vni" )
    private long vni;
    @JsonProperty( "p2pSubnet" )
    private String p2pSubnet;
    @JsonProperty( "containerSubnet" )
    private String containerSubnet;
    @JsonProperty( "initiatorPeerId" )
    private String initiatorPeerId;


    public NetworkResourceImpl( @JsonProperty( "environmentId" ) final String environmentId,
                                @JsonProperty( "vni" ) final long vni,
                                @JsonProperty( "p2pSubnet" ) final String p2pSubnet,
                                @JsonProperty( "containerSubnet" ) final String containerSubnet,
                                @JsonProperty( "initiatorPeerId" ) final String initiatorPeerId )
    {
        this.environmentId = environmentId;
        this.vni = vni;
        this.p2pSubnet = p2pSubnet;
        this.containerSubnet = containerSubnet;
        this.initiatorPeerId = initiatorPeerId;
    }


    @Override
    public String getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public long getVni()
    {
        return vni;
    }


    @Override
    public String getP2pSubnet()
    {
        return p2pSubnet;
    }


    @Override
    public String getContainerSubnet()
    {
        return containerSubnet;
    }


    @JsonIgnore
    @Override
    public int getVlan()
    {
        //vlan is calculated on target peer
        return -1;
    }


    public String getInitiatorPeerId()
    {
        return initiatorPeerId;
    }
}
