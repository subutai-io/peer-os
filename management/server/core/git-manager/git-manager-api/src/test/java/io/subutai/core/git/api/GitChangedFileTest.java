package io.subutai.core.git.api;


import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import io.subutai.core.git.api.GitChangedFile;
import io.subutai.core.git.api.GitFileStatus;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;


/**
 * Test for GitChangedFile
 */
public class GitChangedFileTest
{
    private static final String FILE_PATH = "file/path";


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullStatus()
    {
        new GitChangedFile( null, FILE_PATH );
    }


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailOnNullFilePath()
    {
        new GitChangedFile( GitFileStatus.MODIFIED, null );
    }


    @Test
    public void shouldReturnProperProperties()
    {
        GitChangedFile gitChangedFile = new GitChangedFile( GitFileStatus.MODIFIED, FILE_PATH );

        assertEquals( GitFileStatus.MODIFIED, gitChangedFile.getGitFileStatus() );
        assertEquals( FILE_PATH, gitChangedFile.getGitFilePath() );
    }


    @Test
    public void shouldReturnProperToString()
    {
        GitChangedFile gitChangedFile = new GitChangedFile( GitFileStatus.MODIFIED, FILE_PATH );

        assertThat( gitChangedFile.toString(), containsString( FILE_PATH ) );
        assertThat( gitChangedFile.toString(), containsString( GitFileStatus.MODIFIED.name() ) );
    }


    @Test
    public void shouldBeEqual()
    {
        GitChangedFile gitChangedFile = new GitChangedFile( GitFileStatus.MODIFIED, FILE_PATH );

        assertEquals( gitChangedFile, new GitChangedFile( GitFileStatus.MODIFIED, FILE_PATH ) );

        assertFalse( gitChangedFile.equals( new Object() ));
    }


    @Test
    public void checkHashCode()
    {
        GitChangedFile gitChangedFile = new GitChangedFile( GitFileStatus.MODIFIED, FILE_PATH );


        Map<GitChangedFile, GitChangedFile> map = new HashMap<>();
        map.put( gitChangedFile, gitChangedFile );

        assertEquals( map.get( gitChangedFile ), gitChangedFile );
    }
}
