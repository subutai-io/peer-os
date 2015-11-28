package io.subutai.core.security.api.model;


/**
 *
 */
public interface SecurityKeyTrust
{
    long getId();

    void setId( long id );

    String getSourceFingerprint();

    void setSourceFingerprint( String sourceFingerprint );

    String getTargetFingerprint();

    void setTargetFingerprint( String targetFingerprint );

    int getLevel();

    void setLevel( int level );
}
