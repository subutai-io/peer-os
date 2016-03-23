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

    String getSignedMessage();

    void setSignedMessage( String signedMessage );

    String getToken();

    void setToken( String token );
}
