package org.safehaus.subutai.core.network.impl;


import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
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
    private String domainName;
    private Commands commands;


    public HostManager( Commands commands, List<Agent> agentList, String domainName )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( agentList ), "Agent list is empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ), "Domain name is empty" );

        this.agentList = agentList;
        this.domainName = domainName;
        this.commands = commands;
    }


    public boolean execute()
    {
        return write();
    }


    private boolean write()
    {

        Command command = commands.getAddIpHostToEtcHostsCommand( domainName, Sets.newHashSet( agentList ) );
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
}
