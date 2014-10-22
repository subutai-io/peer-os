package org.safehaus.subutai.core.peer.api;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.NullAgent;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;


/**
 * Base class for Host.
 */
public abstract class Host implements Serializable
{
    private static final Logger LOG = LoggerFactory.getLogger( Host.class );

    private Agent agent = new NullAgent();
    private long lastHeartbeat;
    private long inactiveTime = 5 * 1000 * 60; // 5 min
    private Host parentHost;
    private Set<Host> slaveHosts = new HashSet<>();


    public Agent getAgent()
    {
        return agent;
    }


    public void setAgent( final Agent agent )
    {
        this.agent = agent;
    }


    public UUID getPeerId() throws PeerException
    {
        if ( agent == null )
        {
            throw new PeerException( "Could not determine peer ID. Agent is null." );
        }
        return agent.getSiteId();
    }


    public abstract void invoke( PeerCommandMessage commandMessage ) throws PeerException;


    public Command execute( RequestBuilder requestBilder ) throws CommandException
    {
        if ( agent == null || !isConnected() )
        {
            throw new CommandException( "Could not execute command. Agent is null or disconnected" );
        }
        CommandDispatcher commandDispatcher = getCommandDispatcher();

        Command command = commandDispatcher.createCommand( requestBilder, Sets.newHashSet( agent ) );
        command.execute();
        return command;
    }


    public void executeAsync( RequestBuilder requestBilder, CommandCallback commandCallback ) throws CommandException
    {
        CommandDispatcher commandDispatcher = getCommandDispatcher();

        Command command = commandDispatcher.createCommand( requestBilder, Sets.newHashSet( agent ) );
        command.executeAsync( commandCallback );
    }


    private CommandDispatcher getCommandDispatcher() throws CommandException
    {
        CommandDispatcher commandDispatcher = null;
        try
        {
            ServiceLocator.getServiceNoCache( CommandDispatcher.class );
        }
        catch ( NamingException e )
        {
            throw new CommandException( e.toString() );
        }

        return commandDispatcher;
    }


    public boolean isConnected()
    {
        return System.currentTimeMillis() - lastHeartbeat > inactiveTime;
    }


    public String echo( String text ) throws CommandException
    {
        RequestBuilder requestBuilder = new RequestBuilder( "echo " + text );
        Command command = execute( requestBuilder );
        if ( command.hasSucceeded() )
        {
            Map<UUID, AgentResult> results = command.getResults();
            StringBuilder sb = new StringBuilder();
            for ( UUID agentId : results.keySet() )
            {
                if ( agentId.equals( agent.getUuid() ) )
                {
                    sb.append( results.get( agentId ).getStdOut() );
                }
                else
                {
                    throw new CommandException( "Unknown agentID in AgentResult." );
                }
            }
            return sb.toString();
        }
        else
        {
            throw new CommandException( "Echo execution error: " + command.getAllErrors() );
        }
    }


    public void updateHeartBeat()
    {
        this.lastHeartbeat = System.currentTimeMillis();
    }


    public abstract String getHostname();


    public void addSlaveHost( Host host )
    {
        LOG.info( String.format( "Adding new slave host to %s: %s", host.getParentHost(), host.getHostname() ) );
        if ( host == null )
        {
            throw new IllegalArgumentException( "Slave host could not be null." );
        }
        host.setParentHost( this );
        slaveHosts.add( host );
    }


    public Host getParentHost()
    {
        return this.parentHost;
    }


    public void setParentHost( Host host )
    {
        this.parentHost = host;
    }


    public Host getChildHost( final String hostname )
    {
        Host result = null;
        Iterator<Host> iterator = slaveHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            Host host = iterator.next();
            if ( hostname.equals( host.getHostname() ) )
            {
                result = host;
            }
        }
        return result;
    }


    public Host getChildHost( final UUID uuid )
    {
        Host result = null;
        Iterator<Host> iterator = slaveHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            Host host = iterator.next();
            if ( uuid.equals( host.getAgent().getUuid() ) )
            {
                result = host;
            }
        }
        return result;
    }


    public Set<Host> getSlaveHosts()
    {
        return slaveHosts;
    }


    public void setSlaveHosts( final Set<Host> slaveHosts )
    {
        this.slaveHosts = slaveHosts;
    }
}
