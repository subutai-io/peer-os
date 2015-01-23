package org.safehaus.subutai.core.network.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * HostManager enables to register container's hostname in /etc/hosts file of other agents
 */
public class HostManager
{
    private static final Logger LOG = LoggerFactory.getLogger( HostManager.class.getName() );

    private Set<ContainerHost> containerHosts;
    private String domainName;
    private Commands commands;
    private CommandUtil commandUtil;


    public HostManager( Set<ContainerHost> containerHosts, String domainName )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ), "Containers are empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ), "Domain name is empty" );

        this.containerHosts = containerHosts;
        this.domainName = domainName;
        this.commands = new Commands();
        this.commandUtil = new CommandUtil();
    }


    public void execute() throws NetworkManagerException
    {
        write();
    }


    private void write() throws NetworkManagerException
    {

        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) )
        {
            try
            {
                for ( ContainerHost containerHost : containerHosts )
                {
                    commandUtil.execute(
                            commands.getAddIpHostToEtcHostsCommand( domainName, Sets.newHashSet( containerHosts ) ),
                            containerHost );
                }
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }
    }


    public void execute( ContainerHost containerHost ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( containerHost, "Container is null" );

        containerHosts.add( containerHost );
        write();
    }
}
