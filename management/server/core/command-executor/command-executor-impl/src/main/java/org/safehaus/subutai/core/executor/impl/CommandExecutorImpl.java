package org.safehaus.subutai.core.executor.impl;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Request;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Implementation of CommandExecutor
 */
public class CommandExecutorImpl implements CommandExecutor
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandExecutorImpl.class.getName() );

    private final Broker broker;
    private final HostRegistry hostRegistry;
    private CommandResponseListener responseListener;


    public CommandExecutorImpl( final Broker broker, final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( broker, "Broker is null" );
        Preconditions.checkNotNull( hostRegistry, "Container Registry is null" );

        this.broker = broker;
        this.hostRegistry = hostRegistry;
        this.responseListener = new CommandResponseListener();
    }


    @Override
    public CommandResult execute( final UUID hostId, final RequestBuilder requestBuilder )
    {
        return null;
    }


    @Override
    public CommandResult execute( final UUID hostId, final RequestBuilder requestBuilder,
                                  final CommandCallback callback )
    {
        return null;
    }


    @Override
    public void executeAsync( final UUID hostId, final RequestBuilder requestBuilder )
    {

    }


    @Override
    public void executeAsync( final UUID hostId, final RequestBuilder requestBuilder, final CommandCallback callback )
    {
        Preconditions.checkNotNull( hostId, "Invalid host id" );
        Preconditions.checkNotNull( requestBuilder, "Invalid request builder" );

        Request request = requestBuilder.build2( hostId );
    }


    public void init() throws CommandExecutorException
    {
        try
        {
            broker.addByteMessageListener( responseListener );
        }
        catch ( BrokerException e )
        {
            LOG.error( "Error in init", e );
            throw new CommandExecutorException( e );
        }
    }


    public void dispose()
    {
        broker.removeMessageListener( responseListener );
    }
}
