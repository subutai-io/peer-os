package io.subutai.core.repository.cli;


import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.repository.api.PackageInfo;
import io.subutai.core.repository.api.RepositoryException;
import io.subutai.core.repository.api.RepositoryManager;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ListPackagesCommandTest extends SystemOutRedirectTest
{
    private static final String TO_STRING = "TO_STRING";
    @Mock
    RepositoryManager repositoryManager;

    ListPackagesCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ListPackagesCommand( repositoryManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new ListPackagesCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        PackageInfo packageInfo = mock( PackageInfo.class );
        when( packageInfo.toString() ).thenReturn( TO_STRING );
        when( repositoryManager.listPackages( anyString() ) ).thenReturn( Sets.newHashSet( packageInfo ) );

        command.doExecute();

        verify( repositoryManager ).listPackages( anyString() );
        assertEquals( getSysOut(), TO_STRING );


        RepositoryException exception = mock( RepositoryException.class );
        doThrow( exception ).when( repositoryManager ).listPackages( anyString() );

        command.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
