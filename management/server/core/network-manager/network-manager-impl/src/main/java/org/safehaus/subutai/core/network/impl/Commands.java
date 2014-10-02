package org.safehaus.subutai.core.network.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.AgentUtil;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Commands for NetworkManager
 */
public class Commands
{

    private final CommandRunner commandRunner;


    public Commands( final CommandRunner commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    public Command getCreateSSHCommand( List<Agent> agentList )
    {
        return commandRunner.createCommand( new RequestBuilder( "rm -Rf /root/.ssh && " +
                "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ), Sets.newHashSet( agentList ) );
    }


    public Command getReadSSHCommand( List<Agent> agentList )
    {
        return commandRunner
                .createCommand( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ), new HashSet<>( agentList ) );
    }


    public Command getWriteSSHCommand( List<Agent> agentList, String key )
    {
        return commandRunner.createCommand( new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "echo '%s' > /root/.ssh/authorized_keys && " +
                "chmod 644 /root/.ssh/authorized_keys", key ) ), new HashSet<>( agentList ) );
    }


    public Command getConfigSSHCommand( List<Agent> agentList )
    {
        return commandRunner.createCommand( new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                "chmod 644 /root/.ssh/config" ), new HashSet<>( agentList ) );
    }


    public Command getAddIpHostToEtcHostsCommand( String domainName, Set<Agent> agents )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();

        for ( Agent agent : agents )
        {
            String ip = AgentUtil.getAgentIpByMask( agent, Common.IP_MASK );
            String hostname = agent.getHostname();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( domainName ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }

        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( "' >> '/etc/hosts';" );

        return commandRunner
                .createCommand( "Add ip-host pair to /etc/hosts", new RequestBuilder( appendHosts.toString() ),
                        agents );
    }
}
