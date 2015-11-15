package io.subutai.common.command;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class ResponseImplTest
{
    private static final ResponseType RESPONSE_TYPE = ResponseType.EXECUTE_RESPONSE;
    private static final String ID = UUID.randomUUID().toString();
    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final Integer PID = 123;
    private static final Integer RES_NO = 1;
    private static final String STD_OUT = "out";
    private static final String STD_ERR = "err";
    private static final Integer EXIT_CODE = 0;
    private static final String CONFIG_POINT = "/etc";

    @Mock
    Response response;

    ResponseImpl responseImpl;


    @Before
    public void setUp() throws Exception
    {
        Mockito.when( response.getType() ).thenReturn( RESPONSE_TYPE );
        Mockito.when( response.getId() ).thenReturn( ID );
        Mockito.when( response.getCommandId() ).thenReturn( COMMAND_ID );
        Mockito.when( response.getPid() ).thenReturn( PID );
        Mockito.when( response.getResponseNumber() ).thenReturn( RES_NO );
        Mockito.when( response.getStdOut() ).thenReturn( STD_OUT );
        Mockito.when( response.getStdErr() ).thenReturn( STD_ERR );
        Mockito.when( response.getExitCode() ).thenReturn( EXIT_CODE );
        Mockito.when( response.getConfigPoints() ).thenReturn( Sets.newHashSet( CONFIG_POINT ) );

        responseImpl = new ResponseImpl( response );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( RESPONSE_TYPE, responseImpl.getType() );

        assertEquals( ID, responseImpl.getId() );

        assertEquals( COMMAND_ID, responseImpl.getCommandId() );

        assertEquals( PID, responseImpl.getPid() );

        assertEquals( RES_NO, responseImpl.getResponseNumber() );

        assertEquals( STD_OUT, responseImpl.getStdOut() );

        assertEquals( STD_ERR, responseImpl.getStdErr() );

        assertEquals( EXIT_CODE, responseImpl.getExitCode() );

        assertEquals( Sets.newHashSet( CONFIG_POINT ), responseImpl.getConfigPoints() );
    }
}
