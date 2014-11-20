package org.safehaus.subutai.core.broker.api;


/**
 * Allows to subscribe to messages from topics and send messages to them
 */
public interface Broker
{

    public void sendTextMessage( String topic, String message ) throws BrokerException;

    public void sendByteMessage( String topic, byte[] message ) throws BrokerException;

    public void addByteMessageListener( ByteMessageListener listener ) throws BrokerException;

    public void addTextMessageListener( TextMessageListener listener ) throws BrokerException;

    public void removeMessageListener( MessageListener listener );
}
