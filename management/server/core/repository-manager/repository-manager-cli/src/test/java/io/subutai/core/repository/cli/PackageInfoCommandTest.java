package io.subutai.core.repository.cli;


import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.repository.api.RepositoryException;
import io.subutai.core.repository.api.RepositoryManager;
import io.subutai.core.repository.cli.ListPackagesCommand;
import io.subutai.core.repository.cli.PackageInfoCommand;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PackageInfoCommandTest extends SystemOutRedirectTest
{
    private static final String PACKAGE_INFO = "info";
    @Mock
    RepositoryManager repositoryManager;

    PackageInfoCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new PackageInfoCommand( repositoryManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new ListPackagesCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        when( repositoryManager.getPackageInfo( anyString() ) ).thenReturn( PACKAGE_INFO );

        command.doExecute();

        verify( repositoryManager ).getPackageInfo( anyString() );
        assertEquals( getSysOut(), PACKAGE_INFO );


        RepositoryException exception = mock( RepositoryException.class );
        doThrow( exception ).when( repositoryManager ).getPackageInfo( anyString() );

        command.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
