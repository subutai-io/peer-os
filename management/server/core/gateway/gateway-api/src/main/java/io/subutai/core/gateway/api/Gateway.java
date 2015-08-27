package io.subutai.core.gateway.api;


import java.io.Serializable;


/**
 * Created by tzhamakeev on 8/24/15.
 */
public interface Gateway
{

    Serializable login( String username, String password );

    void logout( Serializable token );

    String getVersion();
}
