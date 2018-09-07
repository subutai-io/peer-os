package io.subutai.bazaar.share.dto;


import java.util.Set;


public class UserDto
{
    private String id;

    private String name;

    private String fingerprint;

    private Boolean blocked;

    private Set<String> peers;

    private String email;

    private String publicKey;


    public UserDto()
    {
    }


    public UserDto( final String id, final String name, final String fingerprint, final Boolean blocked,
                    final Set<String> peers, final String email )
    {
        this.id = id;
        this.name = name;
        this.fingerprint = fingerprint;
        this.blocked = blocked;
        this.peers = peers;
        this.email = email;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getFingerprint()
    {
        return fingerprint;
    }


    public void setFingerprint( final String fingerprint )
    {
        this.fingerprint = fingerprint;
    }


    public Boolean getBlocked()
    {
        return blocked;
    }


    public void setBlocked( final Boolean blocked )
    {
        this.blocked = blocked;
    }


    public Set<String> getPeers()
    {
        return peers;
    }


    public void setPeers( final Set<String> peers )
    {
        this.peers = peers;
    }


    public String getEmail()
    {
        return email;
    }


    public void setEmail( String email )
    {
        this.email = email;
    }


    public String getPublicKey()
    {
        return publicKey;
    }


    public void setPublicKey( final String publicKey )
    {
        this.publicKey = publicKey;
    }
}
