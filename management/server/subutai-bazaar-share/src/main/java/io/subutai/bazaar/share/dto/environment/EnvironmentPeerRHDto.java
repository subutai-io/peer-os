package io.subutai.bazaar.share.dto.environment;


public class EnvironmentPeerRHDto
{
    private String id;

    private String p2pIp;


    public EnvironmentPeerRHDto()
    {
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getP2pIp()
    {
        return p2pIp;
    }


    public void setP2pIp( final String p2pIp )
    {
        this.p2pIp = p2pIp;
    }
}
