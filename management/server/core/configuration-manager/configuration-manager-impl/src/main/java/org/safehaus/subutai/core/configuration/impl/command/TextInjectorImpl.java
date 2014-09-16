package org.safehaus.subutai.core.configuration.impl.command;


import java.util.logging.Logger;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.configuration.api.TextInjector;


/**
 * Created by bahadyr on 7/17/14.
 */
public class TextInjectorImpl implements TextInjector {

    private static final Logger logger = Logger.getLogger( TextInjector.class.getName() );
    private CommandRunner commandRunner;
    private AgentManager agentManager;


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    @Override
    public boolean echoTextIntoAgent( String hostname, String path, String content ) {
        //TODO call echo command on given agent
        Agent agent = agentManager.getAgentByHostname( hostname );
        Command command = Commands.getEchoCommand( agent, path, content );
        commandRunner.runCommand( command );

        if ( !command.hasSucceeded() ) {
            logger.info( "Failed to echo content to file" );
            return false;
        }
        return true;
    }


    @Override
    public String catFile( String hostname, String pathToFile ) {
        //TODO execute cat commat on given agent and path
        Agent agent = agentManager.getAgentByHostname( hostname );
        Command catCommand = Commands.getCatCommand( agent, pathToFile );
        commandRunner.runCommand( catCommand );

        if ( catCommand.hasSucceeded() ) {
            return catCommand.getResults().get( agent.getUuid() ).getStdOut();
        }
        else {
            logger.info( "Failed to cat file" );
            return "";
        }
    }


    @Override
    public String getConfigTemplate( final String path ) {
        return FileUtil.getContent( path, this );
    }
}
