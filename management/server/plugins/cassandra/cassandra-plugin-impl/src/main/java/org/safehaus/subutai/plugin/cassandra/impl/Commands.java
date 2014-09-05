/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.AgentUtil;
import org.safehaus.subutai.core.command.api.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;
import org.safehaus.subutai.core.command.api.RequestBuilder;

import com.google.common.collect.Sets;


/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    public Commands( CommandRunner commandRunner ) {
        init( commandRunner );
    }


    public static Command getInstallCommand( Set<Agent> agents ) {

        return createCommand( new RequestBuilder( "sleep 10; apt-get --force-yes --assume-yes install ksks-cassandra" )
                .withTimeout( 360 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public static Command getStartCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service cassandra start" ), agents );
    }


    public static Command getStopCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service cassandra stop" ), agents );
    }


    public static Command getStatusCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service cassandra status" ), agents );
    }


    public static Command getStatusCommand( Agent agent ) {
        return createCommand( new RequestBuilder( "/etc/init.d/cassandra status" ), Sets.newHashSet( agent ) );
    }


    public static Command getConfigureCommand( Set<Agent> agents, String param ) {

        return createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s", param ) ),
                agents );
    }


    public static Command getConfigureRpcAndListenAddressesCommand( Set<Agent> agents, String param ) {
        Set<AgentRequestBuilder> sarb = new HashSet<AgentRequestBuilder>();
        for ( Agent agent : agents ) {
            AgentRequestBuilder arb = new AgentRequestBuilder( agent,
                    String.format( ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s %s", param,
                            AgentUtil.getAgentIpByMask( agent, Common.IP_MASK ) ) );
            sarb.add( arb );
        }
        return createCommand( sarb );
    }
}
