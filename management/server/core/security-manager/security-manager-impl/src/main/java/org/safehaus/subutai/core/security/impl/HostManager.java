package org.safehaus.subutai.core.security.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
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
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ), "Agent list is empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ), "Domain name is empty" );

        this.containerHosts = containerHosts;
        this.domainName = domainName;
        this.commands = new Commands();
    }


    public boolean execute() throws HostManagerException
    {
        return write();
    }


    private boolean write() throws HostManagerException
    {


        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) )
        {
            try
            {
                for ( ContainerHost containerHost : containerHosts )
                {
                    CommandResult command = containerHost.execute(
                            commands.getAddIpHostToEtcHostsCommand( domainName, Sets.newHashSet( containerHosts ) ) );
                    if ( !command.hasSucceeded() )
                    {
                        throw new HostManagerException( command.getStdOut() );
                    }
                }
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
                throw new HostManagerException( e.getMessage() );
            }
        }
        return true;
    }


    public boolean execute( ContainerHost containerHost ) throws HostManagerException
    {
        Preconditions.checkNotNull( containerHost, "Agent is null" );
        containerHosts.add( containerHost );
        return write();
    }
}
