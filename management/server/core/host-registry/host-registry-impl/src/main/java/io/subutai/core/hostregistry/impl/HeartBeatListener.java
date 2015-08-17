package io.subutai.core.hostregistry.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.Topic;


/**
 * Listens to heartbeats from agents
 */
public class HeartBeatListener implements ByteMessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( HeartBeatListener.class.getName() );
    private final HostRegistryImpl registry;
    protected JsonUtil jsonUtil = new JsonUtil();


    public HeartBeatListener( final HostRegistryImpl registry )
    {
        this.registry = registry;
    }


    @Override
    public Topic getTopic()
    {
        return Topic.HEARTBEAT_TOPIC;
    }


    @Override
    public void onMessage( final byte[] message )
    {
        try
        {
            String response = new String( message, "UTF-8" );
            HeartBeat heartBeat = jsonUtil.from( response, HeartBeat.class );

            LOG.info( String.format( "%n<<<HEARTBEAT>>>%n%s%n", heartBeat.getHostInfo().toString() ) );

            registry.registerHost( heartBeat.getHostInfo() );
        }
        catch ( Exception e )
        {
            LOG.error( "Error processing heartbeat", e );
        }
    }
}
