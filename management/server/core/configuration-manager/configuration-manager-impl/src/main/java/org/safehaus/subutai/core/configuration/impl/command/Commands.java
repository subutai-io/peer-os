/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.configuration.impl.command;


import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * @author dilshat
 */
public class Commands
{
    private final CommandRunnerBase commandRunner;


    public Commands( final CommandRunnerBase commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    public Command getCatCommand( Agent agent, String filePath )
    {

        return commandRunner.createCommand( new RequestBuilder( "cat " + filePath ).withTimeout( 90 )
                                                                                   .withStdOutRedirection(
                                                                                           OutputRedirection
                                                                                                   .CAPTURE_AND_RETURN ),
                Sets.newHashSet( agent ) );
    }


    public Command getEchoCommand( Agent agent, String filePath, String content )
    {

        return commandRunner.createCommand(
                new RequestBuilder( "echo " + " '" + content + "' > " + filePath ).withTimeout( 90 )
                                                                                  .withStdOutRedirection(
                                                                                          OutputRedirection
                                                                                                  .CAPTURE_AND_RETURN ),
                Sets.newHashSet( agent ) );
    }
}
