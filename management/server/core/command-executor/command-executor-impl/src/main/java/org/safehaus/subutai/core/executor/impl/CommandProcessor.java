package org.safehaus.subutai.core.executor.impl;


import java.util.UUID;

import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Request;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Listens to command responses
 */
public class CommandProcessor implements ByteMessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandProcessor.class.getName() );

    private ExpiringCache<UUID, CommandProcess> commands = new ExpiringCache<>();
    private int inactiveCommandDropTimeout = Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC;
    private final Broker broker;
    private final HostRegistry hostRegistry;


    public CommandProcessor( final Broker broker, final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( broker);
        Preconditions.checkNotNull( hostRegistry);

        this.broker = broker;
        this.hostRegistry = hostRegistry;
    }


    @Override
    public Topic getTopic()
    {
        return Topic.RESPONSE_TOPIC;
    }


    public void execute( final Request request, CommandCallback callback ) throws CommandException
    {
        //find target host
        HostInfo targetHost = getTargetHost( request.getId() );
        if ( targetHost == null )
        {
            throw new CommandException( "Host is not connected" );
        }

        //create command process
        CommandProcess commandProcess = new CommandProcess( this, callback );
        boolean queued = commands.put( request.getCommandId(), commandProcess, inactiveCommandDropTimeout * 1000,
                new CommandProcessExpiryCallback() );
        if ( !queued )
        {
            throw new CommandException( "This command is already queued for execution" );
        }

        //send command
        try
        {
            commandProcess.start();

            broker.sendTextMessage( targetHost.getId().toString(), JsonUtil.toJson( request ) );
        }
        catch ( BrokerException e )
        {
            remove( request.getCommandId() );

            commandProcess.stop();

            throw new CommandException( e );
        }
    }


    private HostInfo getTargetHost( UUID hostId )
    {
        HostInfo targetHost = hostRegistry.getHostInfoById( hostId );
        if ( targetHost == null )
        {
            ContainerHostInfo containerHostInfo = hostRegistry.getContainerInfoById( hostId );
            if ( containerHostInfo != null )
            {
                targetHost = hostRegistry.getParentByChild( containerHostInfo );
            }
        }
        return targetHost;
    }


    protected void remove( UUID commandId )
    {
        Preconditions.checkNotNull( commandId );

        commands.remove( commandId );
    }


    public CommandResult getResult( UUID commandId ) throws CommandException
    {
        Preconditions.checkNotNull( commandId );

        CommandProcess commandProcess = commands.get( commandId );
        if ( commandProcess != null )
        {
            //wait until process completes  & return result
            return commandProcess.waitResult();
        }
        else
        {
            throw new CommandException( String.format( "Command process not found by id: %s", commandId ) );
        }
    }


    @Override
    public void onMessage( final byte[] message )
    {
        try
        {
            String responseString = new String( message, "UTF-8" );

            final ResponseImpl response = JsonUtil.fromJson( responseString, ResponseImpl.class );

            final CommandProcess commandProcess = commands.get( response.getCommandId() );

            if ( commandProcess != null )
            {
                //process response
                commandProcess.processResponse( response );
            }
            else
            {
                LOG.warn( "Callback not found for response: %s", response );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error processing response", e );
        }
    }
}
