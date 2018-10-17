package io.subutai.bazaar.share.pgp.crypto;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPDataValidationException;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;


public class PGPVerify
{
    private PGPVerify()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static byte[] verify( byte signedData[], PGPPublicKey publicKey ) throws IOException, PGPException
    {
        JcaPGPObjectFactory objectFactory = getObjectFactory( signedData );

        PGPOnePassSignature onePassSignature = getOnePassSignature( publicKey, objectFactory );

        byte data[] = readSign( objectFactory, onePassSignature );

        doVerify( objectFactory, onePassSignature );

        return data;
    }


    private static byte[] readSign( JcaPGPObjectFactory objectFactory, PGPOnePassSignature onePassSignature )
            throws IOException
    {
        InputStream is = getInputStream( objectFactory );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int ch;

        while ( ( ch = is.read() ) >= 0 )
        {
            onePassSignature.update( ( byte ) ch );
            bos.write( ch );
        }

        return bos.toByteArray();
    }


    private static void doVerify( JcaPGPObjectFactory objectFactory, PGPOnePassSignature onePassSignature )
            throws IOException, PGPException
    {
        PGPSignatureList signatures = ( PGPSignatureList ) objectFactory.nextObject();

        if ( !onePassSignature.verify( signatures.get( 0 ) ) )
        {
            throw new PGPDataValidationException( "Signature verification failed" );
        }
    }


    private static InputStream getInputStream( JcaPGPObjectFactory objectFactory ) throws IOException
    {
        PGPLiteralData literalData = ( PGPLiteralData ) objectFactory.nextObject();

        return literalData.getInputStream();
    }


    private static PGPOnePassSignature getOnePassSignature( PGPPublicKey publicKey, JcaPGPObjectFactory pgpFact )
            throws IOException, PGPException
    {
        PGPOnePassSignatureList p1 = ( PGPOnePassSignatureList ) pgpFact.nextObject();

        PGPOnePassSignature onePassSignature = p1.get( 0 );

        onePassSignature.init( new JcaPGPContentVerifierBuilderProvider().setProvider( "BC" ), publicKey );

        return onePassSignature;
    }


    private static JcaPGPObjectFactory getObjectFactory( byte signedData[] ) throws IOException, PGPException
    {
        InputStream in = PGPUtil.getDecoderStream( new ByteArrayInputStream( signedData ) );

        JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory( in );

        PGPCompressedData compressedData = ( PGPCompressedData ) pgpFact.nextObject();

        return new JcaPGPObjectFactory( compressedData.getDataStream() );
    }
}
