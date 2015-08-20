package io.subutai.core.security.api.crypto;


import java.io.InputStream;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;


/**
 * Tool for working with encryption
 */
public interface EncryptionTool
{

    /********************************************
     *
     */
    public byte[] encrypt( final byte[] message, final PGPPublicKey publicKey, boolean armored );


    /********************************************
     *
     */
    public byte[] decrypt( final byte[] message, final InputStream secretKey, final String secretPwd );


    /********************************************
     *
     */
    public byte[] decryptAndVerify( byte[] encryptedMessage, final PGPSecretKey secretKey,
                                    final String secretPwd, final PGPPublicKey publicKey );
;
    /********************************************
     *
     */
    public byte[] signAndEncrypt( final byte[] message, final PGPSecretKey secretKey, final String secretPwd,
                                  final PGPPublicKey publicKey, final boolean armored );

    /********************************************
     *
     */
    public boolean verify( byte[] signedMessage, PGPPublicKey publicKey );


    /********************************************
     *
     */
    public byte[] sign( byte[] message, PGPSecretKey secretKey, String secretPwd, boolean armor );

}
