package io.subutai.core.security.broker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.broker.api.ByteMessagePreProcessor;


/**
 * This class decrypts incoming messages
 */
public class MessageDecryptor implements ByteMessagePreProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageDecryptor.class.getName() );


    @Override
    public byte[] process( final String topic, final byte[] message )
    {
        LOG.info( String.format( "INCOMING:%s", new String( message ) ) );

        return message;
    }
}
