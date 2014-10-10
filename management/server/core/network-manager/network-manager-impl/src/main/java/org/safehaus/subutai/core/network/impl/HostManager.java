package org.safehaus.subutai.core.network.impl;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * HostManager enables to register agent's hostname in /etc/hosts file of other agents
 */
public class HostManager
{
    private static final Logger LOG = LoggerFactory.getLogger( HostManager.class.getName() );

    private List<Agent> agentList;
    private Set<Container> containers;
    private String domainName;
    private Commands commands;


    public HostManager( Commands commands, List<Agent> agentList, String domainName )
    {
        Preconditions.checkNotNull( commands, "Commands are null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( agentList ), "Agent list is empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ), "Domain name is empty" );

        this.agentList = agentList;
        this.domainName = domainName;
        this.commands = commands;
    }


    public HostManager( final Set<Container> containers, final String domainName, final Commands commands )
    {
        Preconditions.checkNotNull( commands, "Commands are null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containers ), "Containers are empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ), "Domain name is empty" );

        this.containers = containers;
        this.domainName = domainName;
        this.commands = commands;
    }


    public boolean execute()
    {
        return write();
    }


    private boolean write()
    {

        Command command;
        if ( !CollectionUtil.isCollectionEmpty( agentList ) )
        {
            command = commands.getAddIpHostToEtcHostsCommand( domainName, Sets.newHashSet( agentList ) );
        }
        else
        {
            command = commands.getEtcHostsCommand( domainName, containers );
        }

        try
        {
            command.execute();
            return command.hasSucceeded();
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
        }
        return false;
    }


    public boolean execute( Agent agent )
    {
        Preconditions.checkNotNull( agent, "Agent is null" );

        agentList.add( agent );
        return write();
    }


    public boolean execute( Container container )
    {
        Preconditions.checkNotNull( container, "Container is null" );

        containers.add( container );
        return write();
    }
}
