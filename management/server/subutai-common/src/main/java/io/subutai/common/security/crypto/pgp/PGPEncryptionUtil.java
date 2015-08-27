package io.subutai.common.security.crypto.pgp;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.util.io.Streams;

import com.google.common.base.Objects;


/**
 * Provides methods to encrypt, decrypt, sign and verify signature using PGP keypairs
 */
public class PGPEncryptionUtil
{
    public static final BouncyCastleProvider provider = new BouncyCastleProvider();
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();


    static
    {
        Security.addProvider( provider );
    }


    public static byte[] encrypt( final byte[] message, final PGPPublicKey publicKey, boolean armored )
            throws PGPException
    {
        try
        {
            final ByteArrayInputStream in = new ByteArrayInputStream( message );
            final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            final PGPLiteralDataGenerator literal = new PGPLiteralDataGenerator();
            final PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator( CompressionAlgorithmTags.ZIP );
            final OutputStream pOut =
                    literal.open( comData.open( bOut ), PGPLiteralData.BINARY, "filename", in.available(), new Date() );
            Streams.pipeAll( in, pOut );
            comData.close();
            final byte[] bytes = bOut.toByteArray();
            final PGPEncryptedDataGenerator generator = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder( SymmetricKeyAlgorithmTags.AES_256 ).setWithIntegrityPacket( true )
                                                                                       .setSecureRandom(
                                                                                               new SecureRandom() )

                                                                                       .setProvider( provider ) );
            generator.addMethod( new JcePublicKeyKeyEncryptionMethodGenerator( publicKey ).setProvider( provider ) );
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStream theOut = armored ? new ArmoredOutputStream( out ) : out;
            OutputStream cOut = generator.open( theOut, bytes.length );
            cOut.write( bytes );
            cOut.close();
            theOut.close();
            return out.toByteArray();
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in encrypt", e );
        }
    }


    public static byte[] decrypt( final byte[] encryptedMessage, final InputStream secretKeyRing,
                                  final String secretPwd ) throws PGPException
    {
        try
        {
            final PGPLiteralData msg = asLiteral( encryptedMessage, secretKeyRing, secretPwd );
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Streams.pipeAll( msg.getInputStream(), out );
            return out.toByteArray();
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in decrypt", e );
        }
    }


    public static boolean verifySignature( ContentAndSignatures contentAndSignatures, PGPPublicKey publicKey )
            throws PGPException
    {
        try
        {
            for ( int i = 0; i < contentAndSignatures.getOnePassSignatureList().size(); i++ )
            {
                PGPOnePassSignature ops = contentAndSignatures.getOnePassSignatureList().get( 0 );

                if ( publicKey != null )
                {
                    ops.init( new JcaPGPContentVerifierBuilderProvider().setProvider( "BC" ), publicKey );
                    ops.update( contentAndSignatures.getDecryptedContent() );
                    PGPSignature signature = contentAndSignatures.getSignatureList().get( i );
                    if ( ops.verify( signature ) )
                    {
                        Iterator<?> userIds = publicKey.getUserIDs();
                        while ( userIds.hasNext() )
                        {
                            String userId = ( String ) userIds.next();
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in verifySignature", e );
        }
    }


    public static ContentAndSignatures decryptAndReturnSignatures( final byte[] encryptedMessage,
                                                                   final PGPSecretKey secretKey,
                                                                   final String secretPwd ) throws PGPException
    {
        try
        {
            Iterator<PGPPublicKeyEncryptedData> it = getEncryptedObjects( encryptedMessage );
            PGPPrivateKey sKey;
            PGPPublicKeyEncryptedData pbe;

            pbe = it.next();
            sKey = secretKey.extractPrivateKey(
                    new JcePBESecretKeyDecryptorBuilder().setProvider( provider ).build( secretPwd.toCharArray() ) );
            InputStream clear = pbe.getDataStream(
                    new JcePublicKeyDataDecryptorFactoryBuilder().setProvider( provider ).build( sKey ) );

            PGPObjectFactory plainFact = new PGPObjectFactory( clear, new JcaKeyFingerprintCalculator() );

            Object message;

            PGPOnePassSignatureList onePassSignatureList = null;
            PGPSignatureList signatureList = null;
            PGPCompressedData compressedData;

            message = plainFact.nextObject();
            ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();

            while ( message != null )
            {
                if ( message instanceof PGPCompressedData )
                {
                    compressedData = ( PGPCompressedData ) message;
                    plainFact =
                            new PGPObjectFactory( compressedData.getDataStream(), new JcaKeyFingerprintCalculator() );
                    message = plainFact.nextObject();
                }

                if ( message instanceof PGPLiteralData )
                {
                    // have to read it and keep it somewhere.
                    Streams.pipeAll( ( ( PGPLiteralData ) message ).getInputStream(), actualOutput );
                }
                else if ( message instanceof PGPOnePassSignatureList )
                {
                    onePassSignatureList = ( PGPOnePassSignatureList ) message;
                }
                else if ( message instanceof PGPSignatureList )
                {
                    signatureList = ( PGPSignatureList ) message;
                }
                else
                {
                    throw new PGPException( "message unknown message type." );
                }
                message = plainFact.nextObject();
            }
            actualOutput.close();

            //verify signature
            if ( onePassSignatureList == null || signatureList == null )
            {
                throw new PGPException( "Poor PGP. Signatures not found." );
            }

            if ( pbe.isIntegrityProtected() && !pbe.verify() )
            {
                throw new PGPException( "Data is integrity protected but integrity is lost." );
            }


            return new ContentAndSignatures( actualOutput.toByteArray(), onePassSignatureList, signatureList );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in decryptAndVerify", e );
        }
    }


    public static byte[] decryptAndVerify( byte[] encryptedMessage, final PGPSecretKey secretKey,
                                           final String secretPwd, final PGPPublicKey publicKey ) throws PGPException
    {

        try
        {
            Iterator<PGPPublicKeyEncryptedData> it = getEncryptedObjects( encryptedMessage );
            PGPPrivateKey sKey;
            PGPPublicKeyEncryptedData pbe;

            pbe = it.next();
            sKey = secretKey.extractPrivateKey(
                    new JcePBESecretKeyDecryptorBuilder().setProvider( provider ).build( secretPwd.toCharArray() ) );
            InputStream clear = pbe.getDataStream(
                    new JcePublicKeyDataDecryptorFactoryBuilder().setProvider( provider ).build( sKey ) );

            PGPObjectFactory plainFact = new PGPObjectFactory( clear, new JcaKeyFingerprintCalculator() );

            Object message;

            PGPOnePassSignatureList onePassSignatureList = null;
            PGPSignatureList signatureList = null;
            PGPCompressedData compressedData;

            message = plainFact.nextObject();
            ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();

            while ( message != null )
            {
                if ( message instanceof PGPCompressedData )
                {
                    compressedData = ( PGPCompressedData ) message;
                    plainFact =
                            new PGPObjectFactory( compressedData.getDataStream(), new JcaKeyFingerprintCalculator() );
                    message = plainFact.nextObject();
                }

                if ( message instanceof PGPLiteralData )
                {
                    // have to read it and keep it somewhere.
                    Streams.pipeAll( ( ( PGPLiteralData ) message ).getInputStream(), actualOutput );
                }
                else if ( message instanceof PGPOnePassSignatureList )
                {
                    onePassSignatureList = ( PGPOnePassSignatureList ) message;
                }
                else if ( message instanceof PGPSignatureList )
                {
                    signatureList = ( PGPSignatureList ) message;
                }
                else
                {
                    throw new PGPException( "message unknown message type." );
                }
                message = plainFact.nextObject();
            }
            actualOutput.close();
            byte[] output = actualOutput.toByteArray();


            //verify signature
            if ( onePassSignatureList == null || signatureList == null )
            {
                throw new PGPException( "Poor PGP. Signatures not found." );
            }
            else
            {

                for ( int i = 0; i < onePassSignatureList.size(); i++ )
                {
                    PGPOnePassSignature ops = onePassSignatureList.get( 0 );

                    if ( publicKey != null )
                    {
                        ops.init( new JcaPGPContentVerifierBuilderProvider().setProvider( "BC" ), publicKey );
                        ops.update( output );
                        PGPSignature signature = signatureList.get( i );
                        if ( ops.verify( signature ) )
                        {
                            Iterator<?> userIds = publicKey.getUserIDs();
                            while ( userIds.hasNext() )
                            {
                                String userId = ( String ) userIds.next();
                            }
                        }
                        else
                        {
                            throw new SignatureException( "Signature verification failed" );
                        }
                    }
                }
            }

            if ( pbe.isIntegrityProtected() && !pbe.verify() )
            {
                throw new PGPException( "Data is integrity protected but integrity is lost." );
            }
            else if ( publicKey == null )
            {
                throw new SignatureException( "Signature not found" );
            }

            return actualOutput.toByteArray();
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in decryptAndVerify", e );
        }
    }


    public static byte[] decryptAndVerify( byte[] encryptedMessage, final InputStream secretKeyRing,
                                           final String secretPwd, final PGPPublicKey publicKey ) throws PGPException
    {

        try
        {
            Iterator<PGPPublicKeyEncryptedData> it = getEncryptedObjects( encryptedMessage );
            PGPPrivateKey sKey = null;
            PGPPublicKeyEncryptedData pbe = null;
            final PGPSecretKeyRingCollection keys =
                    new PGPSecretKeyRingCollection( secretKeyRing, new JcaKeyFingerprintCalculator() );
            while ( sKey == null && it.hasNext() )
            {
                pbe = it.next();
                sKey = getPrivateKey( keys, pbe.getKeyID(), secretPwd );
            }
            if ( sKey == null )
            {
                throw new IllegalArgumentException( "Unable to find secret key to decrypt the message" );
            }

            InputStream clear = pbe.getDataStream(
                    new JcePublicKeyDataDecryptorFactoryBuilder().setProvider( provider ).build( sKey ) );

            PGPObjectFactory plainFact = new PGPObjectFactory( clear, new JcaKeyFingerprintCalculator() );

            Object message;

            PGPOnePassSignatureList onePassSignatureList = null;
            PGPSignatureList signatureList = null;
            PGPCompressedData compressedData;

            message = plainFact.nextObject();
            ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();

            while ( message != null )
            {
                if ( message instanceof PGPCompressedData )
                {
                    compressedData = ( PGPCompressedData ) message;
                    plainFact =
                            new PGPObjectFactory( compressedData.getDataStream(), new JcaKeyFingerprintCalculator() );
                    message = plainFact.nextObject();
                }

                if ( message instanceof PGPLiteralData )
                {
                    // have to read it and keep it somewhere.
                    Streams.pipeAll( ( ( PGPLiteralData ) message ).getInputStream(), actualOutput );
                }
                else if ( message instanceof PGPOnePassSignatureList )
                {
                    onePassSignatureList = ( PGPOnePassSignatureList ) message;
                }
                else if ( message instanceof PGPSignatureList )
                {
                    signatureList = ( PGPSignatureList ) message;
                }
                else
                {
                    throw new PGPException( "message unknown message type." );
                }
                message = plainFact.nextObject();
            }
            actualOutput.close();
            byte[] output = actualOutput.toByteArray();


            if ( onePassSignatureList == null || signatureList == null )
            {
                throw new PGPException( "Poor PGP. Signatures not found." );
            }
            else
            {

                for ( int i = 0; i < onePassSignatureList.size(); i++ )
                {
                    PGPOnePassSignature ops = onePassSignatureList.get( 0 );

                    if ( publicKey != null )
                    {
                        ops.init( new JcaPGPContentVerifierBuilderProvider().setProvider( "BC" ), publicKey );
                        ops.update( output );
                        PGPSignature signature = signatureList.get( i );
                        if ( ops.verify( signature ) )
                        {
                            Iterator<?> userIds = publicKey.getUserIDs();
                            while ( userIds.hasNext() )
                            {
                                String userId = ( String ) userIds.next();
                            }
                        }
                        else
                        {
                            throw new SignatureException( "Signature verification failed" );
                        }
                    }
                }
            }

            if ( pbe.isIntegrityProtected() && !pbe.verify() )
            {
                throw new PGPException( "Data is integrity protected but integrity is lost." );
            }
            else if ( publicKey == null )
            {
                throw new SignatureException( "Signature not found" );
            }

            return actualOutput.toByteArray();
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in decryptAndVerify", e );
        }
    }


    public static byte[] signAndEncrypt( final byte[] message, final PGPSecretKey secretKey, final String secretPwd,
                                         final PGPPublicKey publicKey, final boolean armored ) throws PGPException
    {
        try
        {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder( SymmetricKeyAlgorithmTags.AES_256 ).setWithIntegrityPacket( true )
                                                                                       .setSecureRandom(
                                                                                               new SecureRandom() )
                                                                                       .setProvider( provider ) );

            encryptedDataGenerator.addMethod(
                    new JcePublicKeyKeyEncryptionMethodGenerator( publicKey ).setSecureRandom( new SecureRandom() )
                                                                             .setProvider( provider ) );

            final OutputStream theOut = armored ? new ArmoredOutputStream( out ) : out;
            final OutputStream encryptedOut = encryptedDataGenerator.open( theOut, new byte[4096] );

            final PGPCompressedDataGenerator compressedDataGenerator =
                    new PGPCompressedDataGenerator( CompressionAlgorithmTags.ZIP );
            final OutputStream compressedOut = compressedDataGenerator.open( encryptedOut, new byte[4096] );
            final PGPPrivateKey privateKey = secretKey.extractPrivateKey(
                    new JcePBESecretKeyDecryptorBuilder().setProvider( provider ).build( secretPwd.toCharArray() ) );
            final PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                    new JcaPGPContentSignerBuilder( secretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1 )
                            .setProvider( provider ) );
            signatureGenerator.init( PGPSignature.BINARY_DOCUMENT, privateKey );
            final Iterator<?> it = secretKey.getPublicKey().getUserIDs();
            if ( it.hasNext() )
            {
                final PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
                spGen.setSignerUserID( false, ( String ) it.next() );
                signatureGenerator.setHashedSubpackets( spGen.generate() );
            }
            signatureGenerator.generateOnePassVersion( false ).encode( compressedOut );
            final PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
            final OutputStream literalOut = literalDataGenerator
                    .open( compressedOut, PGPLiteralData.BINARY, "filename", new Date(), new byte[4096] );
            final InputStream in = new ByteArrayInputStream( message );
            final byte[] buf = new byte[4096];
            for ( int len; ( len = in.read( buf ) ) > 0; )
            {
                literalOut.write( buf, 0, len );
                signatureGenerator.update( buf, 0, len );
            }
            in.close();
            literalDataGenerator.close();
            signatureGenerator.generate().encode( compressedOut );
            compressedDataGenerator.close();
            encryptedDataGenerator.close();
            theOut.close();
            return out.toByteArray();
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in signAndEncrypt", e );
        }
    }


    public static KeyRef generateKeyPair( String userId, String secretPwd, OutputStream newPublicKeyRing,
                                          OutputStream newSecretKeyRing ) throws PGPException
    {
        try
        {
            KeyRef keyRef = new KeyRef();
            PGPKeyRingGenerator krgen = generateKeyRingGenerator( userId, secretPwd, keyRef );

            // Generate public key ring, dump to file.
            PGPPublicKeyRing pkr = krgen.generatePublicKeyRing();
            BufferedOutputStream pubout = new BufferedOutputStream( newPublicKeyRing );
            //        System.out.println( Long.toHexString( pkr.getPublicKey().getKeyID() ) );
            pkr.encode( pubout );
            pubout.close();

            // Generate private key, dump to file.
            PGPSecretKeyRing skr = krgen.generateSecretKeyRing();
            BufferedOutputStream secout = new BufferedOutputStream( newSecretKeyRing );
            skr.encode( secout );
            secout.close();

            return keyRef;
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in generateKeyPair", e );
        }
    }


    public static class KeyRef
    {
        private String signingKeyId;
        private String signingKeyFingerprint;
        private String encryptingKeyId;
        private String encryptingKeyFingerprint;


        public void setSigningKeyId( final String signingKeyId )
        {
            this.signingKeyId = signingKeyId;
        }


        public void setSigningKeyFingerprint( final String signingKeyFingerprint )
        {
            this.signingKeyFingerprint = signingKeyFingerprint;
        }


        public String getSigningKeyId()
        {
            return signingKeyId;
        }


        public String getSigningKeyFingerprint()
        {
            return signingKeyFingerprint;
        }


        public String getEncryptingKeyId()
        {
            return encryptingKeyId;
        }


        public void setEncryptingKeyId( final String encryptingKeyId )
        {
            this.encryptingKeyId = encryptingKeyId;
        }


        public String getEncryptingKeyFingerprint()
        {
            return encryptingKeyFingerprint;
        }


        public void setEncryptingKeyFingerprint( final String encryptingKeyFingerprint )
        {
            this.encryptingKeyFingerprint = encryptingKeyFingerprint;
        }


        @Override
        public String toString()
        {
            return Objects.toStringHelper( this ).add( "signingKeyId", signingKeyId )
                          .add( "signingKeyFingerprint", signingKeyFingerprint )
                          .add( "encryptingKeyId", encryptingKeyId )
                          .add( "encryptingKeyFingerprint", encryptingKeyFingerprint ).toString();
        }
    }


    public static boolean verify( byte[] signedMessage, PGPPublicKey publicKey ) throws PGPException
    {
        try
        {
            InputStream in = PGPUtil.getDecoderStream( new ByteArrayInputStream( signedMessage ) );

            JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory( in );

            PGPCompressedData c1 = ( PGPCompressedData ) pgpFact.nextObject();

            pgpFact = new JcaPGPObjectFactory( c1.getDataStream() );

            PGPOnePassSignatureList p1 = ( PGPOnePassSignatureList ) pgpFact.nextObject();

            PGPOnePassSignature ops = p1.get( 0 );

            PGPLiteralData p2 = ( PGPLiteralData ) pgpFact.nextObject();

            InputStream dIn = p2.getInputStream();
            int ch;


            ops.init( new JcaPGPContentVerifierBuilderProvider().setProvider( "BC" ), publicKey );

            while ( ( ch = dIn.read() ) >= 0 )
            {
                ops.update( ( byte ) ch );
            }

            PGPSignatureList p3 = ( PGPSignatureList ) pgpFact.nextObject();

            if ( ops.verify( p3.get( 0 ) ) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in verify", e );
        }
    }


    public static byte[] sign( byte[] message, PGPSecretKey secretKey, String secretPwd, boolean armor )
            throws PGPException
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStream theOut = armor ? new ArmoredOutputStream( out ) : out;

            PGPPrivateKey pgpPrivKey = secretKey.extractPrivateKey(
                    new JcePBESecretKeyDecryptorBuilder().setProvider( "BC" ).build( secretPwd.toCharArray() ) );
            PGPSignatureGenerator sGen = new PGPSignatureGenerator(
                    new JcaPGPContentSignerBuilder( secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1 )
                            .setProvider( "BC" ) );

            sGen.init( PGPSignature.BINARY_DOCUMENT, pgpPrivKey );

            Iterator it = secretKey.getPublicKey().getUserIDs();
            if ( it.hasNext() )
            {
                PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

                spGen.setSignerUserID( false, ( String ) it.next() );
                sGen.setHashedSubpackets( spGen.generate() );
            }

            PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator( PGPCompressedData.ZLIB );

            BCPGOutputStream bOut = new BCPGOutputStream( cGen.open( theOut ) );

            sGen.generateOnePassVersion( false ).encode( bOut );

            //        File file = new File( fileName );
            PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
            OutputStream lOut =
                    lGen.open( bOut, PGPLiteralData.BINARY, "filename", new Date(), new byte[4096] );         //
            InputStream fIn = new ByteArrayInputStream( message );
            int ch;

            while ( ( ch = fIn.read() ) >= 0 )
            {
                lOut.write( ch );
                sGen.update( ( byte ) ch );
            }

            lGen.close();

            sGen.generate().encode( bOut );

            cGen.close();

            theOut.close();

            return out.toByteArray();
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in sign", e );
        }
    }


    public static String BytesToHex( byte[] bytes )
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
       {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String( hexChars );
    }


    public static PGPPublicKey findPublicKeyById( InputStream publicKeyRing, String keyId ) throws PGPException
    {
        try
        {
            return findPublicKey( publicKeyRing, keyId, false );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in findPublicKeyById", e );
        }
    }


    public static PGPPublicKey findPublicKeyByFingerprint( InputStream publicKeyRing, String fingerprint )
            throws PGPException
    {
        try
        {
            return findPublicKey( publicKeyRing, fingerprint, true );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in findPublicKeyByFingerprint", e );
        }
    }


    public static PGPSecretKey findSecretKeyById( InputStream secretKeyRing, String keyId ) throws PGPException
    {
        try
        {
            return findSecretKey( secretKeyRing, keyId, false );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in findSecretKeyById", e );
        }
    }


    public static PGPSecretKey findSecretKeyByFingerprint( InputStream secretKeyRing, String fingerprint )
            throws PGPException
    {
        try
        {
            return findSecretKey( secretKeyRing, fingerprint, true );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in findSecretKeyByFingerprint", e );
        }
    }


    public static X509Certificate getX509CertificateFromPgpKeyPair( PGPPublicKey pgpPublicKey,
                                                                    PGPSecretKey pgpSecretKey, String secretPwd,
                                                                    String issuer, String subject, Date dateOfIssue,
                                                                    Date dateOfExpiry, BigInteger serial )
            throws PGPException, CertificateException, IOException
    {
        JcaPGPKeyConverter c = new JcaPGPKeyConverter();
        PublicKey publicKey = c.getPublicKey( pgpPublicKey );
        PrivateKey privateKey = c.getPrivateKey( pgpSecretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider( provider ).build( secretPwd.toCharArray() ) ) );

        X509v3CertificateBuilder certBuilder =
                new X509v3CertificateBuilder( new X500Name( issuer ), serial, dateOfIssue, dateOfExpiry,
                        new X500Name( subject ), SubjectPublicKeyInfo.getInstance( publicKey.getEncoded() ) );
        byte[] certBytes = certBuilder.build( new JCESigner( privateKey, "SHA256withRSA" ) ).getEncoded();
        CertificateFactory certificateFactory = CertificateFactory.getInstance( "X.509" );

        return ( X509Certificate ) certificateFactory.generateCertificate( new ByteArrayInputStream( certBytes ) );
    }


    //*****************************************


    private static class JCESigner implements ContentSigner
    {

        private static final AlgorithmIdentifier PKCS1_SHA256_WITH_RSA_OID =
                new AlgorithmIdentifier( new ASN1ObjectIdentifier( "1.2.840.113549.1.1.11" ) );

        private Signature signature;
        private ByteArrayOutputStream outputStream;


        public JCESigner( PrivateKey privateKey, String signatureAlgorithm )
        {
            if ( !"SHA256withRSA".equals( signatureAlgorithm ) )
            {
                throw new IllegalArgumentException(
                        "Signature algorithm \"" + signatureAlgorithm + "\" not yet supported" );
            }
            try
            {
                this.outputStream = new ByteArrayOutputStream();
                this.signature = Signature.getInstance( signatureAlgorithm );
                this.signature.initSign( privateKey );
            }
            catch ( GeneralSecurityException gse )
            {
                throw new IllegalArgumentException( gse.getMessage() );
            }
        }


        @Override
        public AlgorithmIdentifier getAlgorithmIdentifier()
        {
            if ( signature.getAlgorithm().equals( "SHA256withRSA" ) )
            {
                return PKCS1_SHA256_WITH_RSA_OID;
            }
            else
            {
                return null;
            }
        }


        @Override
        public OutputStream getOutputStream()
        {
            return outputStream;
        }


        @Override
        public byte[] getSignature()
        {
            try
            {
                signature.update( outputStream.toByteArray() );
                return signature.sign();
            }
            catch ( GeneralSecurityException gse )
            {
                gse.printStackTrace();
                return null;
            }
        }
    }


    @SuppressWarnings( "unchecked" )
    private static Iterator<PGPPublicKeyEncryptedData> getEncryptedObjects( final byte[] message ) throws IOException
    {
        final PGPObjectFactory factory =
                new PGPObjectFactory( PGPUtil.getDecoderStream( new ByteArrayInputStream( message ) ),
                        new JcaKeyFingerprintCalculator() );
        final Object first = factory.nextObject();
        final Object list = ( first instanceof PGPEncryptedDataList ) ? first : factory.nextObject();
        return ( ( PGPEncryptedDataList ) list ).getEncryptedDataObjects();
    }


    private static PGPLiteralData asLiteral( final byte[] message, final InputStream secretKeyRingPath,
                                             final String secretPwd ) throws IOException, PGPException
    {
        PGPPrivateKey key = null;
        PGPPublicKeyEncryptedData encrypted = null;
        final PGPSecretKeyRingCollection keys =
                new PGPSecretKeyRingCollection( ( secretKeyRingPath ), new JcaKeyFingerprintCalculator() );
        for ( final Iterator<PGPPublicKeyEncryptedData> i = getEncryptedObjects( message );
              ( key == null ) && i.hasNext(); )
        {
            encrypted = i.next();
            key = getPrivateKey( keys, encrypted.getKeyID(), secretPwd );
        }
        if ( key == null )
        {
            throw new IllegalArgumentException( "secret key for message not found." );
        }
        final InputStream stream = encrypted
                .getDataStream( new JcePublicKeyDataDecryptorFactoryBuilder().setProvider( provider ).build( key ) );
        return asLiteral( stream );
    }


    /**
     * ***********************************************
     */
    private static PGPLiteralData asLiteral( final InputStream clear ) throws IOException, PGPException
    {
        final PGPObjectFactory plainFact = new PGPObjectFactory( clear, new JcaKeyFingerprintCalculator() );
        final Object message = plainFact.nextObject();
        if ( message instanceof PGPCompressedData )
        {
            final PGPCompressedData cData = ( PGPCompressedData ) message;
            final PGPObjectFactory pgpFact =
                    new PGPObjectFactory( cData.getDataStream(), new JcaKeyFingerprintCalculator() );
            // Find the first PGPLiteralData object
            Object object = null;
            for ( int safety = 0; ( safety++ < 1000 ) && !( object instanceof PGPLiteralData );
                  object = pgpFact.nextObject() )
            {
                ;
            }
            return ( PGPLiteralData ) object;
        }
        else if ( message instanceof PGPLiteralData )
        {
            return ( PGPLiteralData ) message;
        }
        else if ( message instanceof PGPOnePassSignatureList )
        {
            throw new PGPException( "encrypted message contains a signed message - not literal data." );
        }
        else
        {
            throw new PGPException(
                    "message is not a simple encrypted file - type unknown: " + message.getClass().getName() );
        }
    }


    /**
     * ***********************************************
     */
    private static PGPPrivateKey getPrivateKey( final PGPSecretKeyRingCollection keys, final long id,
                                                final String secretPwd )
    {
        try
        {
            final PGPSecretKey key = keys.getSecretKey( id );
            if ( key != null )
            {
                return key.extractPrivateKey( new JcePBESecretKeyDecryptorBuilder().setProvider( provider )
                                                                                   .build( secretPwd.toCharArray() ) );
            }
        }
        catch ( final Exception e )
        {
            // Don't print the passphrase but do print null if thats what it was
            final String passphraseMessage = ( secretPwd == null ) ? "null" : "supplied";
            System.err.println( "Unable to extract key " + id + " using " + passphraseMessage + " passphrase" );
        }
        return null;
    }


    /**
     * ***********************************************
     */
    public static PGPPrivateKey getPrivateKey( final PGPSecretKey secretKey, final String secretPwd )
    {
        try
        {
            if ( secretKey != null )
            {
                return secretKey.extractPrivateKey( new JcePBESecretKeyDecryptorBuilder().setProvider( provider )
                                                                                         .build( secretPwd
                                                                                                 .toCharArray() ) );
            }
        }
        catch ( final Exception e )
        {
            // Don't print the passphrase but do print null if thats what it was
            final String passphraseMessage = ( secretPwd == null ) ? "null" : "supplied";
            System.err.println(
                    "Unable to extract key " + secretKey.getKeyID() + " using " + passphraseMessage + " passphrase" );
        }
        return null;
    }


    /**
     * ***********************************************
     */
    private static PGPPublicKey findPublicKey( InputStream publicKeyRing, String id, boolean fingerprint )
            throws IOException, PGPException
    {

        PGPPublicKeyRingCollection keyrings =
                new PGPPublicKeyRingCollection( publicKeyRing, new JcaKeyFingerprintCalculator() );

        Iterator<PGPPublicKeyRing> it = keyrings.getKeyRings();
        while ( it.hasNext() )
        {
            PGPPublicKeyRing keyRing = it.next();

            if ( fingerprint )
            {

                Iterator<PGPPublicKey> pkIt = keyRing.getPublicKeys();

                while ( pkIt.hasNext() )
                {
                    PGPPublicKey publicKey = pkIt.next();

                    if ( BytesToHex( publicKey.getFingerprint() ).equalsIgnoreCase( id ) )
                    {
                        return publicKey;
                    }
                }
            }
            else
            {
                PGPPublicKey publicKey = keyRing.getPublicKey( new BigInteger( id, 16 ).longValue() );

                if ( publicKey != null )
                {
                    return publicKey;
                }
            }
        }

        throw new PGPException( "Key not found" );
    }


    private static PGPSecretKey findSecretKey( InputStream secretKeyRing, String id, boolean fingerprint )
            throws IOException, PGPException
    {

        PGPSecretKeyRingCollection keyrings =
                new PGPSecretKeyRingCollection( secretKeyRing, new JcaKeyFingerprintCalculator() );

        Iterator<PGPSecretKeyRing> it = keyrings.getKeyRings();
        while ( it.hasNext() )
        {
            PGPSecretKeyRing keyRing = it.next();

            if ( fingerprint )
            {

                Iterator<PGPSecretKey> pkIt = keyRing.getSecretKeys();

                while ( pkIt.hasNext() )
                {
                    PGPSecretKey secretKey = pkIt.next();

                    if ( BytesToHex( secretKey.getPublicKey().getFingerprint() ).equalsIgnoreCase( id ) )
                    {
                        return secretKey;
                    }
                }
            }
            else
            {
                PGPSecretKey secretKey = keyRing.getSecretKey( new BigInteger( id, 16 ).longValue() );

                if ( secretKey != null )
                {
                    return secretKey;
                }
            }
        }

        throw new PGPException( "Key not found" );
    }


    private static PGPKeyRingGenerator generateKeyRingGenerator( String userId, String secretPwd, KeyRef keyRef )
            throws Exception
    {
        return generateKeyRingGenerator( userId, secretPwd.toCharArray(), 0xc0, keyRef );
    }

    // Note: s2kcount is a number between 0 and 0xff that controls the
    // number of times to iterate the password hash before use. More
    // iterations are useful against offline attacks, as it takes more
    // time to check each password. The actual number of iterations is
    // rather complex, and also depends on the hash function in use.
    // Refer to Section 3.7.1.3 in rfc4880.txt. Bigger numbers give
    // you more iterations.  As a rough rule of thumb, when using
    // SHA256 as the hashing function, 0x10 gives you about 64
    // iterations, 0x20 about 128, 0x30 about 256 and so on till 0xf0,
    // or about 1 million iterations. The maximum you can go to is
    // 0xff, or about 2 million iterations.  I'll use 0xc0 as a
    // default -- about 130,000 iterations.


    private static PGPKeyRingGenerator generateKeyRingGenerator( String id, char[] pass, int s2kcount, KeyRef keyRef )
            throws Exception
    {
        // This object generates individual key-pairs.
        RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

        // Boilerplate RSA parameters, no need to change anything
        // except for the RSA key-size (2048). You can use whatever
        // key-size makes sense for you -- 4096, etc.
        kpg.init( new RSAKeyGenerationParameters( BigInteger.valueOf( 0x10001 ), new SecureRandom(), 2048, 12 ) );

        // First create the master (signing) key with the generator.
        PGPKeyPair rsakp_sign = new BcPGPKeyPair( PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date() );
        // Then an encryption subkey.
        PGPKeyPair rsakp_enc = new BcPGPKeyPair( PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date() );

        keyRef.setSigningKeyId( Long.toHexString( rsakp_sign.getKeyID() ) );
        keyRef.setSigningKeyFingerprint( BytesToHex( rsakp_sign.getPublicKey().getFingerprint() ) );
        keyRef.setEncryptingKeyId( Long.toHexString( rsakp_enc.getKeyID() ) );
        keyRef.setEncryptingKeyFingerprint( BytesToHex( rsakp_enc.getPublicKey().getFingerprint() ) );

        // Add a self-signature on the id
        PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();

        // Add signed metadata on the signature.
        // 1) Declare its purpose
        signhashgen.setKeyFlags( false, KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER );
        // 2) Set preferences for secondary crypto algorithms to use
        //    when sending messages to this key.
        signhashgen.setPreferredSymmetricAlgorithms( false, new int[] {
                SymmetricKeyAlgorithmTags.AES_256, SymmetricKeyAlgorithmTags.AES_192, SymmetricKeyAlgorithmTags.AES_128
        } );
        signhashgen.setPreferredHashAlgorithms( false, new int[] {
                HashAlgorithmTags.SHA256, HashAlgorithmTags.SHA1, HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA512,
                HashAlgorithmTags.SHA224,
        } );
        // 3) Request senders add additional checksums to the
        //    message (useful when verifying unsigned messages.)
        signhashgen.setFeature( false, Features.FEATURE_MODIFICATION_DETECTION );

        // Create a signature on the encryption subkey.
        PGPSignatureSubpacketGenerator enchashgen = new PGPSignatureSubpacketGenerator();
        // Add metadata to declare its purpose
        enchashgen.setKeyFlags( false, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE );

        // Objects used to encrypt the secret key.
        PGPDigestCalculator sha1Calc = new BcPGPDigestCalculatorProvider().get( HashAlgorithmTags.SHA1 );
        PGPDigestCalculator sha256Calc = new BcPGPDigestCalculatorProvider().get( HashAlgorithmTags.SHA256 );

        // bcpg 1.48 exposes this API that includes s2kcount. Earlier
        // versions use a default of 0x60.
        //        PBESecretKeyEncryptor pske = ( new BcPBESecretKeyEncryptorBuilder( PGPEncryptedData.AES_256 ) )
        // .build( pass );

        PBESecretKeyEncryptor pske =
                ( new BcPBESecretKeyEncryptorBuilder( PGPEncryptedData.AES_256, sha256Calc, s2kcount ) ).build( pass );
        // Finally, create the keyring itself. The constructor
        // takes parameters that allow it to generate the self
        // signature.
        PGPKeyRingGenerator keyRingGen =
                new PGPKeyRingGenerator( PGPSignature.POSITIVE_CERTIFICATION, rsakp_sign, id, sha1Calc,
                        signhashgen.generate(), null,
                        new BcPGPContentSignerBuilder( rsakp_sign.getPublicKey().getAlgorithm(),
                                HashAlgorithmTags.SHA1 ), pske );

        // Add our encryption subkey, together with its signature.
        keyRingGen.addSubKey( rsakp_enc, enchashgen.generate(), null );
        return keyRingGen;
    }


    /**
     * ********************************************************** Load Keyring  file into InpurStream.
     */
    public static InputStream getFileInputStream( String keyringFile )
    {
        try
        {
            FileInputStream keyIn = new FileInputStream( keyringFile );

            return keyIn;
        }
        catch ( IOException ex )
        {
            return null;
        }
    }


    public static String getKeyringArmored( String keyringFile ) throws PGPException
    {
        try
        {
            FileInputStream keyIn = new FileInputStream( keyringFile );
            InputStream keyring = PGPUtil.getDecoderStream( keyIn );
            PGPPublicKeyRingCollection pgpPub =
                    new PGPPublicKeyRingCollection( keyring, new JcaKeyFingerprintCalculator() );

            ByteArrayOutputStream encOut = new ByteArrayOutputStream();
            ArmoredOutputStream armorOut = new ArmoredOutputStream( encOut );

            armorOut.write( pgpPub.getEncoded() );
            armorOut.flush();
            armorOut.close();
            return new String( encOut.toByteArray() );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error loading keyring", e );
        }
    }
}
