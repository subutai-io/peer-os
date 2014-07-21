package org.safehaus.subutai.configuration.manager.impl.command;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.configuration.manager.api.TextInjector;
import org.safehaus.subutai.shared.protocol.Agent;


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
        Command catCommand = Commands.getEchoCommand( agent, path, content );
        commandRunner.runCommand( catCommand );

        if ( catCommand.hasSucceeded() ) {
            //            po.addLog( "cat done" );
            System.out.println( "echo done!" );
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
            System.out.println( "cat done!" );
        }
        else {
            //            po.addLogFailed( String.format( "Installation failed, %s", catCommand.getAllErrors() ) );
            System.out.println( "cat failed!" );
            return "";
        }
        return "";
    }
}
