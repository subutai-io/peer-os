package io.subutai.core.identity.api.relation;


import java.io.UnsupportedEncodingException;

import org.bouncycastle.openpgp.PGPException;

import io.subutai.core.identity.api.exception.RelationVerificationException;
import io.subutai.core.identity.api.model.Relation;


/**
 * Created by talas on 12/7/15.
 */


/**
 * TrustMEssageManager is needed to process incoming signed messages to construct trust relationships
 */
public interface RelationMessageManager
{
    /**
     * Decrypt with management private key
     *
     * @param encryptedMessage - Encrypted message where trust relationship is declared
     * @param secretKeyId
     */
    Relation decryptAndVerifyMessage( String encryptedMessage, final String secretKeyId )
            throws PGPException, UnsupportedEncodingException, RelationVerificationException;

    /**
     * Get message sender's key fingerprint
     *
     * @param trustMessage - signed trust relationship message
     */
    String authenticateSource( Relation trustMessage );

    /**
     * Verify that decrypted message is signed by trusted source, message verification should be done by comparing
     * trustMessage properties appended into one string with sourceFingerprint public key
     *
     * @param trustMessage - signed message
     * @param signature - message signature
     * @param sourceFingerprint - sender's fingerprint
     */
    boolean verifyMessageSource( Relation trustMessage, String signature, String sourceFingerprint );
}
