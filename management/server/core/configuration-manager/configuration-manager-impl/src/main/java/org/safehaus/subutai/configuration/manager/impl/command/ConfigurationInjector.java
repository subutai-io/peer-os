package org.safehaus.subutai.configuration.manager.impl.command;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 7/17/14.
 */
public class ConfigurationInjector {

    private CommandRunner commandRunner;


    public void setCommandRunner( final CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public boolean echoTextIntoAgent( Agent agent, String path, String content ) {
        //TODO call echo command on given agent
        Command catCommand = Commands.getEchoCommand( agent, path, content );
        commandRunner.runCommand( catCommand );

        if ( catCommand.hasSucceeded() ) {
            //            po.addLog( "cat done" );

        }
        else {
            //            po.addLogFailed( String.format( "Installation failed, %s", catCommand.getAllErrors() ) );
            return false;
        }
        return true;
    }


    public String catFile( Agent agent, String path ) {
        //TODO execute cat commat on given agent and path
        Command catCommand = Commands.getCatCommand( agent, path );
        commandRunner.runCommand( catCommand );

        if ( catCommand.hasSucceeded() ) {
            //            po.addLog( "cat done" );

        }
        else {
            //            po.addLogFailed( String.format( "Installation failed, %s", catCommand.getAllErrors() ) );
            return "";
        }
        return "";
    }
}
