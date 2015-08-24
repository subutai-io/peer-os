package io.subutai.core.security.rest;


import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.security.api.*;
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


    @Override
    public Response getPublicKey()
    {
        return Response.ok( securityManager.getKeyManager().getPublicKeyAsASCII( null ) ).build();
    }
}
