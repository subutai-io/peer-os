package org.safehaus.subutai.core.containerregistry.impl;


import java.io.UnsupportedEncodingException;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;


/**
 * Listens to heartbeats from agents
 */
public class HeartBeatListener implements ByteMessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( HeartBeatListener.class.getName() );
    private final ContainerRegistryImpl registry;


    public HeartBeatListener( final ContainerRegistryImpl registry )
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
            HeartBeat heartBeat = JsonUtil.fromJson( response, HeartBeat.class );

            registry.registerHost( heartBeat.getHostInfo() );
        }
        catch ( JsonSyntaxException | UnsupportedEncodingException e )
        {
            LOG.error( "Error processing heartbeat", e );
        }
    }
}
