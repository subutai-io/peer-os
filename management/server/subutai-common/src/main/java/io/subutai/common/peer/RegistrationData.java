package io.subutai.common.peer;


/**
 * Registration DTO
 */
public class RegistrationData
{
    private PeerInfo peerInfo;
    private String keyPhrase;
    private RegistrationStatus status;
    private Encrypted sslCert;
    private Encrypted publicKey;
    private String token;


    public RegistrationData()
    {
    }


    public RegistrationData( final PeerInfo peerInfo, final String keyPhrase, final RegistrationStatus status )
    {
        this.peerInfo = peerInfo;
        this.keyPhrase = keyPhrase;
        this.status = status;
    }


    public RegistrationData( final PeerInfo peerInfo, final RegistrationStatus status )
    {
        this.peerInfo = peerInfo;
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


    public RegistrationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final RegistrationStatus status )
    {
        this.status = status;
    }


    public Encrypted getSslCert()
    {
        return sslCert;
    }


    public void setSslCert( final Encrypted sslCert )
    {
        this.sslCert = sslCert;
    }


    public Encrypted getPublicKey()
    {
        return publicKey;
    }


    public void setPublicKey( final Encrypted publicKey )
    {
        this.publicKey = publicKey;
    }


    public void setKeyPhrase( final String keyPhrase )
    {
        this.keyPhrase = keyPhrase;
    }


    public String getToken()
    {
        return token;
    }


    public void setToken( String token )
    {
        this.token = token;
    }
}
