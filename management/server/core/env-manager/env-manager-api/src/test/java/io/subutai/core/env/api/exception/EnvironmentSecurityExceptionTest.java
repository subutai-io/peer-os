package io.subutai.core.env.api.exception;


import org.junit.Test;

import io.subutai.core.env.api.exception.EnvironmentSecurityException;

import static org.junit.Assert.assertEquals;


public class EnvironmentSecurityExceptionTest
{
    private static final String MSG = "ERR";


    @Test
    public void testException() throws Exception
    {

        EnvironmentSecurityException exception = new EnvironmentSecurityException( MSG );

        assertEquals( MSG, exception.getMessage() );
    }
}
