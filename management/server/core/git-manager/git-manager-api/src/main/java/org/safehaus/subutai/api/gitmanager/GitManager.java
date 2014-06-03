package org.safehaus.subutai.api.gitmanager;


import java.util.List;

import org.safehaus.subutai.shared.protocol.Agent;


/**
 * This class executes git commands on agents.
 */
public interface GitManager {

    public void init( Agent host, String repositoryRoot ) throws GitException;

    public void add( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    public void addAll( Agent host, String repositoryRoot ) throws GitException;

    public void delete( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    public String commit( Agent host, String repositoryRoot, List<String> filePaths, String message,
                          boolean afterConflictResolved ) throws GitException;

    public String commitAll( Agent host, String repositoryRoot, String message ) throws GitException;

    public void clone( Agent host, String newBranchName, String targetDir ) throws GitException;

    public void checkout( Agent host, String repositoryRoot, String branchName, boolean create ) throws GitException;

    public void deleteBranch( Agent host, String repositoryRoot, String branchName ) throws GitException;

    public void merge( Agent host, String repositoryRoot ) throws GitException;

    public void merge( Agent host, String repositoryRoot, String branchName ) throws GitException;

    public void pull( Agent host, String repositoryRoot, String branchName ) throws GitException;

    public void pull( Agent host, String repositoryRoot ) throws GitException;

    public GitBranch currentBranch( Agent host, String repositoryRoot ) throws GitException;

    public List<GitBranch> listBranches( Agent host, String repositoryRoot, boolean remote ) throws GitException;

    public void push( Agent host, String repositoryRoot, String branchName ) throws GitException;

    public void undoSoft( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    public void undoHard( Agent host, String repositoryRoot, String branchName ) throws GitException;

    public void revertCommit( Agent host, String repositoryRoot, String commitId ) throws GitException;

    public void stash( Agent host, String repositoryRoot, String stashName ) throws GitException;

    public void unstash( Agent host, String repositoryRoot, String stashName ) throws GitException;

    public List<String> listStashes( Agent host, String repositoryRoot ) throws GitException;
}
