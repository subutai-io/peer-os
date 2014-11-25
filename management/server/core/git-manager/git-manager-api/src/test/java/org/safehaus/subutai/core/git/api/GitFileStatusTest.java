package org.safehaus.subutai.core.git.api;


import org.junit.Test;
import org.safehaus.subutai.core.git.api.GitFileStatus;

import static junit.framework.Assert.assertEquals;


/**
 * Test for GitFileStatus
 */
public class GitFileStatusTest
{

    @Test
    public void shouldReturnStatusByAcronym()
    {

        assertEquals( GitFileStatus.MODIFIED, GitFileStatus.parse( "M" ) );
        assertEquals( GitFileStatus.ADDED, GitFileStatus.parse( "A" ) );
        assertEquals( GitFileStatus.COPIED, GitFileStatus.parse( "C" ) );
        assertEquals( GitFileStatus.DELETED, GitFileStatus.parse( "D" ) );
        assertEquals( GitFileStatus.RENAMED, GitFileStatus.parse( "R" ) );
        assertEquals( GitFileStatus.UNMERGED, GitFileStatus.parse( "U" ) );
        assertEquals( GitFileStatus.UNVERSIONED, GitFileStatus.parse( "X" ) );
        assertEquals( GitFileStatus.UNMODIFIED, GitFileStatus.parse( "" ) );
        assertEquals( GitFileStatus.UNKNOWN, GitFileStatus.parse( "-" ) );
    }
}
