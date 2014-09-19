package org.safehaus.subutai.core.git.impl;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.git.api.GitBranch;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.git.api.GitCommand;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitFileStatus;
import org.safehaus.subutai.core.git.api.GitManager;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * This is an implementation of GitManager interface
 */
public class GitManagerImpl implements GitManager
{

    private static final String MASTER_BRANCH = "master";
    private static final String LINE_SEPARATOR = "\n";
    private CommandRunner commandRunner;


    public GitManagerImpl( final CommandRunner commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    public void init()
    {
    }


    public void destroy()
    {
    }


    /**
     * Returns list of files changed between specified branch and master branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     *
     * @return - list of {@code GitChangedFile}
     */
    @Override
    public List<GitChangedFile> diffBranches( final Agent host, final String repositoryRoot, final String branchName1 )
            throws GitException
    {
        return diffBranches( host, repositoryRoot, branchName1, MASTER_BRANCH );
    }


    /**
     * Returns list of files changed between specified branches
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     * @param branchName2 - name of branch 2
     *
     * @return - list of {@code GitChangedFile}
     */
    @Override
    public List<GitChangedFile> diffBranches( final Agent host, final String repositoryRoot, final String branchName1,
                                              final String branchName2 ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName1 ), "Branch name 1 is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName2 ), "Branch name 2 is null or empty" );

        Command diffCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git diff --name-status %s %s", branchName1, branchName2 ) )
                        .withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( diffCommand, host, GitCommand.DIFF, false );

        StringTokenizer lines = new StringTokenizer( diffCommand.getResults().get( host.getUuid() ).getStdOut(), "\n" );

        List<GitChangedFile> gitChangedFiles = new ArrayList<>();

        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            if ( line != null )
            {
                String[] ss = line.split( "\\s+" );
                if ( ss.length == 2 )
                {
                    gitChangedFiles.add( new GitChangedFile( GitFileStatus.parse( ss[0] ), ss[1] ) );
                }
            }
        }

        return gitChangedFiles;
    }


    /**
     * Returns diff in file between specified branch and master branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     * @param filePath - relative (to repo root) file path
     */
    @Override
    public String diffFile( final Agent host, final String repositoryRoot, final String branchName1,
                            final String filePath ) throws GitException
    {
        return diffFile( host, repositoryRoot, branchName1, MASTER_BRANCH, filePath );
    }


    /**
     * Returns diff in file between specified branches
     *
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     * @param branchName2 - name of branch 2
     * @param filePath - relative (to repo root) file path
     *
     * @return - differences in file {@code String}
     */
    @Override
    public String diffFile( final Agent host, final String repositoryRoot, final String branchName1,
                            final String branchName2, final String filePath ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName1 ), "Branch name 1 is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName2 ), "Branch name 2 is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( filePath ), "File path is null or empty" );

        Command diffCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git diff %s %s %s", branchName1, branchName2, filePath ) )
                        .withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( diffCommand, host, GitCommand.DIFF, false );

        return diffCommand.getResults().get( host.getUuid() ).getStdOut();
    }


    /**
     * Initializes empty git repo in the specified directory
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     */
    @Override
    public void init( final Agent host, final String repositoryRoot ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );

        Command initCommand = commandRunner
                .createCommand( new RequestBuilder( "git init" ).withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( initCommand, host, GitCommand.INIT );
    }


    private void validateHostNRepoRoot( Agent host, String repositoryRoot )
    {
        Preconditions.checkNotNull( host, "Agent is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( repositoryRoot ), "Repository root is null or empty" );
    }


    private void runCommand( Command command, Agent host, GitCommand gitCommand ) throws GitException
    {
        runCommand( command, host, gitCommand, true );
    }


    private void runCommand( Command command, Agent host, GitCommand gitCommand, boolean output ) throws GitException
    {
        commandRunner.runCommand( command );

        if ( !command.hasSucceeded() )
        {
            if ( command.hasCompleted() )
            {
                AgentResult agentResult = command.getResults().get( host.getUuid() );
                throw new GitException(
                        String.format( "Error while performing [git %s]: %s%n%s, exit code %s", gitCommand.getCommand(),
                                agentResult.getStdOut(), agentResult.getStdErr(), agentResult.getExitCode() ) );
            }
            else
            {
                throw new GitException( String.format( "Error while performing [git %s]: Command timed out",
                        gitCommand.getCommand() ) );
            }
        }
        else if ( output )
        {
            AgentResult agentResult = command.getResults().get( host.getUuid() );
        }
    }


    /**
     * Prepares specified files for commit
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to prepare for commit
     */
    @Override
    public void add( final Agent host, final String repositoryRoot, final List<String> filePaths ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( filePaths != null && !filePaths.isEmpty(), "Files is null or empty" );

        Command addCommand = commandRunner
                .createCommand( new RequestBuilder( "git add" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ),
                        Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.ADD );
    }


    /**
     * Prepares all files in repo for commit
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     */
    public void addAll( final Agent host, final String repositoryRoot ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );

        Command addCommand = commandRunner
                .createCommand( new RequestBuilder( "git add -A" ).withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.ADD );
    }


    /**
     * Deletes specified files from repo
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to prepare for commit
     */
    @Override
    public void delete( final Agent host, final String repositoryRoot, final List<String> filePaths )
            throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( filePaths != null && !filePaths.isEmpty(), "Files is null or empty" );

        Command addCommand = commandRunner
                .createCommand( new RequestBuilder( "git rm" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ),
                        Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.DELETE );
    }


    /**
     * Commits specified files
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to prepare for commit
     * @param message - commit message
     * @param afterConflictResolved - indicates if this commit is done after conflict resolution
     *
     * @return - commit id {@code String}
     */
    @Override
    public String commit( final Agent host, final String repositoryRoot, final List<String> filePaths,
                          final String message, boolean afterConflictResolved ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( filePaths != null && !filePaths.isEmpty(), "Files is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( message ), "Message is null or empty" );

        Command addCommand = commandRunner.createCommand( new RequestBuilder(
                String.format( "git commit -m \"%s\" %s", message, afterConflictResolved ? "-i" : "" ) )
                .withCwd( repositoryRoot ).withCmdArgs( filePaths ), Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.COMMIT, false );
        //parse output to get commitAll id here
        Pattern p = Pattern.compile( "(\\w+)]" );
        Matcher m = p.matcher( addCommand.getResults().get( host.getUuid() ).getStdOut() );

        if ( m.find() )
        {
            return m.group( 1 );
        }

        return null;
    }


    /**
     * Commits all files in repo
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param message -  commit message
     *
     * @return - commit id {@code String}
     */
    @Override
    public String commitAll( final Agent host, final String repositoryRoot, final String message ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( message ), "Message is null or empty" );

        Command addCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git commit -a -m \"%s\"", message ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.COMMIT, false );

        //parse output to get commitAll id here
        Pattern p = Pattern.compile( "(\\w+)]" );
        Matcher m = p.matcher( addCommand.getResults().get( host.getUuid() ).getStdOut() );

        if ( m.find() )
        {
            return m.group( 1 );
        }

        return null;
    }


    /**
     * Clones repo from remote master branch
     *
     * @param host - agent of node
     * @param newBranchName - branch name to create
     * @param targetDir - target directory for the repo
     */
    @Override
    public void clone( final Agent host, final String newBranchName, final String targetDir ) throws GitException
    {
        validateHostNRepoRoot( host, targetDir );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newBranchName ), "Branch name is null or empty" );

        Command cloneCommand = commandRunner.createCommand( new RequestBuilder(
                String.format( "git clone -b %s %s %s", newBranchName, Common.GIT_REPO_URL, targetDir ) )
                .withTimeout( 180 ), Sets.newHashSet( host ) );

        runCommand( cloneCommand, host, GitCommand.CLONE );
    }


    /**
     * Switches to branch or creates new local branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName - branch name
     * @param create - true: create new local branch; false: switch to the specified branch
     */
    @Override
    public void checkout( final Agent host, final String repositoryRoot, final String branchName, final boolean create )
            throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), "Branch name is null or empty" );

        String command = create ? String.format( "git checkout --track -b %s", branchName ) :
                         String.format( "git checkout %s", branchName );
        Command checkoutCommand = commandRunner
                .createCommand( new RequestBuilder( command ).withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( checkoutCommand, host, GitCommand.CHECKOUT );
    }


    /**
     * Delete local branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName - branch name
     */
    @Override
    public void deleteBranch( final Agent host, final String repositoryRoot, final String branchName )
            throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), "Branch name is null or empty" );

        Command checkoutCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git branch -d %s", branchName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( checkoutCommand, host, GitCommand.BRANCH );
    }


    /**
     * Merges current branch with master branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     */
    @Override
    public void merge( final Agent host, final String repositoryRoot ) throws GitException
    {
        merge( host, repositoryRoot, MASTER_BRANCH );
    }


    /**
     * Merges current branch with specified branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repot
     * @param branchName - branch name
     */
    @Override
    public void merge( final Agent host, final String repositoryRoot, final String branchName ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), "Branch name is null or empty" );

        Command mergeCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git merge %s", branchName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( mergeCommand, host, GitCommand.MERGE );
    }


    /**
     * Pulls from remote branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName - branch name to pull from
     */
    @Override
    public void pull( final Agent host, final String repositoryRoot, final String branchName ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), "Branch name is null or empty" );

        Command pullCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git pull origin %s", branchName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( pullCommand, host, GitCommand.PULL );
    }


    /**
     * Pulls from remote master branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     */
    @Override
    public void pull( final Agent host, final String repositoryRoot ) throws GitException
    {
        pull( host, repositoryRoot, MASTER_BRANCH );
    }


    /**
     * Return current branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     *
     * @return - current branch  {@code GitBranch}
     */
    @Override
    public GitBranch currentBranch( final Agent host, final String repositoryRoot ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );

        Command branchCommand = commandRunner
                .createCommand( new RequestBuilder( "git branch" ).withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( branchCommand, host, GitCommand.BRANCH, false );

        branchCommand.getResults().get( host.getUuid() ).getStdOut();

        StringTokenizer lines =
                new StringTokenizer( branchCommand.getResults().get( host.getUuid() ).getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            if ( line != null && line.startsWith( "*" ) )
            {
                return new GitBranch( line.substring( 2 ), true );
            }
        }

        return new GitBranch( MASTER_BRANCH, true );
    }


    /**
     * Returns list of branches in the repo
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param remote - true: return remote branches; false: return local branches
     *
     * @return - list of branches {@code List}
     */
    @Override
    public List<GitBranch> listBranches( final Agent host, final String repositoryRoot, boolean remote )
            throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );

        List<GitBranch> branches = new LinkedList<>();

        Command branchCommand = commandRunner
                .createCommand( new RequestBuilder( remote ? "git branch -r" : "git branch" ).withCwd( repositoryRoot ),
                        Sets.newHashSet( host ) );

        runCommand( branchCommand, host, GitCommand.BRANCH, false );

        StringTokenizer lines =
                new StringTokenizer( branchCommand.getResults().get( host.getUuid() ).getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            if ( line != null )
            {
                branches.add( new GitBranch( line.substring( 2 ), line.startsWith( "*" ) ) );
            }
        }

        return branches;
    }


    /**
     * Pushes to remote branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName - branch name to push to
     */
    @Override
    public void push( final Agent host, final String repositoryRoot, final String branchName ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), "Branch name is null or empty" );

        if ( branchName.toLowerCase().contains( MASTER_BRANCH ) )
        {
            throw new GitException( "Can not perform push to remote master branch" );
        }

        Command pushCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git push origin %s", branchName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( pushCommand, host, GitCommand.PUSH );
    }


    /**
     * Undoes all uncommitted changes to specified files
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to undo changes to
     */
    @Override
    public void undoSoft( final Agent host, final String repositoryRoot, final List<String> filePaths )
            throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( filePaths != null && !filePaths.isEmpty(), "Files is null or empty" );

        Command undoCommand = commandRunner.createCommand(
                new RequestBuilder( "git checkout --" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ),
                Sets.newHashSet( host ) );

        runCommand( undoCommand, host, GitCommand.CHECKOUT );
    }


    /**
     * Brings current branch to the state of the specified remote branch, effectively undoing all local changes
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName - remote branch whose state to restore current branch to
     */
    @Override
    public void undoHard( final Agent host, final String repositoryRoot, final String branchName ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), "Branch name is null or empty" );

        Command undoCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git fetch origin && git reset --hard origin/%s", branchName ) )
                        .withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( undoCommand, host, GitCommand.FETCH );
    }


    /**
     * Brings current branch to the state of remote master branch, effectively undoing all local changes
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     */
    @Override
    public void undoHard( final Agent host, final String repositoryRoot ) throws GitException
    {

        undoHard( host, repositoryRoot, MASTER_BRANCH );
    }


    /**
     * Reverts the specified commit
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param commitId - commit id to revert
     */
    @Override
    public void revertCommit( final Agent host, final String repositoryRoot, String commitId ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( commitId ), "Commit Id is null or empty" );

        Command revertCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git revert %s", commitId ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( revertCommand, host, GitCommand.REVERT );
    }


    /**
     * Stashes all changes in current branch and reverts it to HEAD commit
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     */
    @Override
    public void stash( final Agent host, final String repositoryRoot ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );

        Command stashCommand = commandRunner
                .createCommand( new RequestBuilder( String.format( "git stash" ) ).withCwd( repositoryRoot ),
                        Sets.newHashSet( host ) );

        runCommand( stashCommand, host, GitCommand.STASH );
    }


    /**
     * Applies all stashed changes to current branch
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     * @param stashName - name of stash to apply
     */
    @Override
    public void unstash( final Agent host, final String repositoryRoot, final String stashName ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( stashName ), "Stash name is null or empty" );

        Command stashCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git stash apply %s", stashName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( stashCommand, host, GitCommand.STASH );
    }


    /**
     * Returns list of stashes in the repo
     *
     * @param host - agent of node
     * @param repositoryRoot - path to repo
     *
     * @return - list of stashes {@code List}
     */
    @Override
    public List<String> listStashes( final Agent host, final String repositoryRoot ) throws GitException
    {
        validateHostNRepoRoot( host, repositoryRoot );

        List<String> stashes = new LinkedList<>();

        Command stashCommand = commandRunner
                .createCommand( new RequestBuilder( "git stash list" ).withCwd( repositoryRoot ),
                        Sets.newHashSet( host ) );

        runCommand( stashCommand, host, GitCommand.STASH, false );

        StringTokenizer tok =
                new StringTokenizer( stashCommand.getResults().get( host.getUuid() ).getStdOut(), LINE_SEPARATOR );

        while ( tok.hasMoreTokens() )
        {
            stashes.add( tok.nextToken() );
        }

        return stashes;
    }
}
