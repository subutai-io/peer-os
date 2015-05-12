package org.safehaus.subutai.core.env.api.exception;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class EnvironmentManagerExceptionTest
{
    private static final String MSG = "ERR";


    @Test
    public void testException() throws Exception
    {
        Exception cause = new Exception();

        EnvironmentManagerException exception = new EnvironmentManagerException( MSG, cause );

        assertEquals( cause, exception.getCause() );

        assertEquals( MSG, exception.getMessage() );
    }
}
