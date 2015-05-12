package org.safehaus.subutai.common.command;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class RequestBuilderTest
{
    private RequestBuilder requestBuilder;

    @Mock
    Object object;


    @Before
    public void setUp() throws Exception
    {
        requestBuilder = new RequestBuilder( "test" );
    }


    @Test
    public void testGetTimeout() throws Exception
    {
        assertNotNull( requestBuilder.getTimeout() );
    }


    @Test
    public void testWithCwd() throws Exception
    {
        assertNotNull( requestBuilder.withCwd( "test" ) );
    }


    @Test
    public void testWithType() throws Exception
    {
        assertNotNull( requestBuilder.withType( RequestType.EXECUTE_REQUEST ) );
    }


    @Test
    public void testWithStdOutRedirection() throws Exception
    {
        assertNotNull( requestBuilder.withStdOutRedirection( OutputRedirection.NO ) );
    }


    @Test
    public void testWithStdErrRedirection() throws Exception
    {
        assertNotNull( requestBuilder.withStdErrRedirection( OutputRedirection.RETURN ) );
    }


    @Test
    public void testWithTimeout() throws Exception
    {
        requestBuilder.withTimeout( 500 );
    }


    @Test
    public void testWithRunAs() throws Exception
    {
        assertNotNull( requestBuilder.withRunAs( "test" ) );
    }


    @Test
    public void testWithCmdArgs() throws Exception
    {
        List<String> myList = new ArrayList<>();
        myList.add( "test" );

        assertNotNull( requestBuilder.withCmdArgs( myList ) );
    }


    @Test
    public void testWithEnvVars() throws Exception
    {
        Map<String, String> myMap = new HashMap<>();
        myMap.put( "test", "test" );

        assertNotNull( requestBuilder.withEnvVars( myMap ) );
    }


    @Test
    public void testWithPid() throws Exception
    {
        assertNotNull( requestBuilder.withPid( 5 ) );
    }


    @Test
    public void testWithConfigPoints() throws Exception
    {
        Set<String> mySet = new HashSet<>();
        mySet.add( "test" );

        assertNotNull( requestBuilder.withConfigPoints( mySet ) );
    }


    @Test
    public void testDaemon() throws Exception
    {
        assertNotNull( requestBuilder.daemon() );
    }


    @Test
    public void testBuild() throws Exception
    {
        assertNotNull( requestBuilder.build( UUID.randomUUID() ) );
    }


    @Test
    public void testEquals() throws Exception
    {
        requestBuilder.equals( requestBuilder );
    }


    @Test
    public void testEquals2() throws Exception
    {
        requestBuilder.equals( object );
    }


    @Test
    public void testHashCode() throws Exception
    {
        requestBuilder.hashCode();
    }


    @Test
    public void testProperties()
    {
        requestBuilder.build(UUID.randomUUID()).getId();
        requestBuilder.build(UUID.randomUUID()).getCommandId();
        requestBuilder.build(UUID.randomUUID()).getType();
        requestBuilder.build(UUID.randomUUID()).getWorkingDirectory();
        requestBuilder.build(UUID.randomUUID()).getCommand();
        requestBuilder.build(UUID.randomUUID()).getArgs();
        requestBuilder.build(UUID.randomUUID()).getEnvironment();
        requestBuilder.build(UUID.randomUUID()).getStdOut();
        requestBuilder.build(UUID.randomUUID()).getStdErr();
        requestBuilder.build(UUID.randomUUID()).getRunAs();
        requestBuilder.build(UUID.randomUUID()).getTimeout();
        requestBuilder.build(UUID.randomUUID()).isDaemon();
        requestBuilder.build(UUID.randomUUID()).getConfigPoints();
        requestBuilder.build(UUID.randomUUID()).getPid();
    }
}