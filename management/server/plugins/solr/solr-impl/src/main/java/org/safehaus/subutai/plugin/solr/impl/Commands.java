package org.safehaus.subutai.plugin.solr.impl;


import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.collect.Sets;


public class Commands
{

    public static final String START = "service solr start";
    public static final String STOP = "service solr stop";
    public static final String STATUS = "service solr status";

    private final CommandRunnerBase commandRunnerBase;


    public Commands( CommandRunnerBase commandRunnerBase )
    {
        this.commandRunnerBase = commandRunnerBase;
    }


    public Command getStartCommand( Agent agent )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( START ).withTimeout( 900 ).withStdOutRedirection( OutputRedirection.NO ),
                Sets.newHashSet( agent ) );
    }


    public Command getStopCommand( Agent agent )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( STOP ).withTimeout( 600 ), Sets.newHashSet( agent ) );
    }


    public Command getStatusCommand( Agent agent )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( STATUS ).withTimeout( 600 ), Sets.newHashSet( agent ) );
    }
}
