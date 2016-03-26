package io.subutai.core.kurjun.manager.api.model;


/**
 *
 */
public interface Kurjun
{
    public String getUrl();

    public void setUrl( final String url );

    public boolean getState();

    public void setState( final boolean state );

    long getId();

    void setId( long id );

    int getType();

    void setType( int type );

    public String getOwnerFingerprint();

    public void setOwnerFingerprint( final String ownerFingerprint );

    public String getAuthID();

    public void setAuthID( final String authID );

    public String getSignedMessage();

    public void setSignedMessage( final String signedMessage );

    public String getToken();

    public void setToken( final String token );
}
