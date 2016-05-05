package io.subutai.core.identity.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.identity.api.SecurityController;


/**
 * Logs and Checks security events
 */
public class SecurityControllerImpl implements SecurityController
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SecurityControllerImpl.class.getName() );


    
    @Override
    public void logEvent(String userName , String action)
    {
        LOGGER.info( " *** Security event *** UserName:" + userName + ",  Action:" + action);
    }

}
