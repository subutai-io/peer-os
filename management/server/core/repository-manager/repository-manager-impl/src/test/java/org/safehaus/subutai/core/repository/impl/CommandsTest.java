package org.safehaus.subutai.core.repository.impl;


import org.junit.Test;
import org.safehaus.subutai.common.protocol.RequestBuilder;

import static org.junit.Assert.assertEquals;


public class CommandsTest
{
    private static final String ARGUMENT = "argument";
    Commands commands = new Commands();


    @Test
    public void testGetAddPackageCommand() throws Exception
    {

        assertEquals( new RequestBuilder( String.format( "subutai package_manager add %s", ARGUMENT ) ),
                commands.getAddPackageCommand( ARGUMENT ) );
    }


    @Test
    public void testGetRemovePackageCommand() throws Exception
    {

        assertEquals( new RequestBuilder( String.format( "subutai package_manager remove %s", ARGUMENT ) ),
                commands.getRemovePackageCommand( ARGUMENT ) );
    }


    @Test
    public void testGetExtractPackageCommand() throws Exception
    {

        assertEquals( new RequestBuilder( String.format( "subutai package_manager extract %s", ARGUMENT ) ),
                commands.getExtractPackageCommand( ARGUMENT ) );
    }


    @Test
    public void testGetListPackagesCommand() throws Exception
    {

        assertEquals( new RequestBuilder( String.format( "subutai package_manager list %s", ARGUMENT ) ),
                commands.getListPackagesCommand( ARGUMENT ) );
    }
}
