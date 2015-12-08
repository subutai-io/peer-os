package io.subutai.core.drms.api;


import io.subutai.common.drms.TrustMessage;


/**
 * Created by talas on 12/7/15.
 */
public interface TrustMessageManager
{
    void decryptMessage( String encryptedMessage );

    void authenticateSource( TrustMessage trustMessage );

    void verifyMessageSource( TrustMessage trustMessage, Object source );

    void serializeMessage( TrustMessage message, Class clazz );
}
