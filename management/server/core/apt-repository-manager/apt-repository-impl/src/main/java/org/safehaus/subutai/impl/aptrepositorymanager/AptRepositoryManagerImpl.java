/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.aptrepositorymanager;


import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.safehaus.subutai.api.aptrepositorymanager.AptCommand;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepoException;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepositoryManager;
import org.safehaus.subutai.api.aptrepositorymanager.PackageInfo;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * This is an implementation of AptRepositoryManager
 */
public class AptRepositoryManagerImpl implements AptRepositoryManager {
    private final String LINE_SEPARATOR = "\n";

    private CommandRunner commandRunner;


    public AptRepositoryManagerImpl( final CommandRunner commandRunner ) {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    @Override
    public List<PackageInfo> listPackages( Agent agent, final String pattern ) throws AptRepoException {
        Preconditions.checkNotNull( agent, "Agent is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( pattern ), "Pattern is null or empty" );
        List<PackageInfo> packages = new LinkedList<>();

        Command command = commandRunner.createCommand(
                new RequestBuilder( String.format( "aptitude search '%s'", pattern ) ).withTimeout( 60 ),
                Sets.newHashSet( agent ) );
        runCommand( command, agent, AptCommand.LIST_PACKAGES, false );

        StringTokenizer lines =
                new StringTokenizer( command.getResults().get( agent.getUuid() ).getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() ) {
            String line = lines.nextToken();

            if ( line != null ) {
                String[] packageFields = line.split( "\\s{2,}" );
                if ( packageFields.length == 3 ) {
                    packages.add( new PackageInfo( packageFields[0], packageFields[1], packageFields[2] ) );
                }
            }
        }
        return packages;
    }


    private void runCommand( Command command, Agent host, AptCommand aptCommand, boolean output )
            throws AptRepoException {
        commandRunner.runCommand( command );

        if ( !command.hasSucceeded() ) {
            if ( command.hasCompleted() ) {
                AgentResult agentResult = command.getResults().get( host.getUuid() );
                throw new AptRepoException(
                        String.format( "Error while performing %s: %s\n%s, exit code %s", aptCommand.getCommand(),
                                agentResult.getStdOut(), agentResult.getStdErr(), agentResult.getExitCode() ) );
            }
            else {
                throw new AptRepoException(
                        String.format( "Error while performing %s: Command timed out", aptCommand.getCommand() ) );
            }
        }
        else if ( output ) {
            AgentResult agentResult = command.getResults().get( host.getUuid() );
            System.out.println( agentResult.getStdOut() );
        }
    }


    @Override
    public void addPackageByPath( Agent agent, final String pathToPackageFile, boolean deleteSourcePackage )
            throws AptRepoException {
        Preconditions.checkNotNull( agent, "Agent is null" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( pathToPackageFile ), "Path to package file is null or empty" );
        Preconditions.checkArgument( new File( pathToPackageFile ).exists(), "Package file does not exist" );

        String commandString =
                String.format( "%s %s %3$s && rm -rf db/ dists/ pool/ && reprepro includedeb precise %3$s/*.deb",
                        deleteSourcePackage ? "mv" : "cp", pathToPackageFile, Common.AMD64_ARCH_DEB_PACKAGES_LOCATION );
        Command command = commandRunner
                .createCommand( new RequestBuilder( commandString ).withCwd( Common.APT_REPO_PATH ).withTimeout( 120 ),
                        Sets.newHashSet( agent ) );

        runCommand( command, agent, AptCommand.ADD_PACKAGE );
    }


    @Override
    public void removePackageByName( Agent agent, final String packageName ) throws AptRepoException {
        Preconditions.checkNotNull( agent, "Agent is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packageName ), "Package name is null or empty" );

        String commandString = String.format(
                "pkgFileName=$(apt-cache show %1$s | grep -o '%1$s[^/]*deb$' ) && rm %2$s/$pkgFileName && rm -rf db/ " +
                        "dists/ pool/ && " + "reprepro includedeb precise %2$s/*.deb", packageName,
                Common.AMD64_ARCH_DEB_PACKAGES_LOCATION );

        Command command = commandRunner
                .createCommand( new RequestBuilder( commandString ).withCwd( Common.APT_REPO_PATH ).withTimeout( 120 ),
                        Sets.newHashSet( agent ) );

        runCommand( command, agent, AptCommand.REMOVE_PACKAGE );
    }


    @Override
    public List<String> readFileContents( Agent agent, final String packageName,
                                          final List<String> pathsToFilesInsidePackage ) throws AptRepoException {
        Preconditions.checkNotNull( agent, "Agent is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packageName ), "Package name is null or empty" );
        Preconditions.checkArgument( pathsToFilesInsidePackage != null && !pathsToFilesInsidePackage.isEmpty(),
                "PathsToFilesInsidePackage is null or empty" );
        StringBuilder filesSB = new StringBuilder();
        long nano = System.nanoTime();

        for ( int i = 0; i < pathsToFilesInsidePackage.size(); i++ ) {
            final String path = pathsToFilesInsidePackage.get( i );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( path ),
                    "One of the paths to files inside package is null or empty" );

            filesSB.append( " cat " ).append( path );
            if ( i < pathsToFilesInsidePackage.size() - 1 ) {
                filesSB.append( " && echo '<<<" ).append( nano ).append( ">>>' && " );
            }
        }

        String commandString = String.format(
                "pkgFileName=$(apt-cache show %1$s | grep -o '%1$s[^/]*deb$' ) && dpkg-deb -x %2$s/$pkgFileName "
                        + "%3$s/%1$s-%4$s && %5$s && rm -r %3$s/%1$s-%4$s", packageName,
                Common.AMD64_ARCH_DEB_PACKAGES_LOCATION, Common.TMP_DEB_PACKAGE_UNPACK_PATH, nano, filesSB );

        Command command = commandRunner
                .createCommand( new RequestBuilder( commandString ).withCwd( Common.APT_REPO_PATH ).withTimeout( 120 ),
                        Sets.newHashSet( agent ) );

        runCommand( command, agent, AptCommand.READ_FILE_INSIDE_PACKAGE, false );

        String out = command.getResults().get( agent.getUuid() ).getStdOut();

        String[] outs = out.split( String.format( "<<<%s>>>", nano ) );

        return Lists.newArrayList( outs );
    }


    private void runCommand( Command command, Agent host, AptCommand aptCommand ) throws AptRepoException {
        runCommand( command, host, aptCommand, true );
    }
}
