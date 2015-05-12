package org.safehaus.subutai.common.exception;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostArchitecture;


@RunWith( MockitoJUnitRunner.class )
public class HTTPExceptionTest
{
    private HTTPException httpException;
    private DaoException daoException;

    @Before
    public void setUp() throws Exception
    {
        httpException = new HTTPException( "exception" );
        daoException = new DaoException( "exception" );
        daoException = new DaoException( new Throwable(  ) );
    }


    @Test
    public void test()
    {
        ContainerHostState running = ContainerHostState.RUNNING;
        HostArchitecture amd64 = HostArchitecture.AMD64;
    }
}