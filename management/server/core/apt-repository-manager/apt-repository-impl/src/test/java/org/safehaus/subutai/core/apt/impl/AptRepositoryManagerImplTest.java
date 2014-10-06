package org.safehaus.subutai.core.apt.impl;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.PackageInfo;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;


/**
 * Test for AptRepositoryManagerImpl
 */
@SuppressWarnings( "ResultOfMethodCallIgnored" )
public class AptRepositoryManagerImplTest
{

    private static final String PACKAGES = "i   ksks-mgmt                     - This is a Subutai package " +
            "distribution.\n" + "i   ksks-nginx                    - This is an nginx package of kiskis dist\n";
    private static final String FILE_CONTENT = "some dummy content";
    private static final String FILE_NAME = "test";
    private static final String PATTERN = "pattern";
    private static final String STATUS = "status";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";


    @Test
    public void shouldListPackages() throws AptRepoException
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), PACKAGES, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );
        AptRepositoryManagerImpl aptRepositoryManager = new AptRepositoryManagerImpl( commandRunner );


        List<PackageInfo> packages = aptRepositoryManager.listPackages( agent, PATTERN );

        assertTrue(
                packages.contains( new PackageInfo( "i", "ksks-mgmt", "- This is a Subutai package distribution." ) ) );
        assertTrue( packages.contains(
                new PackageInfo( "i", "ksks-nginx", "- This is an nginx package of kiskis dist" ) ) );
    }


    @Test
    public void shouldAddPackageNBroadcast() throws AptRepoException, IOException
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), "added package", null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );
        AptRepositoryManagerImpl aptRepositoryManager = new AptRepositoryManagerImpl( commandRunner );

        File file = new File( FILE_NAME );
        file.createNewFile();


        aptRepositoryManager.addPackageByPath( agent, FILE_NAME, false );

        verify( commandRunner ).runCommand( command );
        verify( commandRunner ).runCommandAsync( command );

        file.delete();
    }


    @Test
    public void shouldRemovePackageNBroadcast() throws AptRepoException, IOException
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), null, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );
        AptRepositoryManagerImpl aptRepositoryManager = new AptRepositoryManagerImpl( commandRunner );


        aptRepositoryManager.removePackageByName( agent, "package" );

        verify( commandRunner ).runCommand( command );
        verify( commandRunner ).runCommandAsync( command );
    }


    @Test
    public void shouldReadFileContents() throws AptRepoException, IOException
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), FILE_CONTENT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );
        AptRepositoryManagerImpl aptRepositoryManager = new AptRepositoryManagerImpl( commandRunner );

        File file = new File( FILE_NAME );
        file.createNewFile();

        List<String> fileContents =
                aptRepositoryManager.readFileContents( agent, FILE_NAME, Lists.newArrayList( "path" ) );

        verify( commandRunner ).runCommand( command );
        verify( commandRunner ).runCommandAsync( any( Command.class ) );
        assertTrue( fileContents.contains( FILE_CONTENT ) );

        file.delete();
    }


    @Test( expected = AptRepoException.class )
    public void shouldThrowAptException() throws AptRepoException
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        Command command = MockUtils.getCommand( true, false, agent.getUuid(), FILE_CONTENT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );
        AptRepositoryManagerImpl aptRepositoryManager = new AptRepositoryManagerImpl( commandRunner );

        aptRepositoryManager.listPackages( agent, PATTERN );
    }


    @Test()
    public void shouldReturnSameProperties()
    {
        PackageInfo packageInfo = new PackageInfo( STATUS, NAME, DESCRIPTION );

        assertEquals( STATUS, packageInfo.getStatus() );
        assertEquals( NAME, packageInfo.getName() );
        assertEquals( DESCRIPTION, packageInfo.getDescription() );
    }


    @Test()
    public void shouldBeEqual()
    {
        PackageInfo packageInfo = new PackageInfo( STATUS, NAME, DESCRIPTION );
        PackageInfo packageInfo2 = new PackageInfo( STATUS, NAME, DESCRIPTION );

        assertEquals( packageInfo, packageInfo2 );
    }


    @Test()
    public void shouldReturnCustomToString()
    {
        PackageInfo packageInfo = new PackageInfo( STATUS, NAME, DESCRIPTION );

        assertThat( packageInfo.toString(), containsString( STATUS ) );
        assertThat( packageInfo.toString(), containsString( NAME ) );
        assertThat( packageInfo.toString(), containsString( DESCRIPTION ) );
    }


    @Test()
    public void checkHashCode()
    {
        PackageInfo packageInfo = new PackageInfo( STATUS, NAME, DESCRIPTION );

        Map<PackageInfo, PackageInfo> map = Maps.newHashMap();

        map.put( packageInfo, packageInfo );

        assertEquals( packageInfo, map.get( packageInfo ) );
    }
}
