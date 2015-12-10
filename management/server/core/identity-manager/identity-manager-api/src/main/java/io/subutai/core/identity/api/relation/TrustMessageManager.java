package io.subutai.core.identity.api.relation;


import java.io.UnsupportedEncodingException;

import org.bouncycastle.openpgp.PGPException;

import io.subutai.core.identity.api.model.TrustRelation;
import io.subutai.core.identity.api.model.TrustRelationship;


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
    TrustRelation decryptAndVerifyMessage( String encryptedMessage ) throws PGPException, UnsupportedEncodingException;

    /**
     * Get message sender's key fingerprint
     *
     * @param trustMessage - signed trust relationship message
     */
    String authenticateSource( TrustRelation trustMessage );

    /**
     * Verify that decrypted message is signed by trusted source, message verification should be done by comparing
     * trustMessage properties appended into one string with sourceFingerprint public key
     *
     * @param trustMessage - signed message
     * @param signature - message signature
     * @param sourceFingerprint - sender's fingerprint
     */
    boolean verifyMessageSource( TrustRelation trustMessage, String signature, String sourceFingerprint );


    TrustRelationship serializeMessage( String rawRelationship );
}
