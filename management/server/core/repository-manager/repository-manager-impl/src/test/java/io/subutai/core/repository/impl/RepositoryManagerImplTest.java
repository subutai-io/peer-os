package io.subutai.core.repository.impl;


import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.repository.api.PackageInfo;
import io.subutai.core.repository.api.RepositoryException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RepositoryManagerImplTest
{
    private static final String ARGUMENT = "argument";
    private static final String SHORT_NAME = "hadoop-subutai-template";
    private static final String FULL_NAME = "hadoop-subutai-template_2.1.0_amd64.deb";
    private static final String LIST_OUTPUT = "subutai-repo-hbase - Subutai Repository Package";
    private static final String PACKAGE_INFO =
            "Package: hadoop-subutai-template\n" + "Maintainer: subutai\n" + "Architecture: amd64\n"
                    + "Version: 2.1.0\n" + "Depends: subutai-cli (>= 2.1.0), master-subutai-template (= 2.1.0)\n"
                    + "Priority: optional\n" + "Section: devel\n"
                    + "Filename: pool/main/h/hadoop-subutai-template/hadoop-subutai-template_2.1.0_amd64.deb\n"
                    + "Size: 348573636\n" + "SHA256: 79a1c342c4bf99b588e6df3ce4cdccf889de5d5249024c69af01845d7814ef1f\n"
                    + "SHA1: c005a1d82d6a46af97612c6d3c2e2e59524a2865\n" + "MD5sum: 3fee4a7c3530f3c0d54964c8ca8a4a96\n"
                    + "Description: This is a Subutai delta image debian package of hadoop template\n"
                    + "Description-md5: d7a727affd4f3f44c48851e84dc4dabe\n";
    private static final Set<String> FILES = Sets.newHashSet( ARGUMENT );

    @Mock
    PeerManager peerManager;

    @Mock
    LocalPeer localPeer;

    @Mock
    ManagementHost managementHost;

    @Mock
    CommandResult result;

    @Mock
    Commands commands;
    @Mock
    CommandUtil commandUtil;

    @Mock
    RequestBuilder requestBuilder;

    RepositoryManagerImpl repositoryManager;


    @Before
    public void setUp() throws Exception
    {
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( managementHost.execute( any( RequestBuilder.class ) ) ).thenReturn( result );
        when( commandUtil.execute( any( RequestBuilder.class ), any( Host.class ) ) ).thenReturn( result );
        repositoryManager = new RepositoryManagerImpl( peerManager );
        repositoryManager.commands = commands;
        repositoryManager.commandUtil = commandUtil;
        when( result.hasSucceeded() ).thenReturn( true, true );
        when( result.hasCompleted() ).thenReturn( true, false );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new RepositoryManagerImpl( null );

            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testExecuteCommand() throws Exception
    {
        when( commandUtil.execute( any( RequestBuilder.class ), any( Host.class ) ) ).thenReturn( result ).thenThrow(
                new CommandException( "" ) );

        CommandResult commandResult = repositoryManager.executeCommand( requestBuilder );

        assertEquals( result, commandResult );

        try
        {
            repositoryManager.executeCommand( requestBuilder );

            fail( "Expected RepositoryException" );
        }
        catch ( RepositoryException e )
        {
        }
    }


    @Test( expected = RepositoryException.class )
    public void testAddPackageByPath() throws Exception
    {

        repositoryManager.addPackageByPath( ARGUMENT );

        verify( commands ).getAddPackageCommand( ARGUMENT );

        repositoryManager.addPackageByPath( ARGUMENT );
    }


    @Test( expected = RepositoryException.class )
    public void testRemovePackageByName() throws Exception
    {

        repositoryManager.removePackageByName( ARGUMENT );

        verify( commands ).getRemovePackageCommand( ARGUMENT );

        repositoryManager.removePackageByName( ARGUMENT );
    }


    @Test
    public void testExtractPackageByName() throws Exception
    {

        repositoryManager.extractPackageByName( ARGUMENT );

        verify( commands ).getExtractPackageCommand( ARGUMENT );
    }


    @Test
    public void testExtractFiles() throws Exception
    {

        repositoryManager.extractPackageFiles( ARGUMENT, FILES );

        verify( commands ).getExtractFilesCommand( ARGUMENT, FILES );
    }


    @Test
    public void testListPackages() throws Exception
    {
        when( result.getStdOut() ).thenReturn( LIST_OUTPUT );

        Set<PackageInfo> packageList = repositoryManager.listPackages( ARGUMENT );

        assertFalse( packageList.isEmpty() );
    }


    @Test
    public void testGetPackageInfo() throws Exception
    {

        when( result.getStdOut() ).thenReturn( PACKAGE_INFO );

        String packageInfo = repositoryManager.getPackageInfo( ARGUMENT );

        assertEquals( PACKAGE_INFO, packageInfo );
    }


    @Test( expected = RepositoryException.class )
    public void testGetFullPackageName() throws Exception
    {

        when( result.getStdOut() ).thenReturn( PACKAGE_INFO );

        String fullPackageName = repositoryManager.getFullPackageName( SHORT_NAME );

        assertEquals( FULL_NAME, fullPackageName );

        when( result.getStdOut() ).thenReturn( "" );

        repositoryManager.getFullPackageName( ARGUMENT );
    }


    @Test( expected = RepositoryException.class )
    public void testExecuteUpdateRepoCommand() throws Exception
    {
        doThrow( new CommandException( "" ) ).when( managementHost ).execute( any( RequestBuilder.class ) );

        repositoryManager.executeUpdateRepoCommand();
    }


    @Test( expected = RepositoryException.class )
    public void testAddAptSource() throws Exception
    {
        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), eq( managementHost ) );

        repositoryManager.addRepository( ARGUMENT );
    }


    @Test( expected = RepositoryException.class )
    public void testRemoveAptSource() throws Exception
    {
        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), eq( managementHost ) );

        repositoryManager.removeRepository( ARGUMENT );
    }
}
