package io.subutai.core.env.api.exception;


import org.junit.Test;

import io.subutai.core.env.api.exception.EnvironmentCreationException;

import static org.junit.Assert.assertEquals;


public class EnvironmentCreationExceptionTest
{

    private static final String MSG = "ERR";


    @Test
    public void testException() throws Exception
    {
        Exception cause = new Exception();

        EnvironmentCreationException exception = new EnvironmentCreationException( cause );

        assertEquals( cause, exception.getCause() );

        EnvironmentCreationException exception2 = new EnvironmentCreationException( MSG );

        assertEquals( MSG, exception2.getMessage() );
    }
}
