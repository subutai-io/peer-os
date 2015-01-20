package org.safehaus.subutai.core.security.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.common.peer.ContainerHost;
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
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ), "Agent list is empty" );
        this.containerHosts = containerHosts;
        this.commands = new Commands();
        this.commandUtil = new CommandUtil();
    }


    public void append( String key ) throws SSHManagerException
    {
        for ( ContainerHost host : containerHosts )
        {
            try
            {
                commandUtil.execute( commands.getAppendSSHCommand( key ), host );
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in append: %s", e.getMessage() ), e );
                throw new SSHManagerException( e.getMessage() );
            }
        }
    }


    public void execute() throws SSHManagerException
    {
        create();
        read();
        write();
        config();
    }


    public void execute( ContainerHost containerHost ) throws SSHManagerException
    {
        Preconditions.checkNotNull( containerHost, "Container host is null" );

        create( containerHost );
        containerHosts.add( containerHost );
        read();
        write();
        config();
    }


    private void create() throws SSHManagerException
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
                throw new SSHManagerException( e.getMessage() );
            }
        }
    }


    private void read() throws SSHManagerException
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
                throw new SSHManagerException( e.getMessage() );
            }
        }

        if ( Strings.isNullOrEmpty( value.toString() ) )
        {
            throw new SSHManagerException( "Could not read ssh keys from containers" );
        }

        keys = value.toString();
    }


    private void write() throws SSHManagerException
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
                throw new SSHManagerException( e.getMessage() );
            }
        }
    }


    private void config() throws SSHManagerException
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
                throw new SSHManagerException( e.getMessage() );
            }
        }
    }


    private void create( ContainerHost containerHost ) throws SSHManagerException
    {
        try
        {
            commandUtil.execute( commands.getCreateSSHCommand(), containerHost );
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in create: %s", e.getMessage() ), e );
            throw new SSHManagerException( e.getMessage() );
        }
    }
}
