package io.subutai.core.metric.impl;


import org.junit.Test;

import io.subutai.common.command.RequestBuilder;

import static org.junit.Assert.assertEquals;


public class CommandsTest
{
    private static final String HOSTNAME = "host";
    private Commands commands = new Commands();


    @Test
    public void testGetCurrentMetricCommand() throws Exception
    {
        assertEquals( new RequestBuilder( String.format( "subutai stats system %s", HOSTNAME ) ),
                commands.getCurrentMetricCommand( HOSTNAME ) );
    }
}
