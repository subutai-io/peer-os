package io.subutai.core.env.api.exception;


import org.junit.Test;

import io.subutai.core.env.api.exception.EnvironmentDestructionException;

import static org.junit.Assert.assertEquals;


public class EnvironmentDestructionExceptionTest
{
    private static final String MSG = "ERR";


    @Test
    public void testException() throws Exception
    {
        Exception cause = new Exception();

        EnvironmentDestructionException exception = new EnvironmentDestructionException( cause );

        assertEquals( cause, exception.getCause() );

        EnvironmentDestructionException exception2 = new EnvironmentDestructionException( MSG );

        assertEquals( MSG, exception2.getMessage() );
    }
}
