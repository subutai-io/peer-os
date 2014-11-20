package org.safehaus.subutai.core.container.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.container.api.Container;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.container.api.ContainerState;
import org.safehaus.subutai.core.container.api.ContainerType;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Created by timur on 9/13/14.
 */
public class ContainerImpl implements Container
{
    private Agent agent;

    private ContainerManager containerManager;
    private AgentManager agentManager;
    private CommandRunner commandRunner;


    public ContainerImpl( Agent agent, ContainerManager containerManager, AgentManager agentManager,
                          CommandRunner commandRunner )
    {
        this.agent = agent;
        this.containerManager = containerManager;
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
    }


    @Override
    public void execute( Command command )
    {
        commandRunner.runCommand( command );
    }


    @Override
    public void execute( final RequestBuilder requestBuilder ) throws CommandException
    {
        Command command = commandRunner.createCommand( requestBuilder, Sets.newHashSet( agent ) );
        command.execute();
    }


    @Override
    public boolean start()
    {
        return containerManager
                .startLxcOnHost( agentManager.getAgentByHostname( agent.getParentHostName() ), agent.getHostname() );
    }


    @Override
    public boolean stop()
    {
        return containerManager
                .stopLxcOnHost( agentManager.getAgentByHostname( agent.getParentHostName() ), agent.getHostname() );
    }


    @Override
    public boolean isConnected()
    {
        return agentManager.getAgentByUUID( agent.getUuid() ) != null;
    }


    @Override
    public ContainerState getContainerState()
    {
        ContainerState result = ContainerState.UNKNOWN;
        if ( ContainerType.PHYSICAL.equals( getContainerType() ) )
        {
            return result;
        }

        Agent parentAgent = agentManager.getAgentByHostname( agent.getParentHostName() );

        if ( parentAgent != null )
        {
            Command getLxcListCommand =
                    new Commands( commandRunner ).getLxcListCommand( Sets.newHashSet( parentAgent ) );
            commandRunner.runCommand( getLxcListCommand );

            if ( getLxcListCommand.hasCompleted() )
            {
                for ( AgentResult agentResult : getLxcListCommand.getResults().values() )
                {
                    result = getContainerState( agentResult.getStdOut() );
                }
            }
        }
        return result;
    }


    private ContainerState getContainerState( final String agentResult )
    {
        ContainerState result = ContainerState.UNKNOWN;
        if ( Strings.isNullOrEmpty( agentResult ) )
        {
            return result;
        }

        String[] lxcStrs = agentResult.split( "\\n" );

        for ( int i = 2; i < lxcStrs.length && result.equals( ContainerState.UNKNOWN ); i++ )
        {
            String[] lxcProperties = lxcStrs[i].split( "\\s+" );
            if ( lxcProperties.length > 1 )
            {
                String lxcHostname = lxcProperties[0];

                if ( agent.getHostname().equals( lxcHostname ) )
                {
                    String lxcStatus = lxcProperties[1];
                    result = ContainerState.parseState( lxcStatus );
                }
            }
        }
        return result;
    }


    @Override
    public ContainerType getContainerType()
    {
        return agent.isLXC() ? ContainerType.LOGICAL : ContainerType.PHYSICAL;
    }


    @Override
    public void destroy() throws ContainerDestroyException
    {
        containerManager.destroy( agent.getParentHostName(), Sets.newHashSet( agent.getHostname() ) );
    }


    @Override
    public Set<Container> getLogicalContainers()
    {
        Set<Container> result = new HashSet<>();
        if ( ContainerType.PHYSICAL.equals( getContainerType() ) )
        {
            for ( Agent a : agentManager.getLxcAgentsByParentHostname( agent.getHostname() ) )
            {
                result.add( new ContainerImpl( a, containerManager, agentManager, commandRunner ) );
            }
        }
        return result;
    }


    @Override
    public Agent getAgent()
    {
        return agent;
    }
}

