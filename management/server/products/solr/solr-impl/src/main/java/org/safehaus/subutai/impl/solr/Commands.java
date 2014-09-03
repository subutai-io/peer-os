package org.safehaus.subutai.impl.solr;


import com.google.common.collect.Sets;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;


public class Commands extends CommandsSingleton
{

    public static final String INSTALL = "sleep 20 ; apt-get --force-yes --assume-yes install ksks-solr";
    public static final String START = "service solr start";
    public static final String STOP = "service solr stop";
    public static final String STATUS = "service solr status";


    public Commands( CommandRunner commandRunner )
    {
        init( commandRunner );
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( INSTALL ).withTimeout( 180 ).withStdOutRedirection( OutputRedirection.NO ), agents );
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
