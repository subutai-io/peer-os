package org.safehaus.subutai.api.gitmanager;


import java.util.List;

import org.safehaus.subutai.shared.protocol.Agent;


/**
 * This class executes git commands on agents.
 *
 * TODO: add wrapper methods using specific default location at mgmt server as repositoryRoot
 */
public interface GitManager {

    public void add( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    public void delete( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    public void commit( Agent host, String repositoryRoot, List<String> filePaths, String message ) throws GitException;

    public void commit( Agent host, String repositoryRoot, String message ) throws GitException;

    public void clone( Agent host, String repositoryRoot, String src, String targetDir ) throws GitException;

    public void clone( Agent host, String repositoryRoot, String src, String targetDir, String newBranchName )
            throws GitException;

    public void checkout( Agent host, String repositoryRoot, String branchName, boolean create ) throws GitException;

    public void merge( Agent host, String repositoryRoot ) throws GitException;

    public void merge( Agent host, String repositoryRoot, String branchName ) throws GitException;

    public void pull( Agent host, String repositoryRoot, String repoURL ) throws GitException;

    public void fetch( Agent host, String repositoryRoot, String repoURL ) throws GitException;

    public GitBranch currentBranch( Agent host, String repositoryRoot ) throws GitException;

    public List<GitBranch> listBranches( Agent host, String repositoryRoot ) throws GitException;

    public void push( Agent host, String repositoryRoot ) throws GitException;

    public void revert( Agent host, String repositoryRoot, List<String> filePaths ) throws GitException;

    public void revert( Agent host, String repositoryRoot ) throws GitException;
}
