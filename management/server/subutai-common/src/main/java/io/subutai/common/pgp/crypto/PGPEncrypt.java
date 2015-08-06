package io.subutai.common.pgp.crypto;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PGPEncrypt
{
    private static final Logger log = LoggerFactory.getLogger( PGPEncrypt.class );


    public static byte[] encrypt( byte[] data, PGPPublicKey publicKey ) throws IOException, PGPException
    {
        log.debug( "Encrypting with: " + Hex.toHexString( publicKey.getFingerprint() ) );
        byte[] compressedData = compress( data );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ArmoredOutputStream aos = new ArmoredOutputStream( bos );

        OutputStream encOut = getEncryptedGenerator( publicKey ).open( aos, compressedData.length );

        encOut.write( compressedData );

        encOut.close();

        aos.close();

        return bos.toByteArray();
    }


    private static PGPEncryptedDataGenerator getEncryptedGenerator( PGPPublicKey publicKey )
    {
        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder( PGPEncryptedData.CAST5 ).setWithIntegrityPacket( true )
                                                                        .setSecureRandom( new SecureRandom() )
                                                                        .setProvider( "BC" ) );

        encGen.addMethod( new JcePublicKeyKeyEncryptionMethodGenerator( publicKey ).setProvider( "BC" ) );

        return encGen;
    }


    private static byte[] compress( byte[] data ) throws IOException
    {
        PGPCompressedDataGenerator compressGen = new PGPCompressedDataGenerator( CompressionAlgorithmTags.ZIP );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream compressOut = compressGen.open( bos );

        OutputStream os =
                new PGPLiteralDataGenerator().open( compressOut, PGPLiteralData.BINARY, "", data.length, new Date() );

        os.write( data );

        os.close();

        compressGen.close();

        return bos.toByteArray();
    }
}
