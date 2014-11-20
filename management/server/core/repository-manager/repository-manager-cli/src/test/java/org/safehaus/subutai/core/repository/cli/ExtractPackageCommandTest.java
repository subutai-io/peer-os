package org.safehaus.subutai.core.repository.cli;


import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ExtractPackageCommandTest
{
    @Mock
    RepositoryManager repositoryManager;

    ExtractPackageCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ExtractPackageCommand( repositoryManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new ExtractPackageCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {

        command.doExecute();

        verify( repositoryManager ).extractPackageByName( anyString() );

        RepositoryException exception = mock( RepositoryException.class );

        doThrow( exception ).when( repositoryManager ).extractPackageByName( anyString() );

        command.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
