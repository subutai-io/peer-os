package io.subutai.core.gateway.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.gateway.api.Gateway;


/**
 * Subutai Gateway Implementation
 */
public class GatewayImpl implements Gateway
{
    private final static String VERSION = "0.0.1";
    Logger LOGGER = LoggerFactory.getLogger( GatewayImpl.class );


    @Override
    public void login()
    {
        LOGGER.info( "Login invoked..." );
    }


    @Override
    public void logout()
    {
        LOGGER.info( "Logout invoked..." );
    }


    @Override
    public String getVersion()
    {
        return VERSION;
    }
}
