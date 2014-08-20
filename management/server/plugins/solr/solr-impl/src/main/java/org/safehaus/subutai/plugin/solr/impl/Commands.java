package org.safehaus.subutai.plugin.solr.impl;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.CommandsSingleton;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;


public class Commands extends CommandsSingleton {

    public static final String START = "service solr start";
    public static final String STOP = "service solr stop";
    public static final String STATUS = "service solr status";


    public Commands( CommandRunner commandRunner ) {
        init( commandRunner );
    }


    public Command getStartCommand( Agent agent ) {
        return createCommand(
                new RequestBuilder( START ).withTimeout( 90 ).withStdOutRedirection( OutputRedirection.NO ),
                Util.wrapAgentToSet( agent ) );
    }


    public Command getStopCommand( Agent agent ) {
        return createCommand( new RequestBuilder( STOP ).withTimeout( 60 ), Util.wrapAgentToSet( agent ) );
    }


    public Command getStatusCommand( Agent agent ) {
        return createCommand( new RequestBuilder( STATUS ).withTimeout( 60 ), Util.wrapAgentToSet( agent ) );
    }
}
