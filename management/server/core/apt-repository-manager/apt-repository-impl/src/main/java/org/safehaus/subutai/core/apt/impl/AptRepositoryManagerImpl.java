/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.apt.impl;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.apt.api.AptCommand;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;
import org.safehaus.subutai.core.apt.api.PackageInfo;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * This is an implementation of AptRepositoryManager
 */
public class AptRepositoryManagerImpl implements AptRepositoryManager
{


    private static final String AGENT_IS_NULL_MSG = "Agent is null";
    private static final String LINE_SEPARATOR = "\n";
    private static final Logger LOG = LoggerFactory.getLogger( AptRepositoryManagerImpl.class.getName() );
    private CommandRunner commandRunner;


    public AptRepositoryManagerImpl( final CommandRunner commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    /**
     * Returns list of packages in apt repository
     *
     * @param agent - agent of node where apt repository resides
     * @param pattern - pattern to search for in packages names, might be empty
     *
     * @return - list of packages {@code PackageInfo}
     */
    @Override
    public List<PackageInfo> listPackages( Agent agent, final String pattern ) throws AptRepoException
    {
        Preconditions.checkNotNull( agent, AGENT_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( pattern ), "Pattern is null or empty" );
        List<PackageInfo> packages = new LinkedList<>();

        Command command = commandRunner.createCommand(
                new RequestBuilder( String.format( "aptitude search '%s'", pattern ) ).withTimeout( 60 ),
                Sets.newHashSet( agent ) );
        runCommand( command, agent, AptCommand.LIST_PACKAGES, false );

        StringTokenizer lines =
                new StringTokenizer( command.getResults().get( agent.getUuid() ).getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            if ( line != null )
            {
                String[] packageFields = line.split( "\\s{2,}" );
                if ( packageFields.length == 3 )
                {
                    packages.add( new PackageInfo( packageFields[0], packageFields[1], packageFields[2] ) );
                }
            }
        }
        return packages;
    }


    private void runCommand( Command command, Agent host, AptCommand aptCommand, boolean output )
            throws AptRepoException
    {
        commandRunner.runCommand( command );

        if ( !command.hasSucceeded() )
        {
            if ( command.hasCompleted() )
            {
                AgentResult agentResult = command.getResults().get( host.getUuid() );
                throw new AptRepoException(
                        String.format( "Error while performing %s: %s%n%s, exit code %s", aptCommand.getCommand(),
                                agentResult.getStdOut(), agentResult.getStdErr(), agentResult.getExitCode() ) );
            }
            else
            {
                throw new AptRepoException(
                        String.format( "Error while performing %s: Command timed out", aptCommand.getCommand() ) );
            }
        }
        else if ( output )
        {
            AgentResult agentResult = command.getResults().get( host.getUuid() );
            LOG.info( agentResult.getStdOut() );
        }
    }


    /**
     * Adds debian packages to apt repository. As a result of this call all connected agents will execute apt-get
     * update.
     *
     * @param agent - agent of node where apt repository resides
     * @param pathToPackageFile - absolute path to debian package file
     * @param deleteSourcePackage - indicates whether to delete source package after addition to apt repo
     */
    @Override
    public void addPackageByPath( Agent agent, final String pathToPackageFile, boolean deleteSourcePackage )
            throws AptRepoException
    {
        Preconditions.checkNotNull( agent, AGENT_IS_NULL_MSG );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( pathToPackageFile ), "Path to package file is null or empty" );
        File packageFile = new File( pathToPackageFile );
        Preconditions.checkArgument( packageFile.exists(), "Package file does not exist" );
        Preconditions.checkArgument( !packageFile.isDirectory(), "Package file is directory" );

        Path pathToDebPack = Paths.get( Common.APT_REPO_AMD64_PACKAGES_SUBPATH, packageFile.getName() );
        Command command = commandRunner.createCommand( new RequestBuilder(
                //reprepro includedeb trusty amd64/trusty/*.deb
                String.format( "cp %s %s && reprepro includedeb %s %s%s", pathToPackageFile,
                        Common.APT_REPO_PATH + Common.APT_REPO_AMD64_PACKAGES_SUBPATH, Common.APT_REPO, pathToDebPack,
                        deleteSourcePackage ? String.format( " && rm -f %s", pathToPackageFile ) : "" ) )
                .withCwd( Common.APT_REPO_PATH ).withTimeout( 120 ), Sets.newHashSet( agent ) );

        runCommand( command, agent, AptCommand.ADD_PACKAGE );
        broadcastAptGetUpdateCommand();
    }


    /**
     * Removes package from apt repository.As a result of this call all connected agents will execute apt-get
     *
     * @param agent - agent of node where apt repository resides
     * @param packageName - name of package to delete
     */
    @Override
    public void removePackageByName( Agent agent, final String packageName ) throws AptRepoException
    {
        Preconditions.checkNotNull( agent, AGENT_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packageName ), "Package name is null or empty" );

        Command command = commandRunner.createCommand(
                new RequestBuilder( String.format( "reprepro remove %s %s", Common.APT_REPO, packageName ) )
                        .withCwd( Common.APT_REPO_PATH ).withTimeout( 120 ), Sets.newHashSet( agent ) );

        runCommand( command, agent, AptCommand.REMOVE_PACKAGE );
        broadcastAptGetUpdateCommand();
    }


    /**
     * Returns contents of files inside debian packages
     *
     * @param agent -agent of node where package resides
     * @param pathToPackageFile - absolute path to debian package file
     * @param pathsToFilesInsidePackage - relative paths to files whose contents to return
     *
     * @return - list of contents of files in the same order as in  @pathsToFilesInsidePackage argument
     */
    @Override
    public List<String> readFileContents( Agent agent, final String pathToPackageFile,
                                          final List<String> pathsToFilesInsidePackage ) throws AptRepoException
    {
        Preconditions.checkNotNull( agent, AGENT_IS_NULL_MSG );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( pathToPackageFile ), "Path to package file is null or empty" );
        File packageFile = new File( pathToPackageFile );
        Preconditions.checkArgument( packageFile.exists(), "Package file does not exist" );
        Preconditions.checkArgument( !packageFile.isDirectory(), "Package file is directory" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( pathsToFilesInsidePackage ),
                "PathsToFilesInsidePackage is null or empty" );
        StringBuilder filesSB = new StringBuilder();
        long nano = System.nanoTime();

        for ( int i = 0; i < pathsToFilesInsidePackage.size(); i++ )
        {
            final String path = pathsToFilesInsidePackage.get( i );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( path ),
                    "One of the paths to files inside package is null or empty" );

            filesSB.append( " cat " ).append( Common.TMP_DEB_PACKAGE_UNPACK_PATH ).append( "/" ).append( nano )
                   .append( "/" ).append( path );
            if ( i < pathsToFilesInsidePackage.size() - 1 )
            {
                filesSB.append( " && echo '<<<" ).append( nano ).append( ">>>' && " );
            }
        }

        String commandString =
                String.format( "dpkg-deb -x %s %s/%s && %s", pathToPackageFile, Common.TMP_DEB_PACKAGE_UNPACK_PATH,
                        nano, filesSB );

        Command command = commandRunner
                .createCommand( new RequestBuilder( commandString ).withCwd( Common.APT_REPO_PATH ).withTimeout( 120 ),
                        Sets.newHashSet( agent ) );

        try
        {
            runCommand( command, agent, AptCommand.READ_FILE_INSIDE_PACKAGE, false );
        }
        finally
        {

            commandRunner.runCommandAsync( commandRunner.createCommand(
                    new RequestBuilder( String.format( "rm -rf %s/%s", Common.TMP_DEB_PACKAGE_UNPACK_PATH, nano ) ),
                    Sets.newHashSet( agent ) ) );
        }


        String out = command.getResults().get( agent.getUuid() ).getStdOut();

        String[] outs = out.split( String.format( "<<<%s>>>", nano ) );

        return Lists.newArrayList( outs );
    }


    private void runCommand( Command command, Agent host, AptCommand aptCommand ) throws AptRepoException
    {
        runCommand( command, host, aptCommand, true );
    }


    //sends broadcast command to all currently connected agents
    private void broadcastAptGetUpdateCommand()
    {
        Command command =
                commandRunner.createBroadcastCommand( new RequestBuilder( "apt-get update" ).withTimeout( 90 ) );
        commandRunner.runCommandAsync( command );
    }
}
