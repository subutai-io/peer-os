package io.subutai.common.peer;


public class ApproveRequest
{
    PeerInfo peerInfo;
    String cert;


    public ApproveRequest()
    {
    }


    public ApproveRequest( final PeerInfo peerInfo, final String cert )
    {
        this.peerInfo = peerInfo;
        this.cert = cert;
    }


    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    public void setPeerInfo( final PeerInfo peerInfo )
    {
        this.peerInfo = peerInfo;
    }


    public String getCert()
    {
        return cert;
    }


    public void setCert( final String cert )
    {
        this.cert = cert;
    }
}
