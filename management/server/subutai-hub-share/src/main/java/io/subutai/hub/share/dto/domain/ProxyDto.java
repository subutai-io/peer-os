package io.subutai.hub.share.dto.domain;


import java.util.HashSet;
import java.util.Set;


public class ProxyDto
{
    public enum State
    {
        SUCCESS, FAILED
    }

    private Long clusterId;
    private String p2pHash;
    private String p2SecretKey;
    private Long p2pSecretTTL;
    private String logs;
    private Set<P2PInfoDto> p2PInfoDtos = new HashSet();


    public ProxyDto()
    {

    }


    public Long getClusterId()
    {
        return clusterId;
    }


    public void setClusterId( final Long clusterId )
    {
        this.clusterId = clusterId;
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
