package io.subutai.core.repository.cli;


import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.repository.api.RepositoryException;
import io.subutai.core.repository.api.RepositoryManager;
import io.subutai.core.repository.cli.ExtractFilesCommand;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ExtractFilesCommandTest
{
    @Mock
    RepositoryManager repositoryManager;

    ExtractFilesCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ExtractFilesCommand( repositoryManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new ExtractFilesCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {

        command.doExecute();

        verify( repositoryManager ).extractPackageFiles( anyString(), anySet() );

        RepositoryException exception = mock( RepositoryException.class );

        doThrow( exception ).when( repositoryManager ).extractPackageFiles( anyString(), anySet() );

        command.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
