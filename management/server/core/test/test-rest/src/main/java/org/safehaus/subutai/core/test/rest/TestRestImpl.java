package org.safehaus.subutai.core.test.rest;


import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.test.api.Test;


public class TestRestImpl implements TestRest
{
    private final Test test;


    public TestRestImpl( final Test test )
    {
        this.test = test;
    }


    @Override
    public Response auth( final String username )
    {
        test.loginWithToken( username );
        return Response.ok( test.getUserName() ).build();
    }
}
