package io.subutai.core.kurjun.manager.api.model;


/**
 *
 */
public interface Kurjun
{
    String getOwnerFingerprint();

    void setOwnerFingerprint( String ownerFingerprint );

    String getAuthID();

    void setAuthID( String authID );

    byte[] getSignedMessage();

    void setSignedMessage( byte[] signedMessage );

    String getToken();

    void setToken( String token );

    long getId();

    void setId( long id );

    int getType();

    void setType( int type );
}
