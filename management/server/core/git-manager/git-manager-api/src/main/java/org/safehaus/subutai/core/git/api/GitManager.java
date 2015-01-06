package org.safehaus.subutai.core.git.api;


import java.util.List;


/**
 * This class executes git commands on agents.
 */
public interface GitManager
{

    /**
     * Returns list of files changed between specified branches
     *
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     * @param branchName2 - name of branch 2
     *
     * @return - list of {@code GitChangedFile}
     */
    public List<GitChangedFile> diffBranches( String repositoryRoot, String branchName1, String branchName2 )
            throws GitException;

    /**
     * Returns list of files changed between specified branch and master branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     *
     * @return - list of {@code GitChangedFile}
     */
    public List<GitChangedFile> diffBranches( String repositoryRoot, String branchName1 ) throws GitException;

    /**
     * Returns diff in file between specified branch and master branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName1 - name of branch 1
     * @param filePath - relative (to repo root) file path
     */
    public String diffFile( String repositoryRoot, String branchName1, String filePath ) throws GitException;

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
    public String diffFile( String repositoryRoot, String branchName1, String branchName2, String filePath )
            throws GitException;


    /**
     * Returns diff in file between specified branches
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name
     * @param filePath - relative (to repo root) file path
     *
     * @return - differences in file {@code String}
     */
    public String showFile( String repositoryRoot, String branchName, String filePath ) throws GitException;


    /**
     * Initializes empty git repo in the specified directory
     *
     * @param repositoryRoot - path to repo
     */
    public void init( String repositoryRoot ) throws GitException;

    /**
     * Prepares specified files for commit
     *
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to prepare for commit
     */
    public void add( String repositoryRoot, List<String> filePaths ) throws GitException;

    /**
     * Prepares all files in repo for commit
     *
     * @param repositoryRoot - path to repo
     */
    public void addAll( String repositoryRoot ) throws GitException;

    /**
     * Deletes specified files from repo
     *
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to prepare for commit
     */
    public void delete( String repositoryRoot, List<String> filePaths ) throws GitException;

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
    public String commit( String repositoryRoot, List<String> filePaths, String message, boolean afterConflictResolved )
            throws GitException;

    /**
     * Commits all files in repo
     *
     * @param repositoryRoot - path to repo
     * @param message -  commit message
     *
     * @return - commit id {@code String}
     */
    public String commitAll( String repositoryRoot, String message ) throws GitException;

    /**
     * Clones repo from remote master branch
     *
     * @param newBranchName - branch name to create
     * @param targetDir - target directory for the repo
     */
    public void clone( String newBranchName, String targetDir ) throws GitException;

    /**
     * Switches to branch or creates new local branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name
     * @param create - true: create new local branch; false: switch to the specified branch
     */
    public void checkout( String repositoryRoot, String branchName, boolean create ) throws GitException;

    /**
     * Delete local branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name
     */
    public void deleteBranch( String repositoryRoot, String branchName ) throws GitException;

    /**
     * Merges current branch with master branch
     *
     * @param repositoryRoot - path to repo
     */
    public void merge( String repositoryRoot ) throws GitException;

    /**
     * Merges current branch with specified branch
     *
     * @param repositoryRoot - path to repot
     * @param branchName - branch name
     */
    public void merge( String repositoryRoot, String branchName ) throws GitException;

    /**
     * Pulls from remote branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name to pull from
     */
    public void pull( String repositoryRoot, String branchName ) throws GitException;

    /**
     * Pulls from remote master branch
     *
     * @param repositoryRoot - path to repo
     */
    public void pull( String repositoryRoot ) throws GitException;

    /**
     * Return current branch
     *
     * @param repositoryRoot - path to repo
     *
     * @return - current branch  {@code GitBranch}
     */
    public GitBranch currentBranch( String repositoryRoot ) throws GitException;

    /**
     * Returns list of branches in the repo
     *
     * @param repositoryRoot - path to repo
     * @param remote - true: return remote branches; false: return local branches
     *
     * @return - list of branches {@code List}
     */
    public List<GitBranch> listBranches( String repositoryRoot, boolean remote ) throws GitException;

    /**
     * Pushes to remote branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName - branch name to push to
     */
    public void push( String repositoryRoot, String branchName ) throws GitException;

    /**
     * Undoes all uncommitted changes to specified files
     *
     * @param repositoryRoot - path to repo
     * @param filePaths - paths to files to undo changes to
     */
    public void undoSoft( String repositoryRoot, List<String> filePaths ) throws GitException;

    /**
     * Brings current branch to the state of the specified remote branch, effectively undoing all local changes
     *
     * @param repositoryRoot - path to repo
     * @param branchName - remote branch whose state to restore current branch to
     */
    public void undoHard( String repositoryRoot, String branchName ) throws GitException;

    /**
     * Brings current branch to the state of remote master branch, effectively undoing all local changes
     *
     * @param repositoryRoot - path to repo
     */
    public void undoHard( String repositoryRoot ) throws GitException;

    /**
     * Reverts the specified commit
     *
     * @param repositoryRoot - path to repo
     * @param commitId - commit id to revert
     */
    public void revertCommit( String repositoryRoot, String commitId ) throws GitException;

    /**
     * Stashes all changes in current branch and reverts it to HEAD commit
     *
     * @param repositoryRoot - path to repo
     */
    public void stash( String repositoryRoot ) throws GitException;

    /**
     * Applies all stashed changes to current branch
     *
     * @param repositoryRoot - path to repo
     * @param stashName - name of stash to apply
     */
    public void unstash( String repositoryRoot, String stashName ) throws GitException;

    /**
     * Returns list of stashes in the repo
     *
     * @param repositoryRoot - path to repo
     *
     * @return - list of stashes {@code List}
     */
    public List<String> listStashes( String repositoryRoot ) throws GitException;
}
