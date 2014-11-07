package org.safehaus.subutai.core.agentregistry.impl;


import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;


/**
 * Listens to heartbeats from agents
 */
public class HeartBeatListener implements ByteMessageListener
{

    @Override
    public Topic getTopic()
    {
        return Topic.HEARTBEAT_TOPIC;
    }


    @Override
    public void onMessage( final byte[] message )
    {

    }
}
