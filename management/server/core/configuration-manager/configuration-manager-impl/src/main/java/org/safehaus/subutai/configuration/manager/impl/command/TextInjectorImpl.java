package org.safehaus.subutai.configuration.manager.impl.command;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.configuration.manager.api.TextInjector;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.FileUtil;


/**
 * Created by bahadyr on 7/17/14.
 */
public class TextInjectorImpl implements TextInjector {

    private CommandRunner commandRunner;


    public void setCommandRunner( final CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    @Override
    public boolean echoTextIntoAgent( Agent agent, String path, String content ) {
        //TODO call echo command on given agent
        Command command = Commands.getEchoCommand( agent, path, content );
        commandRunner.runCommand( command );

        if ( command.hasSucceeded() ) {
            String config = command.getResults().get( agent.getUuid() ).getStdOut();
            System.out.println( config );
        }
        else {
            //            po.addLogFailed( String.format( "Installation failed, %s", catCommand.getAllErrors() ) );
            System.out.println( "echo failed!" );
            return false;
        }
        return true;
    }


    @Override
    public String catFile( Agent agent, String pathToFile ) {
        //TODO execute cat commat on given agent and path
        Command catCommand = Commands.getCatCommand( agent, pathToFile );
        commandRunner.runCommand( catCommand );

        if ( catCommand.hasSucceeded() ) {
            //            po.addLog( "cat done" );
            String config = catCommand.getResults().get( agent.getUuid() ).getStdOut();
            System.out.println( config );
            System.out.println( "cat done!" );
        }
        else {
            //            po.addLogFailed( String.format( "Installation failed, %s", catCommand.getAllErrors() ) );
            System.out.println( "cat failed!" );
            return "";
        }
        return "";
    }


    @Override
    public String getConfigTemplate( final String path ) {
        String content = FileUtil.getContent( path, this );
        return content;
    }
}
