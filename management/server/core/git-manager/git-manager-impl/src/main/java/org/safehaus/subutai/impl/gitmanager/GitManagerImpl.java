package org.safehaus.subutai.impl.gitmanager;


import java.util.List;

import org.safehaus.subutai.api.gitmanager.GitBranch;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * This is an implementation of GitManager interface
 */
public class GitManagerImpl implements GitManager {
    @Override
    public void add( final Agent host, final String repositoryRoot, final List<String> filePaths ) throws GitException {
        //repositoryRoot will be current working directory for agent process
    }


    @Override
    public void delete( final Agent host, final String repositoryRoot, final List<String> filePaths )
            throws GitException {

    }


    @Override
    public void commit( final Agent host, final String repositoryRoot, final List<String> filePaths,
                        final String message ) throws GitException {

    }


    @Override
    public void commit( final Agent host, final String repositoryRoot, final String message ) throws GitException {

    }


    @Override
    public void clone( final Agent host, final String repositoryRoot, final String src, final String targetDir )
            throws GitException {

    }


    @Override
    public void clone( final Agent host, final String repositoryRoot, final String src, final String targetDir,
                       final String newBranchName ) throws GitException {

    }


    @Override
    public void checkout( final Agent host, final String repositoryRoot, final String branchName, final boolean create )
            throws GitException {

    }


    @Override
    public void merge( final Agent host, final String repositoryRoot ) throws GitException {

    }


    @Override
    public void merge( final Agent host, final String repositoryRoot, final String branchName ) throws GitException {

    }


    @Override
    public void pull( final Agent host, final String repositoryRoot, final String repoURL ) throws GitException {

    }


    @Override
    public void fetch( final Agent host, final String repositoryRoot, final String repoURL ) throws GitException {

    }


    @Override
    public GitBranch currentBranch( final Agent host, final String repositoryRoot ) throws GitException {
        return null;
    }


    @Override
    public List<GitBranch> listBranches( final Agent host, final String repositoryRoot ) throws GitException {
        return null;
    }


    @Override
    public void push( final Agent host, final String repositoryRoot ) throws GitException {

    }


    @Override
    public void push( final Agent host, final String repositoryRoot, final String branchName ) throws GitException {

    }


    @Override
    public void revert( final Agent host, final String repositoryRoot, final List<String> filePaths )
            throws GitException {

    }


    @Override
    public void revert( final Agent host, final String repositoryRoot ) throws GitException {

    }
}
