package io.subutai.core.broker.api;


/**
 * Allows clients to intercept incoming messages before they are sent.
 *
 * Messages can be altered by pre-processors.
 */
public interface TextMessagePreProcessor extends TextMessageInterceptor
{

}
