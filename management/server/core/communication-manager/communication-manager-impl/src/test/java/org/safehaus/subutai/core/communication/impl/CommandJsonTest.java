package org.safehaus.subutai.core.communication.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.communication.api.Command;
import org.safehaus.subutai.core.communication.api.CommandJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Test for CommandJson
 */
public class CommandJsonTest
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    @Test
    public void shouldReturnRequestCommandJson()
    {

        String actual = CommandJson.getRequestCommandJson( TestUtils.getRequestTemplate( UUID.randomUUID() ) );
        String expected = GSON.toJson( GSON.fromJson( actual, CommandJson.CommandImpl.class ) );


        assertEquals( expected, actual );
    }


    @Test
    public void shouldReturnResponseCommandJson()
    {
        Response response = new Response();
        String expected = GSON.toJson( new CommandJson.CommandImpl( response ) );
        String actual = CommandJson.getResponseCommandJson( response );


        assertEquals( expected, actual );
    }


    @Test
    public void shouldReturnCommandJson()
    {

        String actual = CommandJson.getCommandJson( new CommandJson.CommandImpl( new Response() ) );
        String expected = GSON.toJson( GSON.fromJson( actual, CommandJson.CommandImpl.class ) );


        assertEquals( expected, actual );
    }


    @Test
    public void shouldReturnCommandFromJson()
    {
        Request request = TestUtils.getRequestTemplate( UUID.randomUUID() );
        Command expected = new CommandJson.CommandImpl( request );
        Command actual = CommandJson.getCommandFromJson( GSON.toJson( expected ) );


        assertEquals( expected, actual );
    }


    @Test
    public void shouldReturnRequestFromCommandJson()
    {
        Request expected = TestUtils.getRequestTemplate( UUID.randomUUID() );
        String cmdJson = GSON.toJson( new CommandJson.CommandImpl( expected ) );
        Request actual = CommandJson.getRequestFromCommandJson( cmdJson );


        assertEquals( expected, actual );
    }


    @Test
    public void shouldReturnResponse()
    {
        Response expected = new Response();
        String cmdJson = GSON.toJson( new CommandJson.CommandImpl( expected ) );
        Response actual = CommandJson.getResponseFromCommandJson( cmdJson );


        assertEquals( expected, actual );
    }


    @Test
    public void shouldReturnNonNullResponse()
    {
        Response response = new Response();
        CommandJson.CommandImpl cmd = new CommandJson.CommandImpl( response );


        assertNotNull( cmd.getResponse() );
    }


    @Test
    public void shouldReturnNullResponse()
    {

        Response response = CommandJson.getResponseFromCommandJson( "" );


        assertNull( response );
    }


    @Test
    public void shouldReturnNullRequest()
    {

        Request request = CommandJson.getRequestFromCommandJson( "" );


        assertNull( request );
    }


    @Test
    public void shouldReturnNonNullRequest()
    {
        Request request = TestUtils.getRequestTemplate( UUID.randomUUID() );
        CommandJson.CommandImpl cmd = new CommandJson.CommandImpl( request );


        assertNotNull( cmd.getRequest() );
    }


    @Test
    public void checkEquals()
    {
        Request request = TestUtils.getRequestTemplate( UUID.randomUUID() );
        CommandJson.CommandImpl cmd = new CommandJson.CommandImpl( request );
        String cmdJson = CommandJson.getRequestCommandJson( request );
        Command cmd2 = CommandJson.getCommandFromJson( cmdJson );


        assertEquals( cmd, cmd2 );
    }


    @Test
    public void checkHashCode()
    {
        Request request = TestUtils.getRequestTemplate( UUID.randomUUID() );
        CommandJson.CommandImpl cmd = new CommandJson.CommandImpl( request );
        String cmdJson = CommandJson.getRequestCommandJson( request );
        Command cmd2 = CommandJson.getCommandFromJson( cmdJson );
        Map<Command, Command> map = new HashMap<>();
        map.put( cmd, cmd );


        assertEquals( cmd, map.get( cmd2 ) );
    }


    @Test
    public void shouldReturnNullOnMalformedJson()
    {
        Request request = TestUtils.getRequestTemplate( UUID.randomUUID() );
        String cmdJson = CommandJson.getRequestCommandJson( request ) + "invalid";


        assertNull( CommandJson.getCommandFromJson( cmdJson ) );
    }


    @Test
    public void shouldEscapeString()
    {
        String str = "a\"\\\b\f\n\r\t/" + '\u007F' + '\u001F' + '\u2000';


        assertEquals( "a\"\\\b\f\n\r\t\\/" + "\\u007F" + "\\u001F" + "\\u2000", CommandJson.escape( str ) );
    }
}
