package org.safehaus.subutai.core.broker.impl;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Routes message to listeners
 */
public class MessageRoutingListener implements MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageRoutingListener.class.getName() );

    protected Set<org.safehaus.subutai.core.broker.api.MessageListener> listeners = Collections
            .newSetFromMap( new ConcurrentHashMap<org.safehaus.subutai.core.broker.api.MessageListener, Boolean>() );

    protected ExecutorService notifier = Executors.newCachedThreadPool();


    public void addListener( org.safehaus.subutai.core.broker.api.MessageListener listener )
    {
        listeners.add( listener );
    }


    public void removeListener( org.safehaus.subutai.core.broker.api.MessageListener listener )
    {
        listeners.remove( listener );
    }


    @Override
    public void onMessage( final Message message )
    {
        try
        {
            //assume we are always using topics
            Topic destination = ( Topic ) message.getJMSDestination();

            for ( org.safehaus.subutai.core.broker.api.MessageListener listener : listeners )
            {
                if ( listener.getTopic().name().equalsIgnoreCase( destination.getTopicName() ) )
                {
                    notifyListener( listener, message );
                }
            }
        }
        catch ( JMSException e )
        {
            LOG.error( "Error in onMessage", e );
        }
    }


    protected void notifyListener( org.safehaus.subutai.core.broker.api.MessageListener listener, Message message )
    {
        notifier.execute( new MessageNotifier( listener, message ) );
    }


    protected void dispose()
    {
        notifier.shutdown();
    }
}
