package org.safehaus.subutai.impl.elasticsearch;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandsSingleton;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.Set;

public class Commands extends CommandsSingleton {

    public static Command getInstallCommand(Set<Agent> agents) {

        return createCommand(
                new RequestBuilder(
                        " apt-get --force-yes --assume-yes install ksks-elasticsearch ")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );

    }


    public static Command getConfigureCommand( Set<Agent> agents, String param ) {
        return createCommand( new RequestBuilder( String.format( " . /etc/profile && es-conf.sh %s ", param ) ), agents
                            );
    }


    public static Command getStatusCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service elasticsearch status" ), agents );
    }


    public static Command getStartCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service elasticsearch start" ), agents );
    }


    public static Command getStopCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service elasticsearch stop" ), agents );
    }
}
