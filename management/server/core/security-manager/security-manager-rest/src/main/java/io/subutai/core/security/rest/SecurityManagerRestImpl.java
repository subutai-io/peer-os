package io.subutai.core.security.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.security.api.SecurityManager;


/**
 * Implementation of Key Server Rest
 */
public class SecurityManagerRestImpl implements SecurityManagerRest
{

    private static final Logger LOG = LoggerFactory.getLogger( SecurityManagerRestImpl.class.getName() );

    // SecurityManager service
    private SecurityManager securityManager;


    /********************************
     *
     */
    public SecurityManagerRestImpl( SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }

}
