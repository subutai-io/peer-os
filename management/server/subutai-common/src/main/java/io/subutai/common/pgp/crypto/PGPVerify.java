package io.subutai.common.pgp.crypto;


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

import org.apache.commons.io.IOUtils;


public class PGPVerify
{
    public static String verify( String signedData, PGPPublicKey publicKey ) throws Exception
    {
        JcaPGPObjectFactory objectFactory = getObjectFactory( signedData );

        PGPOnePassSignature onePassSignature = getOnePassSignature( publicKey, objectFactory );

        String data = readSign( objectFactory, onePassSignature );

        doVerify( objectFactory, onePassSignature );

        return data;
    }


    private static String readSign( JcaPGPObjectFactory objectFactory, PGPOnePassSignature onePassSignature ) throws IOException
    {
        InputStream is = getInputStream( objectFactory );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int ch;

        while ( ( ch = is.read() ) >= 0 )
        {
            onePassSignature.update( ( byte ) ch );
            bos.write( ch );
        }

        return bos.toString( "UTF-8" );
    }


    private static void doVerify( JcaPGPObjectFactory objectFactory, PGPOnePassSignature onePassSignature ) throws IOException, PGPException
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


    private static PGPOnePassSignature getOnePassSignature( PGPPublicKey publicKey, JcaPGPObjectFactory pgpFact ) throws IOException, PGPException
    {
        PGPOnePassSignatureList p1 = ( PGPOnePassSignatureList ) pgpFact.nextObject();

        PGPOnePassSignature onePassSignature = p1.get( 0 );

        onePassSignature.init( new JcaPGPContentVerifierBuilderProvider().setProvider( "BC" ), publicKey );

        return onePassSignature;
    }


    private static JcaPGPObjectFactory getObjectFactory( String signedData ) throws IOException, PGPException
    {
        InputStream in = PGPUtil.getDecoderStream( IOUtils.toInputStream( signedData ) );

        JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory( in );

        PGPCompressedData compressedData = ( PGPCompressedData ) pgpFact.nextObject();

        return new JcaPGPObjectFactory( compressedData.getDataStream() );
    }

}
