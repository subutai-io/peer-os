package io.subutai.core.network.impl;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.network.api.NetworkManagerException;


/**
 * HostManager enables to register container's hostname in /etc/hosts file of other agents
 */
public class HostManager
{
    private static final Logger LOG = LoggerFactory.getLogger( HostManager.class.getName() );

    private Set<ContainerHost> containerHosts;
    private String domainName;
    private Commands commands;
    protected CommandUtil commandUtil;


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
}
