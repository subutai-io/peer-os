package io.subutai.core.environment.impl.exception;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.environment.impl.exception.EnvironmentBuildException;
import io.subutai.core.environment.impl.exception.EnvironmentTunnelException;
import io.subutai.core.environment.impl.exception.NodeGroupBuildException;

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
