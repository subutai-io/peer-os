package org.safehaus.subutai.core.git.api;


import org.safehaus.subutai.common.protocol.Agent;

import java.util.List;


/**
 * This class executes git commands on agents.
 */
public interface GitManager
{

    /**
     * Returns list of files changed between specified branches
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName1    - name of branch 1
     * @param branchName2    - name of branch 2
     * @return - list of {@code GitChangedFile}
     */
    public List<GitChangedFile> diffBranches( Agent host, String repositoryRoot, String branchName1,
        String branchName2 ) throws GitException;

    /**
     * Returns list of files changed between specified branch and master branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName1    - name of branch 1
     * @return - list of {@code GitChangedFile}
     */
    public List<GitChangedFile> diffBranches( Agent host, String repositoryRoot, String branchName1 )
        throws GitException;

    /**
     * Returns diff in file between specified branch and master branch
     *
     * @param repositoryRoot - path to repo
     * @param branchName1    - name of branch 1
     * @param filePath       - relative (to repo root) file path
     */
    public String diffFile( Agent host, String repositoryRoot, String branchName1, String filePath )
        throws GitException;

    /**
     * Returns diff in file between specified branches
     *
     * @param repositoryRoot - path to repo
     * @param branchName1    - name of branch 1
     * @param branchName2    - name of branch 2
     * @param filePath       - relative (to repo root) file path
     * @return - differences in file {@code String}
     */
    public String diffFile( Agent host, String repositoryRoot, String branchName1, String branchName2, String filePath )
        throws GitException;

    /**
     * Initializes empty git repo in the specified directory
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     */
    public void init( Agent host, String repositoryRoot ) throws GitException;

    /**
     * Prepares specified files for commit
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param filePaths      - paths to files to prepare for commit
     */
    public void add( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    /**
     * Prepares all files in repo for commit
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     */
    public void addAll( Agent host, String repositoryRoot ) throws GitException;

    /**
     * Deletes specified files from repo
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param filePaths      - paths to files to prepare for commit
     */
    public void delete( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    /**
     * Commits specified files
     *
     * @param host                  - agent of node
     * @param repositoryRoot        - path to repo
     * @param filePaths             - paths to files to prepare for commit
     * @param message               - commit message
     * @param afterConflictResolved - indicates if this commit is done after conflict resolution
     * @return - commit id {@code String}
     */
    public String commit( Agent host, String repositoryRoot, List<String> filePaths, String message,
        boolean afterConflictResolved ) throws GitException;

    /**
     * Commits all files in repo
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param message        -  commit message
     * @return - commit id {@code String}
     */
    public String commitAll( Agent host, String repositoryRoot, String message ) throws GitException;

    /**
     * Clones repo from remote master branch
     *
     * @param host          - agent of node
     * @param newBranchName - branch name to create
     * @param targetDir     - target directory for the repo
     */
    public void clone( Agent host, String newBranchName, String targetDir ) throws GitException;

    /**
     * Switches to branch or creates new local branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName     - branch name
     * @param create         - true: create new local branch; false: switch to the specified branch
     */
    public void checkout( Agent host, String repositoryRoot, String branchName, boolean create ) throws GitException;

    /**
     * Delete local branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName     - branch name
     */
    public void deleteBranch( Agent host, String repositoryRoot, String branchName ) throws GitException;

    /**
     * Merges current branch with master branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     */
    public void merge( Agent host, String repositoryRoot ) throws GitException;

    /**
     * Merges current branch with specified branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repot
     * @param branchName     - branch name
     */
    public void merge( Agent host, String repositoryRoot, String branchName ) throws GitException;

    /**
     * Pulls from remote branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName     - branch name to pull from
     */
    public void pull( Agent host, String repositoryRoot, String branchName ) throws GitException;

    /**
     * Pulls from remote master branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     */
    public void pull( Agent host, String repositoryRoot ) throws GitException;

    /**
     * Return current branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @return - current branch  {@code GitBranch}
     */
    public GitBranch currentBranch( Agent host, String repositoryRoot ) throws GitException;

    /**
     * Returns list of branches in the repo
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param remote         - true: return remote branches; false: return local branches
     * @return - list of branches {@code List}
     */
    public List<GitBranch> listBranches( Agent host, String repositoryRoot, boolean remote ) throws GitException;

    /**
     * Pushes to remote branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName     - branch name to push to
     */
    public void push( Agent host, String repositoryRoot, String branchName ) throws GitException;

    /**
     * Undoes all uncommitted changes to specified files
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param filePaths      - paths to files to undo changes to
     */
    public void undoSoft( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    /**
     * Brings current branch to the state of the specified remote branch, effectively undoing all local changes
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param branchName     - remote branch whose state to restore current branch to
     */
    public void undoHard( Agent host, String repositoryRoot, String branchName ) throws GitException;

    /**
     * Brings current branch to the state of remote master branch, effectively undoing all local changes
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     */
    public void undoHard( Agent host, String repositoryRoot ) throws GitException;

    /**
     * Reverts the specified commit
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param commitId       - commit id to revert
     */
    public void revertCommit( Agent host, String repositoryRoot, String commitId ) throws GitException;

    /**
     * Stashes all changes in current branch and reverts it to HEAD commit
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     */
    public void stash( Agent host, String repositoryRoot ) throws GitException;

    /**
     * Applies all stashed changes to current branch
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @param stashName      - name of stash to apply
     */
    public void unstash( Agent host, String repositoryRoot, String stashName ) throws GitException;

    /**
     * Returns list of stashes in the repo
     *
     * @param host           - agent of node
     * @param repositoryRoot - path to repo
     * @return - list of stashes {@code List}
     */
    public List<String> listStashes( Agent host, String repositoryRoot ) throws GitException;
}
