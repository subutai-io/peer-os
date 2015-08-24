package io.subutai.common.security.crypto.pgp;


import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPSignatureList;


/**
 * Holds decrypted content and associated signatures to verify
 */
public class ContentAndSignatures
{
    private final byte[] decryptedContent;
    private final PGPOnePassSignatureList onePassSignatureList;
    private final PGPSignatureList signatureList;


    public ContentAndSignatures( final byte[] decryptedContent, final PGPOnePassSignatureList onePassSignatureList,
                                 final PGPSignatureList signatureList )
    {

        this.decryptedContent = decryptedContent;
        this.onePassSignatureList = onePassSignatureList;
        this.signatureList = signatureList;
    }


    public byte[] getDecryptedContent()
    {
        return decryptedContent;
    }


    public PGPOnePassSignatureList getOnePassSignatureList()
    {
        return onePassSignatureList;
    }


    public PGPSignatureList getSignatureList()
    {
        return signatureList;
    }
}
