package org.safehaus.subutai.core.repository.impl;


import java.util.Set;
import java.util.StringTokenizer;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.repository.api.PackageInfo;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Implementation of RepositoryManager
 */
public class RepositoryManagerImpl implements RepositoryManager
{
    private static final Logger LOG = LoggerFactory.getLogger( RepositoryManagerImpl.class.getName() );
    private static final String LINE_SEPARATOR = "\n";

    private final ManagementHost managementHost;
    protected Commands commands = new Commands();


    public RepositoryManagerImpl( final PeerManager peerManager ) throws RepositoryException
    {
        Preconditions.checkNotNull( peerManager, "Peer manager is null" );

        try
        {
            managementHost = peerManager.getLocalPeer().getManagementHost();
        }
        catch ( PeerException e )
        {
            LOG.error( "Error in constructor", e );
            throw new RepositoryException( e );
        }
    }


    protected CommandResult executeCommand( RequestBuilder requestBuilder ) throws RepositoryException
    {
        try
        {
            CommandResult result = managementHost.execute( requestBuilder );
            if ( !result.hasSucceeded() )
            {
                if ( result.hasCompleted() )
                {
                    throw new RepositoryException( result.getStdErr() );
                }
                else
                {
                    throw new RepositoryException( "Command timed out" );
                }
            }

            return result;
        }
        catch ( CommandException e )
        {
            throw new RepositoryException( e );
        }
    }


    @Override
    public void addPackageByPath( final String pathToPackage ) throws RepositoryException
    {
        executeCommand( commands.getAddPackageCommand( pathToPackage ) );
    }


    @Override
    public void removePackageByName( final String packageName ) throws RepositoryException
    {
        executeCommand( commands.getRemovePackageCommand( packageName ) );
    }


    @Override
    public void extractPackageByName( final String packageName ) throws RepositoryException
    {
        executeCommand( commands.getExtractPackageCommand( packageName ) );
    }


    @Override
    public Set<PackageInfo> listPackages( final String term ) throws RepositoryException
    {
        CommandResult result = executeCommand( commands.getListPackagesCommand( term ) );

        Set<PackageInfo> packages = Sets.newHashSet();

        StringTokenizer lines = new StringTokenizer( result.getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            if ( line != null )
            {
                String[] packageFields = line.split( "\\s+-\\s+" );
                if ( packageFields.length == 2 )
                {
                    packages.add( new PackageInfo( packageFields[0], packageFields[1] ) );
                }
            }
        }

        return packages;
    }
}
