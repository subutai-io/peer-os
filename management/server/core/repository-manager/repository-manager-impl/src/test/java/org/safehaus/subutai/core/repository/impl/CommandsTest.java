package org.safehaus.subutai.core.repository.impl;


import java.util.Set;

import org.junit.Test;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.StringUtil;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;


public class CommandsTest
{
    private static final String ARGUMENT = "argument";
    private static final Set<String> FILES = Sets.newHashSet( ARGUMENT );
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
    public void testGetExtractFilesCommand() throws Exception
    {

        assertEquals( new RequestBuilder( String.format( "subutai package_manager extract %s -f %s", ARGUMENT,
                        StringUtil.joinStrings( FILES, ',', false ) ) ),
                commands.getExtractFilesCommand( ARGUMENT, FILES ) );
    }


    @Test
    public void testGetListPackagesCommand() throws Exception
    {

        assertEquals( new RequestBuilder( String.format( "subutai package_manager list %s", ARGUMENT ) ),
                commands.getListPackagesCommand( ARGUMENT ) );
    }


    @Test
    public void testGetPackageInfoCommand() throws Exception
    {

        assertEquals( new RequestBuilder( String.format( "subutai package_manager info %s", ARGUMENT ) ),
                commands.getPackageInfoCommand( ARGUMENT ) );
    }
}
