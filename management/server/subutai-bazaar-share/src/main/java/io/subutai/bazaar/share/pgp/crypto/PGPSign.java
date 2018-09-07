package io.subutai.bazaar.share.pgp.crypto;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;


public class PGPSign
{
    private PGPSign()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static byte[] sign( byte data[], PGPPrivateKey privateKey ) throws IOException, PGPException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ArmoredOutputStream aos = new ArmoredOutputStream( bos );

        PGPCompressedDataGenerator compressGen = new PGPCompressedDataGenerator( PGPCompressedData.ZLIB );

        BCPGOutputStream bcOut = new BCPGOutputStream( compressGen.open( aos ) );

        PGPSignatureGenerator signGen = getSignatureGenerator( privateKey, bcOut );

        produceSign( data, bcOut, signGen );

        compressGen.close();

        aos.close();

        return bos.toByteArray();
    }


    private static void produceSign( byte[] data, BCPGOutputStream bcOut, PGPSignatureGenerator signGen )
            throws IOException, PGPException
    {
        PGPLiteralDataGenerator literalGen = new PGPLiteralDataGenerator();

        OutputStream os = literalGen.open( bcOut, PGPLiteralData.BINARY, "", data.length, new Date() );

        InputStream is = new ByteArrayInputStream( data );

        int ch;

        while ( ( ch = is.read() ) >= 0 )
        {
            signGen.update( ( byte ) ch );
            os.write( ch );
        }

        literalGen.close();

        signGen.generate().encode( bcOut );
    }


    private static PGPSignatureGenerator getSignatureGenerator( PGPPrivateKey privateKey, BCPGOutputStream bcOut )
            throws PGPException, IOException
    {
        PGPSignatureGenerator signGen = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder( privateKey.getPublicKeyPacket().getAlgorithm(), PGPUtil.SHA1 )
                        .setProvider( "BC" ) );

        signGen.init( PGPSignature.BINARY_DOCUMENT, privateKey );

        signGen.generateOnePassVersion( false ).encode( bcOut );

        return signGen;
    }
}
