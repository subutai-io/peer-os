/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.collect.Sets;


/**
 * <p/> <p/> * @todo refactor addPropertyCommand & removePropertyCommand to not use custom scripts
 */
public class Commands
{

    public static final String PACKAGE_NAME = "ksks-zookeeper";
    private final CommandRunnerBase commandRunnerBase;


    public Commands( CommandRunnerBase commandRunnerBase )
    {
        this.commandRunnerBase = commandRunnerBase;
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ), agents );
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 900)
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 900 )
                                                                                              .withStdOutRedirection(
                                                                                                      OutputRedirection.NO ),
                agents );
    }


    public Command getStartCommand( Set<Agent> agents )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( "service zookeeper start" ).withTimeout( 15 ), agents );
    }


    public Command getRestartCommand( Set<Agent> agents )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( "service zookeeper restart" ).withTimeout( 15 ), agents );
    }


    public Command getStopCommand( Agent agent )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( "service zookeeper stop" ), Sets.newHashSet( agent ) );
    }


    public Command getStatusCommand( Agent agent )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( "service zookeeper status" ), Sets.newHashSet( agent ) );
    }


    public Command getConfigureClusterCommand( Set<Agent> agents, String myIdFilePath, String zooCfgFileContents,
                                               String zooCfgFilePath )
    {
        Set<AgentRequestBuilder> requestBuilders = new HashSet<>();

        int id = 0;
        for ( Agent agent : agents )
        {
            requestBuilders.add( new AgentRequestBuilder( agent,
                    String.format( ". /etc/profile && zookeeper-setID.sh %s && echo '%s' > %s", ++id,
                            zooCfgFileContents, zooCfgFilePath ) ) );
        }

        return commandRunnerBase.createCommand( requestBuilders );
    }


    public Command getAddPropertyCommand( String fileName, String propertyName, String propertyValue,
                                          Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder(
                String.format( ". /etc/profile && zookeeper-property.sh add %s %s %s", fileName, propertyName,
                        propertyValue ) ), agents );
    }


    public Command getRemovePropertyCommand( String fileName, String propertyName, Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && zookeeper-property.sh remove %s %s", fileName,
                                propertyName ) ), agents );
    }
}
