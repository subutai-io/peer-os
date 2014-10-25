package org.safehaus.subutai.core.dispatcher.impl;


import org.junit.Test;
import org.safehaus.subutai.common.exception.RunCommandException;


/**
 * Test for RunCommandException
 */
public class RunCommandExceptionTest
{
    @Test( expected = RuntimeException.class )
    public void shouldCreateNThrowIt()
    {
        RunCommandException runCommandException = new RunCommandException( "OOPS" );

        throw runCommandException;
    }
}
