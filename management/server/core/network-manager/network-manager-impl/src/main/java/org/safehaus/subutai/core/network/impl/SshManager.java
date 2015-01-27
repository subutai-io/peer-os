package org.safehaus.subutai.core.network.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Ssh manager for adding/exchanging ssh keys and enabling password-less communication
 */
public class SshManager
{

    private static final Logger LOG = LoggerFactory.getLogger( SshManager.class.getName() );

    private Set<ContainerHost> containerHosts;

    private String keys;
    private Commands commands;
    private CommandUtil commandUtil;


    public SshManager( Set<ContainerHost> containerHosts )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ), "Containers are empty" );
        this.containerHosts = containerHosts;
        this.commands = new Commands();
        this.commandUtil = new CommandUtil();
    }


    public void append( String key ) throws NetworkManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Invalid ssh key" );

        for ( ContainerHost host : containerHosts )
        {
            try
            {
                commandUtil.execute( commands.getAppendSshKeyCommand( key ), host );
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in append: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }
    }


    public void replace( String oldKey, String newKey ) throws NetworkManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( oldKey ), "Invalid old ssh key" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newKey ), "Invalid new ssh key" );

        for ( ContainerHost host : containerHosts )
        {
            try
            {
                commandUtil.execute( commands.getReplaceSshKeyCommand( oldKey, newKey ), host );
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in replace: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }
    }


    public void execute() throws NetworkManagerException
    {
        create();
        read();
        write();
        config();
    }


    private void create() throws NetworkManagerException
    {
        for ( ContainerHost host : containerHosts )
        {
            try
            {
                commandUtil.execute( commands.getCreateSSHCommand(), host );
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in create: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }
    }


    private void read() throws NetworkManagerException
    {
        StringBuilder value = new StringBuilder();

        for ( ContainerHost host : containerHosts )
        {
            try
            {
                CommandResult command = commandUtil.execute( commands.getReadSSHCommand(), host );
                if ( !Strings.isNullOrEmpty( command.getStdOut() ) )
                {
                    value.append( command.getStdOut() );
                }
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in read: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }

        if ( Strings.isNullOrEmpty( value.toString() ) )
        {
            throw new NetworkManagerException( "Could not read ssh keys from containers" );
        }

        keys = value.toString();
    }


    private void write() throws NetworkManagerException
    {
        for ( ContainerHost host : containerHosts )
        {
            try
            {
                commandUtil.execute( commands.getWriteSSHCommand( keys ), host );
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }
    }


    private void config() throws NetworkManagerException
    {
        for ( ContainerHost host : containerHosts )
        {
            try
            {
                commandUtil.execute( commands.getConfigSSHCommand(), host );
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in config: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }
    }
}
