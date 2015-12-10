package io.subutai.core.identity.api.relation;


import io.subutai.common.drms.TrustMessage;


/**
 * Created by talas on 12/7/15.
 */


/**
 * TrustMEssageManager is needed to process incoming signed messages to construct trust relationships
 */
public interface TrustMessageManager
{
    /**
     * Decrypt with management private key
     *
     * @param encryptedMessage - Encrypted message where trust relationship is declared
     */
    void decryptMessage( String encryptedMessage );

    /**
     * Verify that trust relationship is from trusted source
     * @param trustMessage - signed trust relationship message
     */
    void authenticateSource( TrustMessage trustMessage );

    /**
     * Verify that decrypted message is signed by trusted source
     * @param trustMessage - signed message
     * @param source - source
     */
    void verifyMessageSource( TrustMessage trustMessage, Object source );


    void serializeMessage( TrustMessage message, Class clazz );
}
