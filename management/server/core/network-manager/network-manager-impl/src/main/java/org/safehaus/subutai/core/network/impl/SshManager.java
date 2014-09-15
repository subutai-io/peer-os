package org.safehaus.subutai.core.network.impl;


import java.util.Arrays;
import java.util.List;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;

import com.google.common.base.Strings;


/**
 * Created by daralbaev on 04.04.14.
 */
public class SshManager {
    private List<Agent> agentList;
    private String keys;
    private CommandRunner commandRunner;
    private Commands commands;


    public SshManager( CommandRunner commandRunner, List<Agent> agentList ) {
        this.commandRunner = commandRunner;
        this.agentList = agentList;
        this.commands = new Commands( commandRunner );
    }


    public boolean execute() {
        if ( agentList != null && !agentList.isEmpty() ) {
            if ( create() ) {
                if ( read() ) {
                    if ( write() ) {
                        return config();
                    }
                }
            }
        }

        return false;
    }


    private boolean create() {
        Command command = commands.getCreateSSHCommand( agentList );
        commandRunner.runCommand( command );

        return command.hasSucceeded();
    }


    private boolean read() {
        Command command = commands.getReadSSHCommand( agentList );
        commandRunner.runCommand( command );

        StringBuilder value = new StringBuilder();
        if ( command.hasCompleted() ) {
            for ( Agent agent : agentList ) {
                AgentResult result = command.getResults().get( agent.getUuid() );
                if ( !Strings.isNullOrEmpty( result.getStdOut() ) ) {
                    value.append( result.getStdOut() );
                }
            }
        }
        keys = value.toString();

        if ( !Strings.isNullOrEmpty( keys ) && command.hasSucceeded() ) {
            return true;
        }
        else {
            return false;
        }
    }


    private boolean write() {
        Command command = commands.getWriteSSHCommand( agentList, keys );
        commandRunner.runCommand( command );

        return command.hasSucceeded();
    }


    private boolean config() {
        Command command = commands.getConfigSSHCommand( agentList );
        commandRunner.runCommand( command );

        return command.hasSucceeded();
    }


    public boolean execute( Agent agent ) {
        if ( agentList != null && !agentList.isEmpty() && agent != null ) {
            if ( create( agent ) ) {
                agentList.add( agent );

                if ( read() ) {
                    if ( write() ) {
                        return config();
                    }
                }
            }
        }

        return false;
    }


    private boolean create( Agent agent ) {
        Command command = commands.getCreateSSHCommand( Arrays.asList( agent ) );
        commandRunner.runCommand( command );

        return command.hasSucceeded();
    }
}
