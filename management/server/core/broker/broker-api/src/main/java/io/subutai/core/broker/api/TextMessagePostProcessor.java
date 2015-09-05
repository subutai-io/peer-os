package io.subutai.core.broker.api;


/**
 * Allows clients to intercept outgoing messages before they are sent.
 *
 * Messages can be altered by post-processors.
 */
public interface TextMessagePostProcessor extends TextMessageInterceptor
{


}
