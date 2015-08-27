package io.subutai.core.gateway.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.gateway.api.Gateway;


public class RestServiceImpl implements RestService
{
    private static final Logger log = LoggerFactory.getLogger( RestServiceImpl.class );

    Gateway gateway;


    public RestServiceImpl( final Gateway gateway )
    {
        this.gateway = gateway;
    }


    @Override
    public String authenticate( UserCredentials userCredentials )
    {
        log.debug( String.format( "User credentials: %s:%s", userCredentials.getUsername(),
                userCredentials.getPassword() ) );
        Object o = gateway.login( userCredentials.getUsername(), userCredentials.getPassword() );

        return o != null?o.toString():"NULL";
    }
}