package org.safehaus.subutai.core.security.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Ssh manager for exchanging ssh keys and enabling passwordless communication
 */
public class SshManager
{

    private static final Logger LOG = LoggerFactory.getLogger( SshManager.class.getName() );

    private Set<ContainerHost> containerHosts;

    private String keys;


    public SshManager( Set<ContainerHost> containerHosts )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ), "Agent list is empty" );
        this.containerHosts = containerHosts;
    }


    public boolean execute() throws SSHManagerException
    {
        try
        {
            return create() && read() && write() && config();
        }
        catch ( CommandException | SSHManagerException e )
        {
            LOG.error( e.getMessage(), e );
            throw new SSHManagerException( e.getMessage() );
        }
    }


    private boolean create() throws CommandException, SSHManagerException
    {

        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) )
        {
            for ( ContainerHost host : containerHosts )
            {
                try
                {
                    CommandResult command = host.execute( new RequestBuilder( "rm -Rf /root/.ssh && " +
                            "mkdir -p /root/.ssh && " +
                            "chmod 700 /root/.ssh && " +
                            "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ) );

                    if ( !command.hasSucceeded() )
                    {
                        throw new SSHManagerException( command.getStdOut() );
                    }
                }
                catch ( CommandException e )
                {
                    LOG.error( String.format( "Error in create: %s", e.getMessage() ), e );
                    throw new SSHManagerException( e.getMessage() );
                }
            }
        }
        return true;
    }


    private boolean read() throws SSHManagerException
    {
        StringBuilder value = new StringBuilder();
        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) )
        {
            for ( ContainerHost host : containerHosts )
            {
                try
                {
                    CommandResult command = host.execute( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ) );
                    if ( !command.hasSucceeded() )
                    {
                        throw new SSHManagerException( command.getStdErr() );
                    }
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
        }
        keys = value.toString();
        return !Strings.isNullOrEmpty( keys );
    }


    private boolean write() throws SSHManagerException
    {
        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) )
        {
            for ( ContainerHost host : containerHosts )
            {
                try
                {
                    CommandResult command = host.execute( new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                            "chmod 700 /root/.ssh && " +
                            "echo '%s' > /root/.ssh/authorized_keys && " +
                            "chmod 644 /root/.ssh/authorized_keys", keys ) ) );

                    if ( !command.hasSucceeded() )
                    {
                        throw new SSHManagerException( command.getStdOut() );
                    }
                }
                catch ( CommandException e )
                {
                    LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
                    throw new SSHManagerException( e.getMessage() );
                }
            }
        }
        return true;
    }


    private boolean config() throws SSHManagerException
    {

        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) )
        {
            for ( ContainerHost host : containerHosts )
            {
                try
                {
                    CommandResult command = host.execute( new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                            "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                            "chmod 644 /root/.ssh/config" ) );

                    if ( !command.hasSucceeded() )
                    {
                        throw new SSHManagerException( command.getStdOut() );
                    }
                }
                catch ( CommandException e )
                {
                    LOG.error( String.format( "Error in config: %s", e.getMessage() ), e );
                    throw new SSHManagerException( e.getMessage() );
                }
            }
        }
        return true;
    }


    public boolean execute( ContainerHost containerHost ) throws SSHManagerException
    {
        Preconditions.checkNotNull( containerHost, "Container host is null" );

        if ( create( containerHost ) )
        {
            containerHosts.add( containerHost );

            try
            {
                read();
                write();
                config();
            }
            catch ( SSHManagerException e )
            {
                LOG.error( e.getMessage(), e );
                throw new SSHManagerException( e.getMessage() );
            }
        }
        return true;
    }


    private boolean create( ContainerHost containerHost ) throws SSHManagerException
    {
        try
        {
            CommandResult command = containerHost.execute( new RequestBuilder( "rm -Rf /root/.ssh && " +
                    "mkdir -p /root/.ssh && " +
                    "chmod 700 /root/.ssh && " +
                    "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ) );

            if ( !command.hasSucceeded() )
            {
                throw new SSHManagerException( command.getStdOut() );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in create: %s", e.getMessage() ), e );
            throw new SSHManagerException( e.getMessage() );
        }
        return true;
    }
}
