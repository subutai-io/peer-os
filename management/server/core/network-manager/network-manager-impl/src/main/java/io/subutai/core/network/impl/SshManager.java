package io.subutai.core.network.impl;


import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.network.api.NetworkManagerException;


/**
 * Ssh manager for adding/exchanging ssh keys and enabling password-less communication
 */
public class SshManager
{

    private static final Logger LOG = LoggerFactory.getLogger( SshManager.class.getName() );

    private Set<EnvironmentContainerHost> containerHosts;
    private Commands commands;

    protected List<String> keys;
    protected CommandUtil commandUtil;


    public SshManager( Set<EnvironmentContainerHost> containerHosts )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ), "Containers are empty" );
        this.containerHosts = containerHosts;
        this.commands = new Commands();
        this.commandUtil = new CommandUtil();
        this.keys = Lists.newArrayList();
    }


    public void appendSshKey( String key ) throws NetworkManagerException
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
                LOG.error( String.format( "Error in appendSshKey: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }
    }


    public void replaceSshKey( String oldKey, String newKey ) throws NetworkManagerException
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
                LOG.error( String.format( "Error in replaceSshKey: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }
    }


    public void removeSshKey( String key ) throws NetworkManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Invalid ssh key" );

        for ( ContainerHost host : containerHosts )
        {
            try
            {
                //execute via host to ignore error if authorized_keys file does not exist
                host.execute( commands.getRemoveSshKeyCommand( key ) );
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in removeSshKey: %s", e.getMessage() ), e );
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


    protected void create() throws NetworkManagerException
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


    protected void read() throws NetworkManagerException
    {
        for ( ContainerHost host : containerHosts )
        {
            try
            {
                CommandResult command = commandUtil.execute( commands.getReadSSHCommand(), host );
                if ( !Strings.isNullOrEmpty( command.getStdOut() ) )
                {
                    keys.add( command.getStdOut() );
                }
            }
            catch ( CommandException e )
            {
                LOG.error( String.format( "Error in read: %s", e.getMessage() ), e );
                throw new NetworkManagerException( e );
            }
        }

        if ( keys.isEmpty() )
        {
            throw new NetworkManagerException( "Could not read ssh keys from containers" );
        }
    }


    protected void write() throws NetworkManagerException
    {
        for ( ContainerHost host : containerHosts )
        {
            int i = 0;
            StringBuilder keyBuilder = new StringBuilder();
            for ( String key : keys )
            {
                keyBuilder.append( key );
                i++;
                if ( i % 5 == 0 || i == keys.size() )
                {
                    try
                    {
                        commandUtil.execute( commands.getAppendSshKeyCommand( keyBuilder.toString() ), host );

                        keyBuilder.setLength( 0 );
                    }
                    catch ( CommandException e )
                    {
                        LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
                        throw new NetworkManagerException( e );
                    }
                }
            }
        }
    }


    protected void config() throws NetworkManagerException
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
