package io.subutai.common.peer;


import java.util.UUID;


/**
 * Created by tzhamakeev on 10/19/15.
 */
public class RegistrationRequest
{
    private PeerInfo peerInfo;
    private String keyPhrase;
    private String cert;
    private RegistrationStatus status;


    public RegistrationRequest()
    {
    }


    public RegistrationRequest( final PeerInfo peerInfo, final String keyPhrase, final RegistrationStatus status )
    {
        this.peerInfo = peerInfo;
        this.keyPhrase = keyPhrase;
        this.status = status;
    }


    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    public void setPeerInfo( final PeerInfo peerInfo )
    {
        this.peerInfo = peerInfo;
    }


    public String getKeyPhrase()
    {
        return keyPhrase;
    }


    public void setKeyPhrase( final String keyPhrase )
    {
        this.keyPhrase = keyPhrase;
    }


    public void setCert( final String cert )
    {
        this.cert = cert;
    }


    public String getCert()
    {
        return cert;
    }


    public RegistrationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final RegistrationStatus status )
    {
        this.status = status;
    }
}
