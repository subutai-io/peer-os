package io.subutai.core.executor.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import io.subutai.common.command.ResponseType;
import io.subutai.common.util.JsonUtil;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


public class ResponseImplTest
{
    private static final UUID ID = UUID.randomUUID();
    private static final String RESPONSE_JSON = String.format(
            "  {" + "      \"type\":\"EXECUTE_RESPONSE\"," + "      \"id\":\"%s\"," + "      \"commandId\":\"%s\","
                    + "      \"pid\":123," + "      \"responseNumber\":2," + "      \"stdOut\":\"output\","
                    + "      \"stdErr\":\"err\"," + "      \"exitCode\" : 0" + "  }", ID.toString(), ID.toString() );


    ResponseImpl response;


    @Before
    public void setUp() throws Exception
    {
        response = JsonUtil.fromJson( RESPONSE_JSON, ResponseImpl.class );
    }


    @Test
    public void testProperties() throws Exception
    {

        assertEquals( ResponseType.EXECUTE_RESPONSE, response.getType() );
        assertEquals( ID, response.getId() );
        assertEquals( ID, response.getCommandId() );
        assertEquals( 123, ( int ) response.getPid() );
        assertEquals( 2, ( int ) response.getResponseNumber() );
        assertEquals( "output", response.getStdOut() );
        assertEquals( "err", response.getStdErr() );
        assertEquals( 0, ( int ) response.getExitCode() );
        assertNull( response.getConfigPoints() );
    }


    @Test
    public void testToString() throws Exception
    {
        String toString = response.toString();

        assertThat( toString, containsString( "EXECUTE_RESPONSE" ) );
    }
}
