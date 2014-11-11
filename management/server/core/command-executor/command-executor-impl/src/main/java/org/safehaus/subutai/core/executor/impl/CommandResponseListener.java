package org.safehaus.subutai.core.executor.impl;


import java.io.UnsupportedEncodingException;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;


/**
 * Listens to command responses
 */
public class CommandResponseListener implements ByteMessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandResponseListener.class.getName() );


    @Override
    public Topic getTopic()
    {
        return Topic.RESPONSE_TOPIC;
    }


    @Override
    public void onMessage( final byte[] message )
    {
        try
        {
            String responseString = new String( message, "UTF-8" );

            ResponseImpl response = JsonUtil.fromJson( responseString, ResponseImpl.class );

            //TODO process response here
        }
        catch ( JsonSyntaxException | UnsupportedEncodingException e )
        {
            LOG.error( "Error processing response", e );
        }
    }
}
