package org.safehaus.subutai.core.network.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.AgentUtil;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Commands for NetworkManager
 */
public class Commands
{

    private final CommandDispatcher commandDispatcher;


    public Commands( final CommandDispatcher commandDispatcher )
    {
        Preconditions.checkNotNull( commandDispatcher, "Command Dispatcher is null" );

        this.commandDispatcher = commandDispatcher;
    }


    public Command getCreateSSHCommand( List<Agent> agentList )
    {
        return commandDispatcher.createCommand( new RequestBuilder( "rm -Rf /root/.ssh && " +
                "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ), Sets.newHashSet( agentList ) );
    }


    public Command getCreateSSHCommand( Set<Container> containers )
    {
        return commandDispatcher.createContainerCommand( new RequestBuilder( "rm -Rf /root/.ssh && " +
                "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ), containers );
    }


    public Command getReadSSHCommand( List<Agent> agentList )
    {
        return commandDispatcher
                .createCommand( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ), Sets.newHashSet( agentList ) );
    }


    public Command getReadSSHCommand( Set<Container> containers )
    {
        return commandDispatcher
                .createContainerCommand( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ), containers );
    }


    public Command getWriteSSHCommand( List<Agent> agentList, String key )
    {
        return commandDispatcher.createCommand( new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "echo '%s' > /root/.ssh/authorized_keys && " +
                "chmod 644 /root/.ssh/authorized_keys", key ) ), Sets.newHashSet( agentList ) );
    }


    public Command getWriteSSHCommand( Set<Container> containers, String key )
    {
        return commandDispatcher.createContainerCommand( new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "echo '%s' > /root/.ssh/authorized_keys && " +
                "chmod 644 /root/.ssh/authorized_keys", key ) ), containers );
    }


    public Command getConfigSSHCommand( List<Agent> agentList )
    {
        return commandDispatcher.createCommand( new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                "chmod 644 /root/.ssh/config" ), new HashSet<>( agentList ) );
    }


    public Command getConfigSSHCommand( Set<Container> containers )
    {
        return commandDispatcher.createContainerCommand( new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                "chmod 644 /root/.ssh/config" ), containers );
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

        return commandDispatcher
                .createCommand( "Add ip-host pair to /etc/hosts", new RequestBuilder( appendHosts.toString() ),
                        agents );
    }


    public Command getEtcHostsCommand( String domainName, Set<Container> containers )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();

        for ( Container container : containers )
        {
            String ip = AgentUtil.getContainerIpByMask( container, Common.IP_MASK );
            String hostname = container.getHostname();
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

        return commandDispatcher.createContainerCommand( new RequestBuilder( appendHosts.toString() ), containers );
    }
}
