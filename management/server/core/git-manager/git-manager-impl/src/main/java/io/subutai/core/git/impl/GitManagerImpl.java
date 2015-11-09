package io.subutai.core.git.impl;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.git.api.GitBranch;
import io.subutai.core.git.api.GitChangedFile;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitFileStatus;
import io.subutai.core.git.api.GitManager;
import io.subutai.common.peer.ManagementHost;
import io.subutai.core.peer.api.PeerManager;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This is an implementation of GitManager interface
 */
public class GitManagerImpl implements GitManager
{

    private static final String MASTER_BRANCH = "master";
    private static final String LINE_SEPARATOR = "\n";
    private static final String FILES_IS_EMPTY_MSG = "Files is null or empty";
    private static final String BRANCH_NAME_IS_EMPTY_MSG = "Branch name is null or empty";

    private PeerManager peerManager;
    protected CommandUtil commandUtil;


    public GitManagerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager, "Peer manager is null" );

        this.peerManager = peerManager;
        this.commandUtil = new CommandUtil();
    }


    private void validateRepoUrl( String repositoryRoot )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( repositoryRoot ), "Repository root is null or empty" );
    }


    /**
     * Returns list of files changed between specified branch and master branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     *
     * @return - list of {@code GitChangedFile}
     */
    @Override
    public List<GitChangedFile> diffBranches( final String repositoryRoot, final String branchName1 )
            throws GitException
    {
        return diffBranches( repositoryRoot, branchName1, MASTER_BRANCH );
    }


    /**
     * Returns list of files changed between specified branches
     *
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     * @param branchName2 - name of branch 2
     *
     * @return - list of {@code GitChangedFile}
     */
    @Override
    public List<GitChangedFile> diffBranches( final String repositoryRoot, final String branchName1,
                                              final String branchName2 ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName1 ), "Branch name 1 is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName2 ), "Branch name 2 is null or empty" );

        CommandResult result = execute(
                new RequestBuilder( String.format( "git diff --name-status %s %s", branchName1, branchName2 ) )
                        .withCwd( repositoryRoot ), false );

        StringTokenizer lines = new StringTokenizer( result.getStdOut(), "\n" );

        List<GitChangedFile> gitChangedFiles = new ArrayList<>();

        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            String[] ss = line.split( "\\s+" );
            if ( ss.length == 2 )
            {
                gitChangedFiles.add( new GitChangedFile( GitFileStatus.parse( ss[0] ), ss[1] ) );
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
    public String diffFile( final String repositoryRoot, final String branchName1, final String filePath )
            throws GitException
    {
        return diffFile( repositoryRoot, branchName1, MASTER_BRANCH, filePath );
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
    public String diffFile( final String repositoryRoot, final String branchName1, final String branchName2,
                            final String filePath ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName1 ), "Branch name 1 is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName2 ), "Branch name 2 is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( filePath ), "File path is null or empty" );

        CommandResult result = execute( new RequestBuilder(
                        String.format( "git diff -U10000 %s %s -- %s", branchName1, branchName2, filePath ) )
                        .withCwd( repositoryRoot ), false );

        return result.getStdOut();
    }


    /**
     * Returns diff in file between specified branches
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name
     * @param filePath - relative (to repo root) file path
     *
     * @return - differences in file {@code String}
     */
    @Override
    public String showFile( final String repositoryRoot, final String branchName, final String filePath )
            throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), "Branch name 1 is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( filePath ), "File path is null or empty" );

        CommandResult result = execute( new RequestBuilder( String.format( "git show %s:%s", branchName, filePath ) )
                        .withCwd( repositoryRoot ), false );

        return result.getStdOut();
    }


    /**
     * Initializes empty git repo in the specified directory
     *
     * @param repositoryRoot - path to repo
     */
    @Override
    public void init( final String repositoryRoot ) throws GitException
    {
        validateRepoUrl( repositoryRoot );

        execute( new RequestBuilder( "git init" ).withCwd( repositoryRoot ), true );
    }


    /**
     * Prepares specified files for commit
     *
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to prepare for commit
     */
    @Override
    public void add( final String repositoryRoot, final List<String> filePaths ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( filePaths ), FILES_IS_EMPTY_MSG );

        execute( new RequestBuilder( "git add" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ), true );
    }


    /**
     * Prepares all files in repo for commit
     *
     * @param repositoryRoot - path to repo
     */
    public void addAll( final String repositoryRoot ) throws GitException
    {
        validateRepoUrl( repositoryRoot );

        execute( new RequestBuilder( "git add -A" ).withCwd( repositoryRoot ), true );
    }


    /**
     * Deletes specified files from repo
     *
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to prepare for commit
     */
    @Override
    public void delete( final String repositoryRoot, final List<String> filePaths ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( filePaths ), FILES_IS_EMPTY_MSG );

        execute( new RequestBuilder( "git rm" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ), true );
    }


    /**
     * Commits specified files
     *
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to prepare for commit
     * @param message - commit message
     * @param afterConflictResolved - indicates if this commit is done after conflict resolution
     *
     * @return - commit id {@code String}
     */
    @Override
    public String commit( final String repositoryRoot, final List<String> filePaths, final String message,
                          boolean afterConflictResolved ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( message ), "Message is null or empty" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( filePaths ), FILES_IS_EMPTY_MSG );


        CommandResult result = execute( new RequestBuilder(
                String.format( "git commit -m \"%s\" %s", message, afterConflictResolved ? "-i" : "" ) )
                .withCwd( repositoryRoot ).withCmdArgs( filePaths ), false );

        //parse output to get commitAll id here
        Pattern p = Pattern.compile( "(\\w+)]" );
        Matcher m = p.matcher( result.getStdOut() );

        if ( m.find() )
        {
            return m.group( 1 );
        }

        throw new GitException( "Could not get commit id from command result" );
    }


    /**
     * Commits all files in repo
     *
     * @param repositoryRoot - path to repo
     * @param message -  commit message
     *
     * @return - commit id {@code String}
     */
    @Override
    public String commitAll( final String repositoryRoot, final String message ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( message ), "Message is null or empty" );

        CommandResult result = execute(
                new RequestBuilder( String.format( "git commit -a -m \"%s\"", message ) ).withCwd( repositoryRoot ),
                false );

        //parse output to get commitAll id here
        Pattern p = Pattern.compile( "(\\w+)]" );
        Matcher m = p.matcher( result.getStdOut() );

        if ( m.find() )
        {
            return m.group( 1 );
        }

        throw new GitException( "Could not get commit id from command result" );
    }


    /**
     * Clones repo from remote master branch
     *
     * @param newBranchName - branch name to create
     * @param targetDir - target directory for the repo
     */
    @Override
    public void clone( final String newBranchName, final String targetDir ) throws GitException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newBranchName ), BRANCH_NAME_IS_EMPTY_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newBranchName ), "Target directory is null or empty" );

        execute( new RequestBuilder(
                String.format( "git clone -b %s %s %s", newBranchName, Common.GIT_REPO_URL, targetDir ) )
                .withTimeout( 180 ), true );
    }


    /**
     * Switches to branch or creates new local branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name
     * @param create - true: create new local branch; false: switch to the specified branch
     */
    @Override
    public void checkout( final String repositoryRoot, final String branchName, final boolean create )
            throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), BRANCH_NAME_IS_EMPTY_MSG );

        String command = create ? String.format( "git checkout --track -b %s", branchName ) :
                         String.format( "git checkout %s", branchName );

        execute( new RequestBuilder( command ).withCwd( repositoryRoot ), true );
    }


    /**
     * Delete local branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name
     */
    @Override
    public void deleteBranch( final String repositoryRoot, final String branchName ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), BRANCH_NAME_IS_EMPTY_MSG );

        execute( new RequestBuilder( String.format( "git branch -d %s", branchName ) ).withCwd( repositoryRoot ),
                true );
    }


    /**
     * Merges current branch with master branch
     *
     * @param repositoryRoot - path to repo
     */
    @Override
    public void merge( final String repositoryRoot ) throws GitException
    {
        merge( repositoryRoot, MASTER_BRANCH );
    }


    /**
     * Merges current branch with specified branch
     *
     * @param repositoryRoot - path to repot
     * @param branchName - branch name
     */
    @Override
    public void merge( final String repositoryRoot, final String branchName ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), BRANCH_NAME_IS_EMPTY_MSG );

        execute( new RequestBuilder( String.format( "git merge %s", branchName ) ).withCwd( repositoryRoot ), true );
    }


    /**
     * Pulls from remote branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name to pull from
     */
    @Override
    public void pull( final String repositoryRoot, final String branchName ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), BRANCH_NAME_IS_EMPTY_MSG );

        execute( new RequestBuilder( String.format( "git pull origin %s", branchName ) ).withCwd( repositoryRoot ),
                true );
    }


    /**
     * Pulls from remote master branch
     *
     * @param repositoryRoot - path to repo
     */
    @Override
    public void pull( final String repositoryRoot ) throws GitException
    {
        pull( repositoryRoot, MASTER_BRANCH );
    }


    /**
     * Return current branch
     *
     * @param repositoryRoot - path to repo
     *
     * @return - current branch  {@code GitBranch}
     */
    @Override
    public GitBranch currentBranch( final String repositoryRoot ) throws GitException
    {
        validateRepoUrl( repositoryRoot );


        CommandResult result = execute( new RequestBuilder( "git branch" ).withCwd( repositoryRoot ), false );

        StringTokenizer lines = new StringTokenizer( result.getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            if ( line.startsWith( "*" ) )
            {
                return new GitBranch( line.substring( 2 ), true );
            }
        }

        return new GitBranch( MASTER_BRANCH, true );
    }


    /**
     * Returns list of branches in the repo
     *
     * @param repositoryRoot - path to repo
     * @param remote - true: return remote branches; false: return local branches
     *
     * @return - list of branches {@code List}
     */
    @Override
    public List<GitBranch> listBranches( final String repositoryRoot, boolean remote ) throws GitException
    {
        validateRepoUrl( repositoryRoot );

        List<GitBranch> branches = new LinkedList<>();


        CommandResult result =
                execute( new RequestBuilder( remote ? "git branch -r" : "git branch" ).withCwd( repositoryRoot ),
                        false );

        StringTokenizer lines = new StringTokenizer( result.getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            branches.add( new GitBranch( line.substring( 2 ), line.startsWith( "*" ) ) );
        }

        return branches;
    }


    /**
     * Pushes to remote branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name to push to
     */
    @Override
    public void push( final String repositoryRoot, final String branchName ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), BRANCH_NAME_IS_EMPTY_MSG );

        if ( branchName.toLowerCase().contains( MASTER_BRANCH ) )
        {
            throw new GitException( "Can not perform push to remote master branch" );
        }

        execute( new RequestBuilder( String.format( "git push origin %s", branchName ) ).withCwd( repositoryRoot ),
                true );
    }


    /**
     * Undoes all uncommitted changes to specified files
     *
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to undo changes to
     */
    @Override
    public void undoSoft( final String repositoryRoot, final List<String> filePaths ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( filePaths ), FILES_IS_EMPTY_MSG );

        execute( new RequestBuilder( "git checkout --" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ), true );
    }


    /**
     * Brings current branch to the state of the specified remote branch, effectively undoing all local changes
     *
     * @param repositoryRoot - path to repo
     * @param branchName - remote branch whose state to restore current branch to
     */
    @Override
    public void undoHard( final String repositoryRoot, final String branchName ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branchName ), BRANCH_NAME_IS_EMPTY_MSG );

        execute( new RequestBuilder( String.format( "git fetch origin && git reset --hard origin/%s", branchName ) )
                .withCwd( repositoryRoot ), true );
    }


    /**
     * Brings current branch to the state of remote master branch, effectively undoing all local changes
     *
     * @param repositoryRoot - path to repo
     */
    @Override
    public void undoHard( final String repositoryRoot ) throws GitException
    {
        undoHard( repositoryRoot, MASTER_BRANCH );
    }


    /**
     * Reverts the specified commit
     *
     * @param repositoryRoot - path to repo
     * @param commitId - commit id to revert
     */
    @Override
    public void revertCommit( final String repositoryRoot, String commitId ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( commitId ), "Commit Id is null or empty" );

        execute( new RequestBuilder( String.format( "git revert %s", commitId ) ).withCwd( repositoryRoot ), true );
    }


    /**
     * Stashes all changes in current branch and reverts it to HEAD commit
     *
     * @param repositoryRoot - path to repo
     */
    @Override
    public void stash( final String repositoryRoot ) throws GitException
    {
        validateRepoUrl( repositoryRoot );

        execute( new RequestBuilder( String.format( "git stash" ) ).withCwd( repositoryRoot ), true );
    }


    /**
     * Applies all stashed changes to current branch
     *
     * @param repositoryRoot - path to repo
     * @param stashName - name of stash to apply
     */
    @Override
    public void unstash( final String repositoryRoot, final String stashName ) throws GitException
    {
        validateRepoUrl( repositoryRoot );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( stashName ), "Stash name is null or empty" );

        execute( new RequestBuilder( String.format( "git stash apply %s", stashName ) ).withCwd( repositoryRoot ),
                true );
    }


    /**
     * Returns list of stashes in the repo
     *
     * @param repositoryRoot - path to repo
     *
     * @return - list of stashes {@code List}
     */
    @Override
    public List<String> listStashes( final String repositoryRoot ) throws GitException
    {
        validateRepoUrl( repositoryRoot );

        List<String> stashes = new LinkedList<>();

        CommandResult result = execute( new RequestBuilder( "git stash list" ).withCwd( repositoryRoot ), false );

        StringTokenizer tok = new StringTokenizer( result.getStdOut(), LINE_SEPARATOR );

        while ( tok.hasMoreTokens() )
        {
            stashes.add( tok.nextToken() );
        }

        return stashes;
    }


    public CommandResult execute( RequestBuilder command, boolean output ) throws GitException
    {
        try
        {
            CommandResult result = commandUtil.execute( command, getManagementHost() );

            if ( output )
            {
                System.out.println( result.getStdOut() );
            }

            return result;
        }
        catch ( CommandException e )
        {
            throw new GitException( e );
        }
    }


    public ManagementHost getManagementHost() throws GitException
    {
        try
        {
            return peerManager.getLocalPeer().getManagementHost();
        }
        catch ( PeerException e )
        {
            throw new GitException( e );
        }
    }
}
