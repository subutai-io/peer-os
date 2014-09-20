package org.safehaus.subutai.plugin.solr.impl;


import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;

import com.google.common.collect.Sets;


public class Commands extends CommandsSingleton
{

    public static final String START = "service solr start";
    public static final String STOP = "service solr stop";
    public static final String STATUS = "service solr status";


    public Commands( CommandRunner commandRunner )
    {
        init( commandRunner );
    }


    public Command getStartCommand( Agent agent )
    {
        return createCommand(
                new RequestBuilder( START ).withTimeout( 90 ).withStdOutRedirection( OutputRedirection.NO ),
                Sets.newHashSet( agent ) );
    }


    public Command getStopCommand( Agent agent )
    {
        return createCommand( new RequestBuilder( STOP ).withTimeout( 60 ), Sets.newHashSet( agent ) );
    }


    public Command getStatusCommand( Agent agent )
    {
        return createCommand( new RequestBuilder( STATUS ).withTimeout( 60 ), Sets.newHashSet( agent ) );
    }
}
