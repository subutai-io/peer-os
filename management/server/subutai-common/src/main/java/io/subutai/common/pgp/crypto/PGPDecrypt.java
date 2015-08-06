package io.subutai.common.pgp.crypto;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.util.Iterator;

import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPDataValidationException;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.util.io.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;


public class PGPDecrypt
{
    private static final Logger log = LoggerFactory.getLogger( PGPDecrypt.class );


    public static byte[] decrypt( byte[] encData, PGPPrivateKey privateKey )
            throws IOException, PGPException, NoSuchProviderException
    {
        log.debug( "Decrypting with: " + privateKey.getKeyID() );

        PGPPublicKeyEncryptedData pgpEncData = getPGPEncryptedData( encData );

        InputStream is = getInputStream( privateKey, pgpEncData );

        // IMPORTANT: pipe() should be before verify(). Otherwise we get "java.io.EOFException: Unexpected end of ZIP
        // input stream".
        byte[] data = pipe( is );

        if ( !pgpEncData.verify() )
        {
            throw new PGPDataValidationException( "Data integrity check failed" );
        }

        return data;
    }


    private static byte[] pipe( InputStream is ) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Streams.pipeAll( is, bos );

        bos.close();

        return bos.toByteArray();
    }


    private static InputStream getInputStream( PGPPrivateKey privateKey, PGPPublicKeyEncryptedData pgpEncData )
            throws PGPException, IOException
    {
        InputStream is = pgpEncData
                .getDataStream( new JcePublicKeyDataDecryptorFactoryBuilder().setProvider( "BC" ).build( privateKey ) );

        JcaPGPObjectFactory objectFactory = new JcaPGPObjectFactory( is );

        Object message = objectFactory.nextObject();

        PGPCompressedData compressedData = ( PGPCompressedData ) message;

        JcaPGPObjectFactory pgpObjectFactory = new JcaPGPObjectFactory( compressedData.getDataStream() );

        PGPLiteralData literalData = ( PGPLiteralData ) pgpObjectFactory.nextObject();

        return literalData.getInputStream();
    }


    private static PGPPublicKeyEncryptedData getPGPEncryptedData( byte[] encData ) throws IOException
    {
        InputStream in = PGPUtil.getDecoderStream( new ByteArrayInputStream( encData ) );

        JcaPGPObjectFactory objectFactory = new JcaPGPObjectFactory( in );

        PGPEncryptedDataList encryptedDataList = ( PGPEncryptedDataList ) objectFactory.nextObject();

        Iterator it = encryptedDataList.getEncryptedDataObjects();

        return ( PGPPublicKeyEncryptedData ) it.next();
    }
}
