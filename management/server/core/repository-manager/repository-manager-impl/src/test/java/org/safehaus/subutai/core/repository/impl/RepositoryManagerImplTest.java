package org.safehaus.subutai.core.repository.impl;


import java.io.PrintStream;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.repository.api.PackageInfo;
import org.safehaus.subutai.core.repository.api.RepositoryException;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RepositoryManagerImplTest
{
    private static final String ARGUMENT = "argument";
    private static final String LIST_OUTPUT = "subutai-repo-hbase - Subutai Repository Package";
    private static final String PACKAGE_INFO = "Some package info";
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
    RequestBuilder requestBuilder;

    RepositoryManagerImpl repositoryManager;


    @Before
    public void setUp() throws Exception
    {
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( managementHost.execute( any( RequestBuilder.class ) ) ).thenReturn( result );
        repositoryManager = new RepositoryManagerImpl( peerManager );
        repositoryManager.commands = commands;
        when( result.hasSucceeded() ).thenReturn( true );
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
        when( result.hasSucceeded() ).thenReturn( true ).thenReturn( false );

        CommandResult commandResult = repositoryManager.executeCommand( requestBuilder );

        assertEquals( result, commandResult );


        when( result.hasCompleted() ).thenReturn( true ).thenReturn( false );

        try
        {
            repositoryManager.executeCommand( requestBuilder );

            fail( "Expected RepositoryException" );
        }
        catch ( RepositoryException e )
        {
        }
        try
        {
            repositoryManager.executeCommand( requestBuilder );

            fail( "Expected RepositoryException" );
        }
        catch ( RepositoryException e )
        {
        }


        CommandException exception = mock( CommandException.class );
        doThrow( exception ).when( managementHost ).execute( any( RequestBuilder.class ) );

        try
        {
            repositoryManager.executeCommand( requestBuilder );

            fail( "Expected RepositoryException" );
        }
        catch ( RepositoryException e )
        {
        }
    }


    @Test
    public void testAddPackageByPath() throws Exception
    {

        repositoryManager.addPackageByPath( ARGUMENT );

        verify( commands ).getAddPackageCommand( ARGUMENT );
    }


    @Test
    public void testRemovePackageByName() throws Exception
    {

        repositoryManager.removePackageByName( ARGUMENT );

        verify( commands ).getRemovePackageCommand( ARGUMENT );
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
}
