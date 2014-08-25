package org.safehaus.subutai.impl.networkmanager;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.AgentRequestBuilder;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.common.AgentUtil;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.settings.Common;


/**
 * Created by daralbaev on 03.04.14.
 */
public class Commands {

    public static Command getCreateSSHCommand( List<Agent> agentList ) {
        return NetwokManagerImpl.getCommandRunner().createCommand( new RequestBuilder( "rm -Rf /root/.ssh && " +
                "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ), new HashSet<Agent>( agentList ) );
    }


    public static Command getReadSSHCommand( List<Agent> agentList ) {
        return NetwokManagerImpl.getCommandRunner().createCommand( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ),
                new HashSet<>( agentList ) );
    }


    public static Command getWriteSSHCommand( List<Agent> agentList, String key ) {
        return NetwokManagerImpl.getCommandRunner()
                                .createCommand( new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                                                "chmod 700 /root/.ssh && " +
                                                "echo '%s' > /root/.ssh/authorized_keys && " +
                                                "chmod 644 /root/.ssh/authorized_keys", key ) ),
                                        new HashSet<>( agentList ) );
    }


    public static Command getConfigSSHCommand( List<Agent> agentList ) {
        return NetwokManagerImpl.getCommandRunner()
                                .createCommand( new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                                        "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                                        "chmod 644 /root/.ssh/config" ), new HashSet<>( agentList ) );
    }


    public static Command getReadHostsCommand( List<Agent> agentList ) {
        return NetwokManagerImpl.getCommandRunner()
                                .createCommand( new RequestBuilder( "cat /etc/hosts" ), new HashSet<>( agentList ) );
    }


    public static Command getWriteHostsCommand( List<Agent> agentList, String hosts ) {
        return NetwokManagerImpl.getCommandRunner()
                                .createCommand( new RequestBuilder( String.format( "echo '%s' > /etc/hosts", hosts ) ),
                                        new HashSet<>( agentList ) );
    }


    public static Command getAddIpHostToEtcHostsCommand( String domainName, Set<Agent> agents ) {
        Set<AgentRequestBuilder> requestBuilders = new HashSet<>();

        for ( Agent agent : agents ) {
            StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
            StringBuilder appendHosts = new StringBuilder();

            for ( Agent otherAgent : agents ) {
                String ip = AgentUtil.getAgentIpByMask( otherAgent, Common.IP_MASK );
                String hostname = otherAgent.getHostname();
                cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
                appendHosts.append( "/bin/echo '" ).
                        append( ip ).append( " " ).
                                   append( hostname ).append( "." ).append( domainName ).
                                   append( " " ).append( hostname ).
                                   append( "' >> '/etc/hosts'; " );
            }

            if ( cleanHosts.length() > 0 ) {
                //drop pipe | symbol
                cleanHosts.setLength( cleanHosts.length() - 1 );
                cleanHosts.insert( 0, "egrep -v '" );
                cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
                appendHosts.insert( 0, cleanHosts );
            }

            appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( "' >> '/etc/hosts';" );

            requestBuilders.add( ( AgentRequestBuilder ) new AgentRequestBuilder( agent, appendHosts.toString() )
                    .withTimeout( 30 ) );
        }

        return NetwokManagerImpl.getCommandRunner().createCommand( "Add ip-host pair to /etc/hosts", requestBuilders );
    }
}
