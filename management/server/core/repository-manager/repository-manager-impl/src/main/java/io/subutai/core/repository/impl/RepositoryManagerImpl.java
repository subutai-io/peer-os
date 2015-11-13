package io.subutai.core.repository.impl;


import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.repository.api.PackageInfo;
import io.subutai.core.repository.api.RepositoryException;
import io.subutai.core.repository.api.RepositoryManager;


/**
 * Implementation of RepositoryManager
 */
public class RepositoryManagerImpl implements RepositoryManager
{
    private static final String LINE_SEPARATOR = "\n";
    private static final String INVALID_PACKAGE_NAME = "Invalid package name";

    private final PeerManager peerManager;
    protected Commands commands = new Commands();
    protected CommandUtil commandUtil = new CommandUtil();


    public RepositoryManagerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager, "Peer manager is null" );

        this.peerManager = peerManager;
    }


    @Override
    public void addPackageByPath( final String pathToPackage ) throws RepositoryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( pathToPackage ), "Invalid package path" );

        executeCommand( commands.getAddPackageCommand( pathToPackage ) );
        executeUpdateRepoCommand();
    }


    @Override
    public void removePackageByName( final String packageName ) throws RepositoryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packageName ), INVALID_PACKAGE_NAME );

        executeCommand( commands.getRemovePackageCommand( packageName ) );
        executeUpdateRepoCommand();
    }


    @Override
    public void extractPackageByName( final String packageName ) throws RepositoryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packageName ), INVALID_PACKAGE_NAME );

        executeCommand( commands.getExtractPackageCommand( packageName ) );
    }


    @Override
    public void extractPackageFiles( final String packageName, final Set<String> files ) throws RepositoryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packageName ), INVALID_PACKAGE_NAME );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( files ), "Files are not specified" );

        executeCommand( commands.getExtractFilesCommand( packageName, files ) );
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

            String[] packageFields = line.split( "\\s+-\\s+" );
            if ( packageFields.length == 2 )
            {
                packages.add( new PackageInfo( packageFields[0], packageFields[1] ) );
            }
        }

        return packages;
    }


    @Override
    public String getPackageInfo( final String packageName ) throws RepositoryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packageName ), INVALID_PACKAGE_NAME );

        CommandResult result = executeCommand( commands.getPackageInfoCommand( packageName ) );

        return result.getStdOut();
    }


    @Override
    public String getFullPackageName( final String shortPackageName ) throws RepositoryException
    {
        String packageInfo = getPackageInfo( shortPackageName );
        Pattern p = Pattern.compile( "Filename:.+/(.+deb)" );
        Matcher m = p.matcher( packageInfo );
        if ( m.find() )
        {
            return m.group( 1 );
        }
        else
        {
            throw new RepositoryException(
                    String.format( "Could not obtain full name by short name %s from:%n%s", shortPackageName,
                            packageInfo ) );
        }
    }


    protected ManagementHost getManagementHost() throws HostNotFoundException
    {
        return peerManager.getLocalPeer().getManagementHost();
    }


    protected CommandResult executeCommand( RequestBuilder requestBuilder ) throws RepositoryException
    {
        try
        {
            return commandUtil.execute( requestBuilder, getManagementHost() );
        }
        catch ( HostNotFoundException | CommandException e )
        {
            throw new RepositoryException( e );
        }
    }


    protected CommandResult executeUpdateRepoCommand() throws RepositoryException
    {
        try
        {
            ManagementHost managementHost = getManagementHost();
            CommandResult result = managementHost.execute( commands.getUpdateRepoCommand() );
            if ( !result.hasCompleted() )
            {
                throw new RepositoryException( "Command timed out" );
            }

            return result;
        }
        catch ( HostNotFoundException | CommandException e )
        {
            throw new RepositoryException( e );
        }
    }


    @Override
    public void addRepository( final String ip ) throws RepositoryException
    {
        try
        {
            commandUtil.execute( commands.getRepositoryCommand( ip ), getManagementHost() );
        }
        catch ( HostNotFoundException | CommandException e )
        {
            throw new RepositoryException( "Could not add remote repository", e );
        }
    }


    @Override
    public void removeRepository( final String ip ) throws RepositoryException
    {
        try
        {
            commandUtil.execute( commands.getRemoveRepositoryCommand( ip ), getManagementHost() );
        }
        catch ( HostNotFoundException | CommandException e )
        {
            throw new RepositoryException( "Could not remove remote repository", e );
        }
    }
}
