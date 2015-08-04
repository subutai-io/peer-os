package io.subutai.common.pgp.crypto;


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

import org.apache.commons.io.IOUtils;


public class PGPSign
{
    public static String sign( String data, PGPPrivateKey privateKey ) throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ArmoredOutputStream aos = new ArmoredOutputStream( bos );

        PGPCompressedDataGenerator compressGen = new PGPCompressedDataGenerator( PGPCompressedData.ZLIB );

        BCPGOutputStream bcOut = new BCPGOutputStream( compressGen.open( aos ) );

        PGPSignatureGenerator signGen = getSignatureGenerator( privateKey, bcOut );

        produceSign( data, bcOut, signGen );

        compressGen.close();

        aos.close();

        return bos.toString( "UTF-8" );
    }


    private static void produceSign( String data, BCPGOutputStream bcOut, PGPSignatureGenerator signGen ) throws IOException, PGPException
    {
        PGPLiteralDataGenerator literalGen = new PGPLiteralDataGenerator();

        OutputStream os = literalGen.open( bcOut, PGPLiteralData.BINARY, "", data.length(), new Date() );

        InputStream is = IOUtils.toInputStream( data );

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
                new JcaPGPContentSignerBuilder( privateKey.getPublicKeyPacket().getAlgorithm(), PGPUtil.SHA1 ).setProvider( "BC" ) );

        signGen.init( PGPSignature.BINARY_DOCUMENT, privateKey );

        signGen.generateOnePassVersion( false ).encode( bcOut );

        return signGen;
    }

}
