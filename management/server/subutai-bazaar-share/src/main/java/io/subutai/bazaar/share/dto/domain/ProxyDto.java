package io.subutai.bazaar.share.dto.domain;


import java.util.HashSet;
import java.util.Set;


public class ProxyDto
{
    public enum State
    {
        READY, FAILED, DESTROY, WAIT, CREATE, UPDATE
    }

    private Long proxyId;
    private String p2pHash;
    private String p2SecretKey;
    private Long p2pSecretTTL;
    private String p2pIfaceName;
    private String p2pIpAddr;
    private String logs;
    private State state;
    private Set<PortMapDto> portMaps = new HashSet<>();


    public ProxyDto()
    {

    }


    public ProxyDto( final Long proxyId, final String p2pHash, final String p2SecretKey, final Long p2pSecretTTL,
                     final String p2pIfaceName, final String p2pIpAddr, final State state )
    {
        this.proxyId = proxyId;
        this.p2pHash = p2pHash;
        this.p2SecretKey = p2SecretKey;
        this.p2pSecretTTL = p2pSecretTTL;
        this.p2pIfaceName = p2pIfaceName;
        this.p2pIpAddr = p2pIpAddr;
        this.state = state;
    }


    public Long getProxyId()
    {
        return proxyId;
    }


    public void setProxyId( final Long proxyId )
    {
        this.proxyId = proxyId;
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
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


    public String getP2pIfaceName()
    {
        return p2pIfaceName;
    }


    public void setP2pIfaceName( final String p2pIfaceName )
    {
        this.p2pIfaceName = p2pIfaceName;
    }


    public String getP2pIpAddr()
    {
        return p2pIpAddr;
    }


    public void setP2pIpAddr( final String p2pIpAddr )
    {
        this.p2pIpAddr = p2pIpAddr;
    }


    @Override
    public String toString()
    {
        return "ProxyDto{" + "proxyId=" + proxyId + ", p2pHash='" + p2pHash + '\'' + ", p2pIfaceName='" + p2pIfaceName
                + '\'' + ", p2pIpAddr='" + p2pIpAddr + '\'' + ", state=" + state + '}';
    }
}
