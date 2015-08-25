package io.subutai.core.gateway.api;


/**
 * Created by tzhamakeev on 8/24/15.
 */
public interface Gateway
{
    void login();

    void logout();

    String getVersion();
}
