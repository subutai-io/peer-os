package org.safehaus.subutai.core.env.impl.exception;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class ExceptionsTest
{
    private static final String ERR_MSG = "ERR";
    @Mock
    RuntimeException cause;


    @Test
    public void testEnvironmentBuildException() throws Exception
    {
        EnvironmentBuildException exception = new EnvironmentBuildException( ERR_MSG, cause );

        assertEquals( cause, exception.getCause() );
        assertEquals( ERR_MSG, exception.getMessage() );
    }


    @Test
    public void testEnvironmentTunnelException() throws Exception
    {
        EnvironmentTunnelException exception = new EnvironmentTunnelException( ERR_MSG, cause );

        assertEquals( cause, exception.getCause() );
        assertEquals( ERR_MSG, exception.getMessage() );
    }


    @Test
    public void testNodeGroupBuildException() throws Exception
    {
        NodeGroupBuildException exception = new NodeGroupBuildException( ERR_MSG, cause );

        assertEquals( cause, exception.getCause() );
        assertEquals( ERR_MSG, exception.getMessage() );
    }


}
