package org.safehaus.subutai.core.broker.impl;


import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;


/**
 * Listens to responses from agents
 */
public class ResponseListener implements ByteMessageListener
{
    @Override
    public void onMessage( final byte[] message )
    {

    }


    @Override
    public Topic getTopic()
    {
        return Topic.RESPONSE_TOPIC;
    }
}
