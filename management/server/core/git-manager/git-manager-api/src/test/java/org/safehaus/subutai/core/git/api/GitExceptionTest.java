package org.safehaus.subutai.core.git.api;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class GitExceptionTest
{

    private static final String MSG = "ERR";


    @Test
    public void testException() throws Exception
    {
        Exception cause = new Exception();

        GitException exception = new GitException( cause );

        assertEquals( cause, exception.getCause() );

        GitException exception2 = new GitException( MSG );

        assertEquals( MSG, exception2.getMessage() );
    }
}
