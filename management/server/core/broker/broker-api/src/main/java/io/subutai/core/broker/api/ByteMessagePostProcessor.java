package io.subutai.core.broker.api;


/**
 * Allows clients to intercept outgoing messages before they are sent.
 *
 * Messages can be altered by clients.
 */
public interface ByteMessagePostProcessor extends ByteMessageInterceptor
{


}
