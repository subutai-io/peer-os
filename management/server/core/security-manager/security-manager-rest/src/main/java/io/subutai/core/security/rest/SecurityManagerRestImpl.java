package io.subutai.core.security.rest;


import java.util.Map;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import io.subutai.common.util.JsonUtil;
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
        Map<String, String> result = Maps.newHashMap();
        result.put( "Key", securityManager.getKeyManager().getPublicKeyAsASCII( null ) );
        return Response.ok( JsonUtil.toJson( result ) ).build();
    }
}
