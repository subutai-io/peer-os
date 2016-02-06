package io.subutai.core.identity.api.model;


/**
 * Delegate for User
 */
public interface UserDelegate
{

    String getId();

    void setId( String id );

    long getUserId();

    void setUserId( long userId );

    int getType();

    void setType( int type );

}
