package org.safehaus.subutai.core.repository.api;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class RepositoryExceptionTest
{

    private static final String MESSAGE = "message";
    private static final Exception CAUSE = new Exception();


    @Test
    public void testMessage() throws Exception
    {
        RepositoryException exception = new RepositoryException( MESSAGE );

        assertEquals( MESSAGE, exception.getMessage() );
    }


    @Test
    public void testCause() throws Exception
    {
        RepositoryException exception = new RepositoryException( CAUSE );

        assertEquals( CAUSE, exception.getCause() );
    }


    @Test
    public void testMessageNCause() throws Exception
    {
        RepositoryException exception = new RepositoryException( MESSAGE, CAUSE );

        assertEquals( CAUSE, exception.getCause() );
        assertEquals( MESSAGE, exception.getMessage() );

    }
}
