package io.subutai.core.identity.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.identity.api.SecurityController;


/**
 * Logs and Checks security events
 */
class SecurityControllerImpl implements SecurityController
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SecurityControllerImpl.class.getName() );

    private String globalkeyServer = "";
    private String localkeyServer = "";


    @Override
    public void logEvent( String userName, String action )
    {
        LOGGER.info( " *** Security event *** UserName:" + userName + ",  Action:" + action);
    }


    @Override
    public void logEvent( String userName,String password, String action )
    {
        LOGGER.info( " *** Security event *** UserName:" + userName + ", Password:" + password + ", Action:" + action);
    }


    @Override
    public int checkTrustLevel( String fingerprint )
    {
        //todo implement
        return 0;
    }


    @Override
    public String getGlobalkeyServer()
    {
        return globalkeyServer;
    }


    @Override
    public void setGlobalkeyServer( final String globalkeyServer )
    {
        this.globalkeyServer = globalkeyServer;
    }


    @Override
    public String getLocalkeyServer()
    {
        return localkeyServer;
    }


    @Override
    public void setLocalkeyServer( final String localkeyServer )
    {
        this.localkeyServer = localkeyServer;
    }
}
