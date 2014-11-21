package org.safehaus.subutai.core.command.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.command.OutputRedirection;
import org.safehaus.subutai.common.command.Request;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.RequestType;
import org.safehaus.subutai.common.util.UUIDUtil;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class RequestBuilderTest
{

    private static final UUID CONTAINER_ID = UUIDUtil.generateTimeBasedUUID();
    private static final String COMMAND = "cmd";
    private static final String CWD = "/";
    private static final String RUN_AS = "user";
    private static final Integer TIMEOUT = 10;
    private static final Integer PID = 1234;
    private static final RequestType REQUEST_TYPE = RequestType.EXECUTE_REQUEST;
    private static final List<String> CMD_ARGS = Lists.newArrayList( "arg1", "arg2" );
    private static final Map<String, String> ENV_VARS =
            Maps.asMap( Sets.newHashSet( "var1", "var2" ), new Function<String, String>()
            {
                @Override
                public String apply( final String s )
                {
                    if ( s.equals( "var1" ) )
                    {
                        return "val1";
                    }
                    else
                    {
                        return "val2";
                    }
                }
            } );

    private static final OutputRedirection STD_REDIRECTION = OutputRedirection.RETURN;
    private static final OutputRedirection ERR_REDIRECTION = OutputRedirection.RETURN;
    RequestBuilder requestBuilder =
            new RequestBuilder( COMMAND ).withCwd( CWD ).withRunAs( RUN_AS ).withTimeout( TIMEOUT ).withPid( PID )
                                         .withType( REQUEST_TYPE ).withCmdArgs( CMD_ARGS ).withEnvVars( ENV_VARS )

                                         .withStdErrRedirection( ERR_REDIRECTION )
                                         .withStdOutRedirection( STD_REDIRECTION );
    RequestBuilder requestBuilder2 =
            new RequestBuilder( COMMAND ).withCwd( CWD ).withRunAs( RUN_AS ).withTimeout( TIMEOUT ).withPid( PID )
                                         .withType( REQUEST_TYPE ).withCmdArgs( CMD_ARGS ).withEnvVars( ENV_VARS )

                                         .withStdErrRedirection( ERR_REDIRECTION )
                                         .withStdOutRedirection( STD_REDIRECTION );


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailEmptyOrNullCommand()
    {
        new RequestBuilder( null );
    }


    @Test
    public void shouldReturnSameProperties()
    {
        Request request = requestBuilder.build2( CONTAINER_ID );

        assertEquals( CONTAINER_ID, request.getId() );
        assertEquals( COMMAND, request.getCommand() );
        assertEquals( CWD, request.getWorkingDirectory() );
        assertEquals( RUN_AS, request.getRunAs() );
        assertEquals( TIMEOUT, requestBuilder.getTimeout() );
        assertEquals( TIMEOUT, request.getTimeout() );
        assertEquals( REQUEST_TYPE, request.getType() );
        assertEquals( CMD_ARGS, request.getArgs() );
        assertEquals( ENV_VARS, request.getEnvironment() );
        assertEquals( ERR_REDIRECTION, request.getStdErr() );
        assertEquals( STD_REDIRECTION, request.getStdOut() );
    }


    @Test
    public void testEquals()
    {

        assertEquals( requestBuilder, requestBuilder2 );
    }


    @Test
    public void testNotEquals()
    {
        RequestBuilder requestBuilder3 =
                new RequestBuilder( COMMAND ).withCwd( CWD ).withRunAs( RUN_AS ).withTimeout( TIMEOUT ).withPid( PID )
                                             .withType( REQUEST_TYPE ).withCmdArgs( CMD_ARGS ).withEnvVars( ENV_VARS )
                                             .withStdErrRedirection( STD_REDIRECTION )
                                             .withStdOutRedirection( OutputRedirection.NO );

        assertNotEquals( requestBuilder, requestBuilder3 );
    }


    @Test
    public void testHashcode()
    {


        Map<RequestBuilder, RequestBuilder> map = new HashMap();

        map.put( requestBuilder, requestBuilder );

        assertEquals( requestBuilder, requestBuilder2 );
    }
}
