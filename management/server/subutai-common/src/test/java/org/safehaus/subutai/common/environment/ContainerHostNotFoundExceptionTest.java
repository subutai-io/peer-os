package org.safehaus.subutai.common.environment;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostNotFoundExceptionTest
{
    private ContainerHostNotFoundException containerHostNotFoundException;
    private EnvironmentModificationException modificationException;
    private EnvironmentNotFoundException environmentNotFoundException;

    @Before
    public void setUp() throws Exception
    {
        containerHostNotFoundException = new ContainerHostNotFoundException( "test" );
        modificationException = new EnvironmentModificationException( new Throwable(  ) );
        modificationException = new EnvironmentModificationException( "test" );
        environmentNotFoundException = new EnvironmentNotFoundException( "test" );
        environmentNotFoundException = new EnvironmentNotFoundException(  );
    }


    @Test
    public void test()
    {

    }
}