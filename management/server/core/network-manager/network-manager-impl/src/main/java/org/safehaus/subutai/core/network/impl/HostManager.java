package org.safehaus.subutai.core.network.impl;


import java.util.Set;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.peer.api.ContainerHost;
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

    private Set<ContainerHost> containerHosts;
    private String domainName;
    private Commands commands;


    public HostManager( Set<ContainerHost> containerHosts, String domainName )
    {
        Preconditions.checkNotNull( commands, "Commands are null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ), "Agent list is empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ), "Domain name is empty" );

        this.containerHosts = containerHosts;
        this.domainName = domainName;
    }


    public boolean execute()
    {
        return write();
    }


    private boolean write()
    {


        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) )
        {
            try
            {
                for ( ContainerHost host : containerHosts )
                {
                    CommandResult command = host.execute(
                            commands.getAddIpHostToEtcHostsCommand( domainName, Sets.newHashSet( containerHosts ) ) );
                    if ( !command.hasSucceeded() )
                    {
                        return false;
                    }
                }
                return true;
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
            }
        }
        return false;
    }


    public boolean execute( ContainerHost agent )
    {
        Preconditions.checkNotNull( agent, "Agent is null" );

        containerHosts.add( agent );
        return write();
    }
}
