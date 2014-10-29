package org.safehaus.subutai.core.network.impl;


import java.util.Set;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
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


    public boolean execute()
    {

        return create() && read() && write() && config();
    }


    private boolean create()
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

                    return command.hasSucceeded();
                }
                catch ( CommandException e )
                {
                    LOG.error( String.format( "Error in create: %s", e.getMessage() ), e );
                }
            }
        }
        return false;
    }


    private boolean read()
    {
        if ( !CollectionUtil.isCollectionEmpty( containerHosts ) )
        {
            for ( ContainerHost host : containerHosts )
            {
                try
                {
                    CommandResult command = host.execute( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ) );
                    StringBuilder value = new StringBuilder();
                    if ( !command.hasCompleted() )
                    {
                        return false;
                    }

                    if ( !Strings.isNullOrEmpty( command.getStdOut() ) )
                    {
                        value.append( command.getStdOut() );
                    }

                    keys = value.toString();

                    return !Strings.isNullOrEmpty( keys ) && command.hasSucceeded();
                }
                catch ( CommandException e )
                {
                    LOG.error( String.format( "Error in read: %s", e.getMessage() ), e );
                }
            }
        }
        return false;
    }


    private boolean write()
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

                    return command.hasSucceeded();
                }
                catch ( CommandException e )
                {
                    LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
                }
            }
        }
        return false;
    }


    private boolean config()
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

                    return command.hasSucceeded();
                }
                catch ( CommandException e )
                {
                    LOG.error( String.format( "Error in config: %s", e.getMessage() ), e );
                }
            }
        }
        return false;
    }


    public boolean execute( ContainerHost containerHost )
    {
        Preconditions.checkNotNull( containerHost, "Agent is null" );

        if ( create( containerHost ) )
        {
            containerHosts.add( containerHost );

            return read() && write() && config();
        }
        return false;
    }


    private boolean create( ContainerHost containerHost )
    {
        try
        {
            CommandResult command = containerHost.execute( new RequestBuilder( "rm -Rf /root/.ssh && " +
                    "mkdir -p /root/.ssh && " +
                    "chmod 700 /root/.ssh && " +
                    "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ) );

            return command.hasSucceeded();
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in create: %s", e.getMessage() ), e );
        }
        return false;
    }
}
