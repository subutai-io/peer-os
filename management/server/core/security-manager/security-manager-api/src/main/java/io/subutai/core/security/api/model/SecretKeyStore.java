package io.subutai.core.security.api.model;


/**
 * Interface for Secret Keyring store
 *
 * todo remove all mutators from interface
 */
public interface SecretKeyStore
{

    String getKeyFingerprint();


    void setKeyFingerprint( final String keyFingerprint );


    short getStatus();


    void setStatus( final short status );


    int getType();


    void setType( final int type );


    String getPwd();


    void setPwd( final String pwd );


    byte[] getData();


    void setData( final byte[] data );
}
