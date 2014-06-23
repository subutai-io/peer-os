/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.aptrepositorymanager;


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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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

        Command command = commandRunner
                .createCommand( new RequestBuilder( String.format( "aptitude search '%s'", pattern ) ),
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
    public void addPackageToRepo( Agent agent, final String pathToPackageFile ) throws AptRepoException {
        Preconditions.checkNotNull( agent, "Agent is null" );
    }


    @Override
    public void removePackageByFilePath( Agent agent, final String packageFileName ) throws AptRepoException {
        Preconditions.checkNotNull( agent, "Agent is null" );
    }


    @Override
    public void removePackageByName( Agent agent, final String packageName ) throws AptRepoException {
        Preconditions.checkNotNull( agent, "Agent is null" );
    }


    @Override
    public String readFileContents( Agent agent, final String pathToFileInsideDebPackage ) throws AptRepoException {
        Preconditions.checkNotNull( agent, "Agent is null" );

        return null;
    }


    private void runCommand( Command command, Agent host, AptCommand aptCommand ) throws AptRepoException {
        runCommand( command, host, aptCommand, true );
    }
}
