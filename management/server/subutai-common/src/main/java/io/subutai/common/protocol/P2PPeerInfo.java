package io.subutai.common.protocol;


public class P2PPeerInfo
{
    private String p2pPeerId;
    private String p2pPeerIP;
    private String p2pPeerEndpoint;


    public P2PPeerInfo( final String p2pPeerId, final String p2pPeerIP, final String p2pPeerEndpoint )
    {
        this.p2pPeerId = p2pPeerId;
        this.p2pPeerIP = p2pPeerIP;
        this.p2pPeerEndpoint = p2pPeerEndpoint;
    }


    public String getP2pPeerId()
    {
        return p2pPeerId;
    }


    public String getP2pPeerIP()
    {
        return p2pPeerIP;
    }


    public String getP2pPeerEndpoint()
    {
        return p2pPeerEndpoint;
    }
}
