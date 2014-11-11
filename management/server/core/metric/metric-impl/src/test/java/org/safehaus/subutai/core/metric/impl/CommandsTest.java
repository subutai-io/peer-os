package org.safehaus.subutai.core.metric.impl;


import org.junit.Test;
import org.safehaus.subutai.common.command.RequestBuilder;

import static org.junit.Assert.assertEquals;


public class CommandsTest
{
    private Commands commands = new Commands();


    @Test
    public void testGetReadResourceHostMetricCommand() throws Exception
    {
        assertEquals( new RequestBuilder( "subutai monitor -p" ), commands.getReadResourceHostMetricCommand() );
    }


    @Test
    public void testGetReadContainerHostMetricCommand() throws Exception
    {
        String hostname = "host";
        assertEquals( new RequestBuilder( String.format( "subutai monitor %s", hostname ) ),
                commands.getReadContainerHostMetricCommand( hostname ) );
    }
}
