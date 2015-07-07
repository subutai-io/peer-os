package io.subutai.core.git.impl;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitBranch;
import io.subutai.core.git.api.GitChangedFile;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitFileStatus;

import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Lists;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for GitManagerImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class GitManagerImplTest extends SystemOutRedirectTest
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

    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;
    @Mock
    CommandUtil commandUtil;
    @Mock
    CommandResult commandResult;

    private GitManagerImpl gitManager;


    @Before
    public void setUp() throws CommandException, PeerException
    {
        gitManager = new GitManagerImpl( peerManager );
        gitManager.commandUtil = commandUtil;
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( commandUtil.execute( any( RequestBuilder.class ), any( Host.class ) ) ).thenReturn( commandResult );
    }


    private void setOutput( String output )
    {
        when( commandResult.getStdOut() ).thenReturn( output );
    }


    private void throwCommandException() throws CommandException
    {
        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), any( Host.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullCommandRunner()
    {
        new GitManagerImpl( null );
    }


    @Test
    public void testInit() throws GitException, CommandException
    {
        setOutput( SOME_DUMMY_OUTPUT );

        gitManager.init( REPOSITORY_ROOT );

        verify( commandUtil ).execute( new RequestBuilder( "git init" ).withCwd( REPOSITORY_ROOT ), managementHost );
        assertEquals( SOME_DUMMY_OUTPUT, getSysOut() );
    }


    @Test( expected = GitException.class )
    public void shouldThrowGitException() throws GitException, CommandException
    {
        throwCommandException();

        gitManager.init( REPOSITORY_ROOT );
    }


    @Test
    public void shouldReturnDiffBranchWithMasterBranch() throws GitException
    {

        setOutput( DIFF_BRANCH_OUTPUT );


        List<GitChangedFile> changedFiles = gitManager.diffBranches( REPOSITORY_ROOT, MASTER_BRANCH );
        GitChangedFile changedFile = changedFiles.get( 0 );


        assertTrue( changedFiles.contains( new GitChangedFile( GitFileStatus.MODIFIED, MODIFIED_FILE_PATH ) ) );
        assertEquals( GitFileStatus.MODIFIED, changedFile.getGitFileStatus() );
        assertEquals( MODIFIED_FILE_PATH, changedFile.getGitFilePath() );
    }


    @Test
    public void shouldReturnDiffBranches() throws GitException
    {
        setOutput( DIFF_BRANCH_OUTPUT );


        List<GitChangedFile> changedFiles = gitManager.diffBranches( REPOSITORY_ROOT, MASTER_BRANCH, DUMMY_BRANCH );
        GitChangedFile changedFile = changedFiles.get( 0 );


        assertTrue( changedFiles.contains( new GitChangedFile( GitFileStatus.MODIFIED, MODIFIED_FILE_PATH ) ) );
        assertEquals( GitFileStatus.MODIFIED, changedFile.getGitFileStatus() );
        assertEquals( MODIFIED_FILE_PATH, changedFile.getGitFilePath() );
    }


    @Test
    public void shouldReturnDiffFile() throws GitException
    {
        setOutput( DIFF_FILE_OUTPUT );

        String diffFile = gitManager.diffFile( REPOSITORY_ROOT, MASTER_BRANCH, DUMMY_BRANCH, FILE_PATH );

        assertEquals( diffFile, DIFF_FILE_OUTPUT );
    }


    @Test
    public void shouldReturnDiffFileWithMasterBranch() throws GitException
    {
        setOutput( DIFF_FILE_OUTPUT );

        String diffFile = gitManager.diffFile( REPOSITORY_ROOT, MASTER_BRANCH, FILE_PATH );

        assertEquals( diffFile, DIFF_FILE_OUTPUT );
    }


    @Test
    public void shouldRunAddCommand() throws GitException, CommandException
    {
        gitManager.add( REPOSITORY_ROOT, filePaths );

        verify( commandUtil )
                .execute( new RequestBuilder( "git add" ).withCwd( REPOSITORY_ROOT ).withCmdArgs( filePaths ),
                        managementHost );
    }


    @Test
    public void shouldRunAddAllCommand() throws GitException, CommandException
    {
        gitManager.addAll( REPOSITORY_ROOT );

        verify( commandUtil ).execute( new RequestBuilder( "git add -A" ).withCwd( REPOSITORY_ROOT ), managementHost );
    }


    @Test
    public void shouldRunDeleteCommand() throws GitException, CommandException
    {
        gitManager.delete( REPOSITORY_ROOT, filePaths );

        verify( commandUtil )
                .execute( new RequestBuilder( "git rm" ).withCwd( REPOSITORY_ROOT ).withCmdArgs( filePaths ),
                        managementHost );
    }


    @Test( expected = GitException.class )
    public void shouldCommitAndReturnCommitId() throws GitException
    {
        setOutput( COMMIT_OUTPUT );

        String commitId = gitManager.commit( REPOSITORY_ROOT, filePaths, COMMIT_MESSAGE, false );

        assertEquals( COMMIT_ID, commitId );


        setOutput( "" );

        gitManager.commit( REPOSITORY_ROOT, filePaths, COMMIT_MESSAGE, false );
    }


    @Test( expected = GitException.class )
    public void shouldCommitAllAndReturnCommitId() throws GitException
    {
        setOutput( COMMIT_OUTPUT );

        String commitId = gitManager.commitAll( REPOSITORY_ROOT, COMMIT_MESSAGE );

        assertEquals( COMMIT_ID, commitId );

        setOutput( "" );

        gitManager.commitAll( REPOSITORY_ROOT, COMMIT_MESSAGE );
    }


    @Test( expected = GitException.class )
    public void shouldSupplyProperRequestBuilder() throws GitException, CommandException
    {
        setOutput( COMMIT_OUTPUT );

        gitManager.commitAll( REPOSITORY_ROOT, COMMIT_MESSAGE );

        verify( commandUtil ).execute( new RequestBuilder( String.format( "git commit -a -m \"%s\"", COMMIT_MESSAGE ) )
                .withCwd( REPOSITORY_ROOT ), managementHost );

        setOutput( "" );

        gitManager.commitAll( REPOSITORY_ROOT, COMMIT_MESSAGE );
    }


    @Test
    public void shouldRunCloneCommand() throws GitException, CommandException
    {
        gitManager.clone( DUMMY_BRANCH, FILE_PATH );

        verify( commandUtil ).execute( new RequestBuilder(
                String.format( "git clone -b %s %s %s", DUMMY_BRANCH, Common.GIT_REPO_URL, FILE_PATH ) )
                .withTimeout( 180 ), managementHost );
    }


    @Test
    public void shouldRunCheckoutCommand() throws GitException, CommandException
    {
        gitManager.checkout( REPOSITORY_ROOT, DUMMY_BRANCH, false );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git checkout %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );
    }


    @Test
    public void shouldRunCheckoutCommandWithNewBranch() throws GitException, CommandException
    {
        gitManager.checkout( REPOSITORY_ROOT, DUMMY_BRANCH, true );

        verify( commandUtil ).execute( new RequestBuilder( String.format( "git checkout --track -b %s", DUMMY_BRANCH ) )
                .withCwd( REPOSITORY_ROOT ), managementHost );
    }


    @Test
    public void shouldRunDeleteBranchCommand() throws GitException, CommandException
    {
        gitManager.deleteBranch( REPOSITORY_ROOT, DUMMY_BRANCH );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git branch -d %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );
    }


    @Test
    public void shouldRunMergeCommand() throws GitException, CommandException
    {

        gitManager.merge( REPOSITORY_ROOT );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git merge %s", MASTER_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );
    }


    @Test
    public void shouldRunMergeCommandWithBranch() throws GitException, CommandException
    {
        gitManager.merge( REPOSITORY_ROOT, DUMMY_BRANCH );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git merge %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );
    }


    @Test
    public void shouldRunPullCommand() throws GitException, CommandException
    {
        gitManager.pull( REPOSITORY_ROOT );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git pull origin %s", MASTER_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );
    }


    @Test
    public void shouldRunPullCommandWithBranch() throws GitException, CommandException
    {
        gitManager.pull( REPOSITORY_ROOT, DUMMY_BRANCH );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git pull origin %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );
    }


    @Test
    public void shouldReturnDummyBranch() throws GitException
    {
        setOutput( GIT_BRANCH_DUMMY_OUTPUT );

        GitBranch gitBranch = gitManager.currentBranch( REPOSITORY_ROOT );

        assertEquals( new GitBranch( DUMMY_BRANCH, true ), gitBranch );
    }


    @Test
    public void shouldReturnMasterBranch() throws GitException
    {
        setOutput( GIT_BRANCH_OUTPUT );

        GitBranch gitBranch = gitManager.currentBranch( REPOSITORY_ROOT );

        assertEquals( new GitBranch( MASTER_BRANCH, true ), gitBranch );
    }


    @Test
    public void shouldListLocalBranches() throws GitException
    {
        setOutput( GIT_BRANCH_DUMMY_OUTPUT );

        List<GitBranch> gitBranches = gitManager.listBranches( REPOSITORY_ROOT, false );

        assertEquals( 2, gitBranches.size() );
        assertTrue( gitBranches.contains( new GitBranch( MASTER_BRANCH, false ) ) );
        assertTrue( gitBranches.contains( new GitBranch( DUMMY_BRANCH, true ) ) );
    }


    @Test
    public void shouldListRemoteBranches() throws GitException, CommandException
    {
        setOutput( GIT_BRANCH_REMOTE_OUTPUT );

        List<GitBranch> gitBranches = gitManager.listBranches( REPOSITORY_ROOT, true );

        verify( commandUtil )
                .execute( new RequestBuilder( "git branch -r" ).withCwd( REPOSITORY_ROOT ), managementHost );
        assertEquals( 3, gitBranches.size() );
        assertTrue( gitBranches.contains( new GitBranch( REMOTE_MASTER_BRANCH, false ) ) );
    }


    @Test
    public void testPush() throws GitException, CommandException
    {

        gitManager.push( REPOSITORY_ROOT, DUMMY_BRANCH );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git push origin %s", DUMMY_BRANCH ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );

        try
        {
            gitManager.push( REPOSITORY_ROOT, MASTER_BRANCH );
        }
        catch ( GitException e )
        {
            assertThat( e.getMessage(), containsString( "Can not perform push to remote master branch" ) );
        }
    }


    @Test
    public void shouldRunUndoSoftCommand() throws GitException, CommandException
    {
        gitManager.undoSoft( REPOSITORY_ROOT, filePaths );

        verify( commandUtil )
                .execute( new RequestBuilder( "git checkout --" ).withCwd( REPOSITORY_ROOT ).withCmdArgs( filePaths ),
                        managementHost );
    }


    @Test
    public void shouldRunUndoHardCommand() throws GitException, CommandException
    {
        gitManager.undoHard( REPOSITORY_ROOT, DUMMY_BRANCH );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git fetch origin && git reset --hard origin/%s", DUMMY_BRANCH ) )
                        .withCwd( REPOSITORY_ROOT ), managementHost );
    }


    @Test
    public void shouldRunUndoHardCommandWithMasterBranch() throws GitException, CommandException
    {
        gitManager.undoHard( REPOSITORY_ROOT );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git fetch origin && git reset --hard origin/%s", MASTER_BRANCH ) )
                        .withCwd( REPOSITORY_ROOT ), managementHost );
    }


    @Test
    public void shouldRunRevertCommitCommand() throws GitException, CommandException
    {
        gitManager.revertCommit( REPOSITORY_ROOT, COMMIT_ID );

        verify( commandUtil )
                .execute( new RequestBuilder( String.format( "git revert %s", COMMIT_ID ) ).withCwd( REPOSITORY_ROOT ),
                        managementHost );
    }


    @Test
    public void shouldRunStashCommand() throws GitException, CommandException
    {
        gitManager.stash( REPOSITORY_ROOT );

        verify( commandUtil ).execute( new RequestBuilder( String.format( "git stash" ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );
    }


    @Test
    public void shouldRunUnstashCommand() throws GitException, CommandException
    {
        gitManager.unstash( REPOSITORY_ROOT, STASH_NAME );

        verify( commandUtil ).execute(
                new RequestBuilder( String.format( "git stash apply %s", STASH_NAME ) ).withCwd( REPOSITORY_ROOT ),
                managementHost );
    }


    @Test
    public void shouldReturnStashesList() throws GitException
    {
        setOutput( STASH_NAME );

        List<String> stashNames = gitManager.listStashes( REPOSITORY_ROOT );

        assertTrue( stashNames.contains( STASH_NAME ) );
    }


    @Test( expected = GitException.class )
    public void testGetManagementHost() throws Exception
    {
        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).getManagementHost();

        gitManager.getManagementHost();
    }
}
