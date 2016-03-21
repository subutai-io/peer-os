package io.subutai.common.protocol;


public class P2PConnectionImpl implements P2PConnection
{
    private String mac;
    private String ip;
    private String hash;


    public P2PConnectionImpl( final String mac, final String ip, final String hash )
    {
        this.mac = mac;
        this.ip = ip;
        this.hash = hash;
    }


    public String getMac()
    {
        return mac;
    }


    public String getIp()
    {
        return ip;
    }


    public String getHash()
    {
        return hash;
    }
}
