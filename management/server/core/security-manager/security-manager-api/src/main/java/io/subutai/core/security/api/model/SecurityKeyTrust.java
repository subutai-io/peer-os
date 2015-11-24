package io.subutai.core.security.api.model;


/**
 *
 */
public interface SecurityKeyTrust
{
    long getId();

    void setId( long id );

    String getSourceId();

    void setSourceId( String sourceId );

    String getTargetId();

    void setTargetId( String targetId );


    int getLevel();

    void setLevel( int level );
}
