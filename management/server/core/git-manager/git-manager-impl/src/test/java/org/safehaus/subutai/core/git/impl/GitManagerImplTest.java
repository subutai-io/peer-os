package org.safehaus.subutai.core.git.impl;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitFileStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Test for GitManagerImpl
 */
public class GitManagerImplTest
{
    private static final String MASTER_BRANCH = "master";
    private static final String DUMMY_BRANCH = "dummy";
    private static final String REPOSITORY_ROOT = "root";
    private static final String MODIFIED_FILE_PATH =
            "management/server/core/communication-manager/communication-manager-api/src/main/java/org/safehaus"
                    + "/subutai/core/communication/api/CommandJson.java";

    private static final String DIFF_BRANCH_OUTPUT = String.format( "M %s", MODIFIED_FILE_PATH );
    private static final String DIFF_FILE_OUTPUT = "+new content\n-old content";
    private static final String FILE_PATH = "some/file/path";
    private static final String SOME_DUMMY_OUTPUT = "some dummy output";
    private static final String COMMIT_MESSAGE = "commit message";
    private static final String COMMIT_ID = "24b6f79";
    private static final String COMMIT_OUTPUT =
            "[core-unit-test 24b6f79] Core Unit Test\n 2 files changed, 35 insertions(+), 3 deletions(-)";


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullCommandRunner()
    {
        new GitManagerImpl( null );
    }


    @Test
    public void shouldPrintToSysOut() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );

        //catch sys out
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );

        gitManager.init( agent, REPOSITORY_ROOT );

        assertEquals( SOME_DUMMY_OUTPUT, myOut.toString().trim() );
    }


    @Test( expected = GitException.class )
    public void shouldThrowGitException() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, false, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );

        gitManager.init( agent, REPOSITORY_ROOT );
    }


    @Test
    public void shouldReturnDiffBranchWithMasterBranch() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), DIFF_BRANCH_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );

        List<GitChangedFile> changedFiles = gitManager.diffBranches( agent, REPOSITORY_ROOT, MASTER_BRANCH );
        GitChangedFile changedFile = changedFiles.get( 0 );

        assertTrue( changedFiles.contains( new GitChangedFile( GitFileStatus.MODIFIED, MODIFIED_FILE_PATH ) ) );
        assertEquals( GitFileStatus.MODIFIED, changedFile.getGitFileStatus() );
        assertEquals( MODIFIED_FILE_PATH, changedFile.getGitFilePath() );
    }


    @Test
    public void shouldReturnDiffBranches() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), DIFF_BRANCH_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );

        List<GitChangedFile> changedFiles =
                gitManager.diffBranches( agent, REPOSITORY_ROOT, MASTER_BRANCH, DUMMY_BRANCH );
        GitChangedFile changedFile = changedFiles.get( 0 );

        assertTrue( changedFiles.contains( new GitChangedFile( GitFileStatus.MODIFIED, MODIFIED_FILE_PATH ) ) );
        assertEquals( GitFileStatus.MODIFIED, changedFile.getGitFileStatus() );
        assertEquals( MODIFIED_FILE_PATH, changedFile.getGitFilePath() );
    }


    @Test
    public void shouldReturnDiffFile() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), DIFF_FILE_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );

        String diffFile = gitManager.diffFile( agent, REPOSITORY_ROOT, MASTER_BRANCH, DUMMY_BRANCH, FILE_PATH );

        assertEquals( diffFile, DIFF_FILE_OUTPUT );
    }


    @Test
    public void shouldReturnDiffFileWithMasterBranch() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), DIFF_FILE_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );

        String diffFile = gitManager.diffFile( agent, REPOSITORY_ROOT, MASTER_BRANCH, FILE_PATH );

        assertEquals( diffFile, DIFF_FILE_OUTPUT );
    }


    @Test
    public void shouldRunAddCommand() throws GitException, CommandException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        gitManager.add( agent, REPOSITORY_ROOT, Lists.newArrayList( "" ) );

        verify( command, times( 1 ) ).execute();
    }


    @Test
    public void shouldRunAddAllCommand() throws GitException, CommandException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        gitManager.addAll( agent, REPOSITORY_ROOT );

        verify( command, times( 1 ) ).execute();
    }


    @Test
    public void shouldRunDeleteCommand() throws GitException, CommandException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        gitManager.delete( agent, REPOSITORY_ROOT, Lists.newArrayList( "" ) );

        verify( command, times( 1 ) ).execute();
    }


    @Test
    public void shouldCommitAndReturnCommitId() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), COMMIT_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        String commitId = gitManager.commit( agent, REPOSITORY_ROOT, Lists.newArrayList( "" ), COMMIT_MESSAGE, false );

        assertEquals( COMMIT_ID, commitId );
    }


    @Test
    public void shouldCommitAllAndReturnCommitId() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), COMMIT_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        String commitId = gitManager.commitAll( agent, REPOSITORY_ROOT, COMMIT_MESSAGE );

        assertEquals( COMMIT_ID, commitId );
    }


    @Test
    public void shouldSupplyProperRequestBuilder() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), COMMIT_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        gitManager.commitAll( agent, REPOSITORY_ROOT, COMMIT_MESSAGE );

        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git commit -a -m \"%s\"", COMMIT_MESSAGE ) )
                        .withCwd( REPOSITORY_ROOT ), Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunCloneCommand() throws GitException, CommandException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        gitManager.clone( agent, DUMMY_BRANCH, FILE_PATH );

        verify( command, times( 1 ) ).execute();
    }


    @Test
    public void shouldRunCheckoutCommand() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        gitManager.checkout( agent, REPOSITORY_ROOT, DUMMY_BRANCH, false );

        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git checkout %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunCheckoutCommandWithNewBranch() throws GitException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        gitManager.checkout( agent, REPOSITORY_ROOT, DUMMY_BRANCH, true );

        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git checkout --track -b %s", DUMMY_BRANCH ) )
                        .withCwd( REPOSITORY_ROOT ), Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunDeleteBranchCommand() throws GitException, CommandException
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        Command command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        CommandRunner commandRunner = MockUtils.getCommandRunner( command );

        GitManagerImpl gitManager = new GitManagerImpl( commandRunner );


        gitManager.deleteBranch( agent, REPOSITORY_ROOT, DUMMY_BRANCH );

        verify( command, times( 1 ) ).execute();
    }
}
