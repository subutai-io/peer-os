package io.subutai.hub.share.dto.domain;


import java.util.HashSet;
import java.util.Set;


public class ProxyDto
{
    private Long proxyId;
    private String p2pHash;
    private String p2SecretKey;
    private Long p2pSecretTTL;
    private String logs;
    private Set<P2PInfoDto> p2PInfoDtos = new HashSet();
    private Set<String> subnets = new HashSet<>();
    private P2PInfoDto.State state;
    private Set<PortMapDto> portMaps = new HashSet<>();


    public ProxyDto()
    {

    }


    public Long getProxyId()
    {
        return proxyId;
    }


    public void setProxyId( final Long proxyId )
    {
        this.proxyId = proxyId;
    }


    public Set<String> getSubnets()
    {
        return subnets;
    }


    public void setSubnets( final Set<String> subnets )
    {
        this.subnets = subnets;
    }


    public P2PInfoDto.State getState()
    {
        return state;
    }


    public void setState( final P2PInfoDto.State state )
    {
        this.state = state;
    }


    public Set<PortMapDto> getPortMaps()
    {
        return portMaps;
    }


    public void setPortMaps( final Set<PortMapDto> portMaps )
    {
        this.portMaps = portMaps;
    }


    public Set<P2PInfoDto> getP2PInfoDtos()
    {
        return p2PInfoDtos;
    }


    public void setP2PInfoDtos( final Set<P2PInfoDto> p2PInfoDtos )
    {
        this.p2PInfoDtos = p2PInfoDtos;
    }


    public String getLogs()
    {
        return logs;
    }


    public void setLogs( final String logs )
    {
        this.logs = logs;
    }


    public String getP2pHash()
    {
        return p2pHash;
    }


    public void setP2pHash( final String p2pHash )
    {
        this.p2pHash = p2pHash;
    }


    public String getP2SecretKey()
    {
        return p2SecretKey;
    }


    public void setP2SecretKey( final String p2SecretKey )
    {
        this.p2SecretKey = p2SecretKey;
    }


    public Long getP2pSecretTTL()
    {
        return p2pSecretTTL;
    }


    public void setP2pSecretTTL( final Long p2pSecretTTL )
    {
        this.p2pSecretTTL = p2pSecretTTL;
    }
}
