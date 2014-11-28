package org.safehaus.subutai.core.hostregistry.impl;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            LOG.info( response );
            HeartBeat heartBeat = jsonUtil.from( response, HeartBeat.class );

            registry.registerHost( heartBeat.getHostInfo() );
        }
        catch ( Exception e )
        {
            LOG.error( "Error processing heartbeat", e );
        }
    }
}
