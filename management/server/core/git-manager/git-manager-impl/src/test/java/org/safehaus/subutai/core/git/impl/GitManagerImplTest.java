package org.safehaus.subutai.core.git.impl;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.git.api.GitBranch;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitFileStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
    private static final String GIT_BRANCH_DUMMY_OUTPUT = "* dummy\n" + "  master";
    private static final String GIT_BRANCH_OUTPUT = " dummy\n" + "  master";
    private static final String GIT_BRANCH_REMOTE_OUTPUT = "  origin/karaf-3\n  origin/lucene-fix-ui\n  origin/master";
    private static final String REMOTE_MASTER_BRANCH = "origin/master";
    private static final List<String> filePaths = Lists.newArrayList( FILE_PATH );
    private static final String STASH_NAME = "stash name";

    private Agent agent;
    private Command command;
    private CommandRunner commandRunner;
    private GitManagerImpl gitManager;
    private ByteArrayOutputStream myOut;
    private boolean verifyCommandExecution;


    @Before
    public void setUp()
    {
        verifyCommandExecution = true;
        agent = MockUtils.getAgent( UUID.randomUUID() );
        command = MockUtils.getCommand( true, true, agent.getUuid(), SOME_DUMMY_OUTPUT, null, null );
        commandRunner = MockUtils.getCommandRunner( command );
        gitManager = new GitManagerImpl( commandRunner );
    }


    @After
    public void tearDown() throws CommandException
    {
        if ( verifyCommandExecution )
        {
            verify( command, times( 1 ) ).execute();
        }
        System.setOut( System.out );
    }


    private void catchSysOut()
    {
        myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );
    }


    private String getSysOut()
    {
        return myOut.toString().trim();
    }


    private void setCommandStatus( boolean completed, boolean succeeded, String output )
    {
        command = MockUtils.getCommand( completed, succeeded, agent.getUuid(), output, null, null );
        commandRunner = MockUtils.getCommandRunner( command );
        gitManager = new GitManagerImpl( commandRunner );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullCommandRunner()
    {
        verifyCommandExecution = false;
        new GitManagerImpl( null );
    }


    @Test
    public void shouldPrintToSysOut() throws GitException
    {
        catchSysOut();


        gitManager.init( agent, REPOSITORY_ROOT );


        assertEquals( SOME_DUMMY_OUTPUT, getSysOut() );
    }


    @Test( expected = GitException.class )
    public void shouldThrowGitException() throws GitException
    {
        setCommandStatus( true, false, SOME_DUMMY_OUTPUT );


        gitManager.init( agent, REPOSITORY_ROOT );
    }


    @Test
    public void shouldReturnDiffBranchWithMasterBranch() throws GitException
    {

        setCommandStatus( true, true, DIFF_BRANCH_OUTPUT );


        List<GitChangedFile> changedFiles = gitManager.diffBranches( agent, REPOSITORY_ROOT, MASTER_BRANCH );
        GitChangedFile changedFile = changedFiles.get( 0 );


        assertTrue( changedFiles.contains( new GitChangedFile( GitFileStatus.MODIFIED, MODIFIED_FILE_PATH ) ) );
        assertEquals( GitFileStatus.MODIFIED, changedFile.getGitFileStatus() );
        assertEquals( MODIFIED_FILE_PATH, changedFile.getGitFilePath() );
    }


    @Test
    public void shouldReturnDiffBranches() throws GitException
    {
        setCommandStatus( true, true, DIFF_BRANCH_OUTPUT );


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
        setCommandStatus( true, true, DIFF_FILE_OUTPUT );


        String diffFile = gitManager.diffFile( agent, REPOSITORY_ROOT, MASTER_BRANCH, DUMMY_BRANCH, FILE_PATH );


        assertEquals( diffFile, DIFF_FILE_OUTPUT );
    }


    @Test
    public void shouldReturnDiffFileWithMasterBranch() throws GitException
    {
        setCommandStatus( true, true, DIFF_FILE_OUTPUT );


        String diffFile = gitManager.diffFile( agent, REPOSITORY_ROOT, MASTER_BRANCH, FILE_PATH );


        assertEquals( diffFile, DIFF_FILE_OUTPUT );
    }


    @Test
    public void shouldRunAddCommand() throws GitException, CommandException
    {

        gitManager.add( agent, REPOSITORY_ROOT, filePaths );


        verify( commandRunner )
                .createCommand( new RequestBuilder( "git add" ).withCwd( REPOSITORY_ROOT ).withCmdArgs( filePaths ),
                        Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunAddAllCommand() throws GitException, CommandException
    {

        gitManager.addAll( agent, REPOSITORY_ROOT );


        verify( commandRunner ).createCommand( new RequestBuilder( "git add -A" ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunDeleteCommand() throws GitException, CommandException
    {

        gitManager.delete( agent, REPOSITORY_ROOT, filePaths );


        verify( commandRunner )
                .createCommand( new RequestBuilder( "git rm" ).withCwd( REPOSITORY_ROOT ).withCmdArgs( filePaths ),
                        Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldCommitAndReturnCommitId() throws GitException
    {

        setCommandStatus( true, true, COMMIT_OUTPUT );


        String commitId = gitManager.commit( agent, REPOSITORY_ROOT, filePaths, COMMIT_MESSAGE, false );


        assertEquals( COMMIT_ID, commitId );
    }


    @Test
    public void shouldCommitAllAndReturnCommitId() throws GitException
    {

        setCommandStatus( true, true, COMMIT_OUTPUT );


        String commitId = gitManager.commitAll( agent, REPOSITORY_ROOT, COMMIT_MESSAGE );


        assertEquals( COMMIT_ID, commitId );
    }


    @Test
    public void shouldSupplyProperRequestBuilder() throws GitException
    {

        setCommandStatus( true, true, COMMIT_OUTPUT );


        gitManager.commitAll( agent, REPOSITORY_ROOT, COMMIT_MESSAGE );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git commit -a -m \"%s\"", COMMIT_MESSAGE ) )
                        .withCwd( REPOSITORY_ROOT ), Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunCloneCommand() throws GitException, CommandException
    {

        gitManager.clone( agent, DUMMY_BRANCH, FILE_PATH );


        verify( commandRunner ).createCommand( new RequestBuilder(
                String.format( "git clone -b %s %s %s", DUMMY_BRANCH, Common.GIT_REPO_URL, FILE_PATH ) )
                .withTimeout( 180 ), Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunCheckoutCommand() throws GitException
    {

        gitManager.checkout( agent, REPOSITORY_ROOT, DUMMY_BRANCH, false );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git checkout %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunCheckoutCommandWithNewBranch() throws GitException
    {

        gitManager.checkout( agent, REPOSITORY_ROOT, DUMMY_BRANCH, true );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git checkout --track -b %s", DUMMY_BRANCH ) )
                        .withCwd( REPOSITORY_ROOT ), Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunDeleteBranchCommand() throws GitException, CommandException
    {

        gitManager.deleteBranch( agent, REPOSITORY_ROOT, DUMMY_BRANCH );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git branch -d %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunMergeCommand() throws GitException
    {

        gitManager.merge( agent, REPOSITORY_ROOT );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git merge %s", MASTER_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunMergeCommandWithBranch() throws GitException
    {

        gitManager.merge( agent, REPOSITORY_ROOT, DUMMY_BRANCH );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git merge %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunPullCommand() throws GitException
    {

        gitManager.pull( agent, REPOSITORY_ROOT );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git pull origin %s", MASTER_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunPullCommandWithBranch() throws GitException
    {

        gitManager.pull( agent, REPOSITORY_ROOT, DUMMY_BRANCH );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git pull origin %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldReturnDummyBranch() throws GitException
    {

        setCommandStatus( true, true, GIT_BRANCH_DUMMY_OUTPUT );


        GitBranch gitBranch = gitManager.currentBranch( agent, REPOSITORY_ROOT );


        assertEquals( new GitBranch( DUMMY_BRANCH, true ), gitBranch );
    }


    @Test
    public void shouldReturnMasterBranch() throws GitException
    {

        setCommandStatus( true, true, GIT_BRANCH_OUTPUT );


        GitBranch gitBranch = gitManager.currentBranch( agent, REPOSITORY_ROOT );


        assertEquals( new GitBranch( MASTER_BRANCH, true ), gitBranch );
    }


    @Test
    public void shouldListLocalBranches() throws GitException
    {

        setCommandStatus( true, true, GIT_BRANCH_DUMMY_OUTPUT );


        List<GitBranch> gitBranches = gitManager.listBranches( agent, REPOSITORY_ROOT, false );


        assertEquals( 2, gitBranches.size() );
        assertTrue( gitBranches.contains( new GitBranch( MASTER_BRANCH, false ) ) );
        assertTrue( gitBranches.contains( new GitBranch( DUMMY_BRANCH, true ) ) );
    }


    @Test
    public void shouldListRemoteBranches() throws GitException
    {

        setCommandStatus( true, true, GIT_BRANCH_REMOTE_OUTPUT );


        List<GitBranch> gitBranches = gitManager.listBranches( agent, REPOSITORY_ROOT, true );


        verify( commandRunner ).createCommand( new RequestBuilder( "git branch -r" ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
        assertEquals( 3, gitBranches.size() );
        assertTrue( gitBranches.contains( new GitBranch( REMOTE_MASTER_BRANCH, false ) ) );
    }


    @Test
    public void shouldFailPushToMaster()
    {
        verifyCommandExecution = false;
        try
        {
            gitManager.push( agent, REPOSITORY_ROOT, MASTER_BRANCH );
        }
        catch ( GitException e )
        {
            assertThat( e.getMessage(), containsString( "Can not perform push to remote master branch" ) );
        }
    }


    @Test
    public void shouldRunUndoSoftCommand() throws GitException
    {

        gitManager.undoSoft( agent, REPOSITORY_ROOT, filePaths );


        verify( commandRunner ).createCommand(
                new RequestBuilder( "git checkout --" ).withCwd( REPOSITORY_ROOT ).withCmdArgs( filePaths ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunUndoHardCommand() throws GitException
    {

        gitManager.undoHard( agent, REPOSITORY_ROOT, DUMMY_BRANCH );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git fetch origin && git reset --hard origin/%s", DUMMY_BRANCH ) )
                        .withCwd( REPOSITORY_ROOT ), Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunUndoHardCommandWithMasterBranch() throws GitException
    {

        gitManager.undoHard( agent, REPOSITORY_ROOT );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git fetch origin && git reset --hard origin/%s", MASTER_BRANCH ) )
                        .withCwd( REPOSITORY_ROOT ), Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunRevertCommitCommand() throws GitException
    {

        gitManager.revertCommit( agent, REPOSITORY_ROOT, COMMIT_ID );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git revert %s", COMMIT_ID ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunStashCommand() throws GitException
    {

        gitManager.stash( agent, REPOSITORY_ROOT );


        verify( commandRunner )
                .createCommand( new RequestBuilder( String.format( "git stash" ) ).withCwd( REPOSITORY_ROOT ),
                        Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldRunUnstashCommand() throws GitException
    {

        gitManager.unstash( agent, REPOSITORY_ROOT, STASH_NAME );


        verify( commandRunner ).createCommand(
                new RequestBuilder( String.format( "git stash apply %s", STASH_NAME ) ).withCwd( REPOSITORY_ROOT ),
                Sets.newHashSet( agent ) );
    }


    @Test
    public void shouldReturnStashesList() throws GitException
    {

        setCommandStatus( true, true, STASH_NAME );

        List<String> stashNames = gitManager.listStashes( agent, REPOSITORY_ROOT );

        assertTrue( stashNames.contains( STASH_NAME ) );
    }
}
