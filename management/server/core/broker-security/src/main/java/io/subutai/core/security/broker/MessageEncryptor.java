package io.subutai.core.security.broker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.broker.api.TextMessagePostProcessor;


/**
 * This class encrypts outgoing messages
 */
public class MessageEncryptor implements TextMessagePostProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageEncryptor.class.getName() );


    @Override
    public String process( final String topic, final String message )
    {
        LOG.info( String.format( "OUTGOING:%s", message ) );

        return message;
    }
}
