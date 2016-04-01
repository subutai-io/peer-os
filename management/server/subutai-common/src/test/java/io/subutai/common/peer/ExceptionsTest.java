package io.subutai.common.peer;


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
    public void testContainerCreationException() throws Exception
    {
        ContainerCreationException exception = new ContainerCreationException( ERR_MSG );

        assertEquals( ERR_MSG, exception.getMessage() );
    }


    @Test
    public void testHostNotFoundException() throws Exception
    {
        HostNotFoundException exception = new HostNotFoundException( ERR_MSG );

        assertEquals( ERR_MSG, exception.getMessage() );
    }


    @Test
    public void testResourceHostException() throws Exception
    {
        ResourceHostException exception = new ResourceHostException( ERR_MSG );

        assertEquals( ERR_MSG, exception.getMessage() );

        exception = new ResourceHostException( cause );

        assertEquals( cause, exception.getCause() );

        exception = new ResourceHostException( ERR_MSG, cause );

        assertEquals( ERR_MSG, exception.getMessage() );
        assertEquals( cause, exception.getCause() );
    }
}
