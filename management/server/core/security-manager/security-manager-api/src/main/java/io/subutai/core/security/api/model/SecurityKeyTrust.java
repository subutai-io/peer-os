package io.subutai.core.security.api.model;


/**
 *
 */
public interface SecurityKeyTrust
{
    long getId();

    void setId( long id );

    int getLevel();

    void setLevel( int level );

    String getSourceFingerprint();

    void setSourceFingerprint( String sourceFingerprint );

    String getTargetFingerprint();

    void setTargetFingerprint( String targetFingerprint );

    SecurityKey getTargetKey();

    void setTargetKey( SecurityKey targetKey );
}
