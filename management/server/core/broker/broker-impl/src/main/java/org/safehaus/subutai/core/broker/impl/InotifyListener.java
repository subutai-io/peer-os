package org.safehaus.subutai.core.broker.impl;


import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;


/**
 * Listens to inotify events from agents
 */
public class InotifyListener implements ByteMessageListener
{
    @Override
    public void onMessage( final byte[] message )
    {

    }


    @Override
    public Topic getTopic()
    {
        return Topic.INOTIFY_TOPIC;
    }
}
