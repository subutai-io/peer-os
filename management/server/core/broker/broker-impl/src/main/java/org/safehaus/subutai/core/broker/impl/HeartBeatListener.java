package org.safehaus.subutai.core.broker.impl;


import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;


/**
 * Listens to heartbeats from agents
 */
public class HeartBeatListener implements ByteMessageListener
{
    @Override
    public void onMessage( final byte[] message )
    {

    }


    @Override
    public Topic getTopic()
    {
        return Topic.HEARTBEAT_TOPIC;
    }
}
