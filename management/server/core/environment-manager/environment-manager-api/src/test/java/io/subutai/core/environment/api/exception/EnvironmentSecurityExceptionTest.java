package io.subutai.core.environment.api.exception;


import org.junit.Test;

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
