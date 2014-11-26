///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.safehaus.subutai.plugin.cassandra.impl;
//
//
//import java.util.HashSet;
//import java.util.Set;
//
//import org.safehaus.subutai.common.command.OutputRedirection;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.common.command.RequestBuilder;
//import org.safehaus.subutai.common.settings.Common;
//import org.safehaus.subutai.common.util.AgentUtil;
//import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
//import org.safehaus.subutai.core.command.api.command.Command;
//import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
//import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
//
//import com.google.common.base.Preconditions;
//import com.google.common.collect.Sets;
//
//
//public class Commands
//{
//
//    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + CassandraClusterConfig.PRODUCT_NAME.toLowerCase();
//    private final CommandRunnerBase commandRunner;
//
//
//    public Commands( CommandRunnerBase commandRunner )
//    {
//        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
//
//        this.commandRunner = commandRunner;
//    }
//
//
//    public Command getInstallCommand( Set<Agent> agents )
//    {
//
//        return commandRunner.createCommand(
//                new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 600 )
//                                                                                                .withStdOutRedirection(
//                                                                                                        OutputRedirection.NO ),
//                agents );
//    }
//
//
//    public Command getStartCommand( Set<Agent> agents )
//    {
//        return commandRunner.createCommand( new RequestBuilder( "service cassandra start" ), agents );
//    }
//
//
//    public Command getStopCommand( Set<Agent> agents )
//    {
//        return commandRunner.createCommand( new RequestBuilder( "service cassandra stop" ), agents );
//    }
//
//
//    public Command getStatusCommand( Set<Agent> agents )
//    {
//        return commandRunner.createCommand( new RequestBuilder( "service cassandra status" ), agents );
//    }
//
//
//    public Command getStatusCommand( Agent agent )
//    {
//        return commandRunner
//                .createCommand( new RequestBuilder( "/etc/init.d/cassandra status" ), Sets.newHashSet( agent ) );
//    }
//
//
//    public Command getConfigureCommand( Set<Agent> agents, String param )
//    {
//
//        return commandRunner.createCommand( new RequestBuilder(
//                        String.format( ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s", param ) ),
//                agents );
//    }
//
//
//    public Command getConfigureRpcAndListenAddressesCommand( Set<Agent> agents, String param )
//    {
//        Set<AgentRequestBuilder> sarb = new HashSet<AgentRequestBuilder>();
//        for ( Agent agent : agents )
//        {
//            AgentRequestBuilder arb = new AgentRequestBuilder( agent,
//                    String.format( ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s %s", param,
//                            AgentUtil.getAgentIpByMask( agent, Common.IP_MASK ) ) );
//            sarb.add( arb );
//        }
//        return commandRunner.createCommand( sarb );
//    }
//}
