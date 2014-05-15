package org.safehaus.kiskis.mgmt.impl.solr;


import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandsSingleton;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;

import java.util.Set;


public class Commands extends CommandsSingleton {

    public static final String INSTALL = "sleep 10 ; apt-get --force-yes --assume-yes install ksks-solr";
    public static final String START = "service solr start";
    public static final String STOP = "service solr stop";
    public static final String STATUS = "service solr status";


    public Commands( CommandRunner commandRunner ) {
        init( commandRunner );
    }


    public Command getInstallCommand( Set<Agent> agents ) {
        return createCommand(
                new RequestBuilder( INSTALL ).withTimeout( 90 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public Command getStartCommand( Agent agent ) {
        return createCommand( new RequestBuilder( START ).withStdOutRedirection( OutputRedirection.NO ),
                Util.wrapAgentToSet( agent ) );
    }


    public Command getStopCommand( Agent agent ) {
        return createCommand( new RequestBuilder( STOP ), Util.wrapAgentToSet( agent ) );
    }


    public Command getStatusCommand( Agent agent ) {
        return createCommand( new RequestBuilder( STATUS ), Util.wrapAgentToSet( agent ) );
    }
}
