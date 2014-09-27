package org.safehaus.subutai.core.git.impl;


import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitFileStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test for GitManagerImpl
 */
public class GitManagerImplTest
{
    private static final String MASTER_BRANCH = "master";
    private static final String REPOSITORY_ROOT = "root";
    private static final String FILEP_PATH =
            "management/server/core/communication-manager/communication-manager-api/src/main/java/org/safehaus"
                    + "/subutai/core/communication/api/CommandJson.java";

    private static final String DIFF_BRANCH_OUTPUT = String.format( "M %s", FILEP_PATH );


    @Test
    public void shouldReturnDiffBranches() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), DIFF_BRANCH_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );

        List<GitChangedFile> changedFiles = gitManager.diffBranches( agent, REPOSITORY_ROOT, MASTER_BRANCH );
        GitChangedFile changedFile = changedFiles.get( 0 );

        assertTrue( changedFiles.contains( new GitChangedFile( GitFileStatus.MODIFIED, FILEP_PATH ) ) );
        assertEquals( GitFileStatus.MODIFIED, changedFile.getGitFileStatus() );
        assertEquals( FILEP_PATH, changedFile.getGitFilePath() );
    }
}
