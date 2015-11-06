package io.subutai.core.broker.impl;


import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;

import io.subutai.common.security.crypto.certificate.CertificateData;


public class SslUtil
{

    /**
     * Converts certificates and keys to PEM format
     *
     * @param obj - certificate / private key to convert, usually in DER format
     *
     * @return - certificate / private key in PEM format
     */
    public String convertToPem( Object obj )
    {
        try
        {
            StringWriter stringWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter( stringWriter );
            pemWriter.writeObject( obj );
            pemWriter.flush();
            pemWriter.close();

            return stringWriter.toString();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to convert object to PEM format", e );
        }
    }


    /**
     * Generates X.509 self-signed certificate from the given keypair
     *
     * @param keyPair - a keypair to use for cert generation
     * @param certificateData - metadata for certificate
     *
     * @return - X509 certificate {@code X509Certificate}
     */
    public X509Certificate generateSelfSignedCertificate( KeyPair keyPair, CertificateData certificateData )
    {
        try
        {
            Security.addProvider( new org.bouncycastle.jce.provider.BouncyCastleProvider() );


            //******************************************************************************
            // Generate self-signed certificate

            X500NameBuilder builder = new X500NameBuilder( BCStyle.INSTANCE );
            builder.addRDN( BCStyle.CN, certificateData.getCommonName() );
            builder.addRDN( BCStyle.OU, certificateData.getOrganizationUnit() );
            builder.addRDN( BCStyle.O, certificateData.getOrganizationName() );
            builder.addRDN( BCStyle.C, certificateData.getCountry() );
            builder.addRDN( BCStyle.L, certificateData.getLocalityName() );
            builder.addRDN( BCStyle.ST, certificateData.getState() );
            builder.addRDN( BCStyle.EmailAddress, certificateData.getEmail() );

            BigInteger serial = BigInteger.valueOf( System.currentTimeMillis() );

            Calendar cal = Calendar.getInstance();
            Date notBefore = cal.getTime();
            cal.add( Calendar.YEAR, 10 );
            Date notAfter = cal.getTime();

            X509v3CertificateBuilder certGen =
                    new JcaX509v3CertificateBuilder( builder.build(), serial, notBefore, notAfter, builder.build(),
                            keyPair.getPublic() );
            ContentSigner sigGen = new JcaContentSignerBuilder( "SHA256WithRSAEncryption" ).setProvider( "BC" ).
                    build( keyPair.getPrivate() );
            X509Certificate x509cert = new JcaX509CertificateConverter().setProvider( "BC" ).
                    getCertificate( certGen.build( sigGen ) );
            x509cert.checkValidity( new Date() );
            x509cert.verify( x509cert.getPublicKey() );
            return x509cert;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to generate self-signed certificate", e );
        }
    }


    /**
     * Generates X.509 certificate from the given keypair, signed by the pass-in CA key
     *
     * @param keyPair - a keypair to use for cert generation
     * @param certificateData - metadata for certificate
     * @param caKey - CA key to sign the certificate
     *
     * @return - X509 certificate {@code X509Certificate}
     */
    public X509Certificate generateCaKeySignedCertificate( KeyPair keyPair, CertificateData certificateData,
                                                           KeyPair caKey )
    {
        try
        {
            Security.addProvider( new org.bouncycastle.jce.provider.BouncyCastleProvider() );

            //******************************************************************************
            // Generate self-signed certificate
            X500NameBuilder builder = new X500NameBuilder( BCStyle.INSTANCE );
            builder.addRDN( BCStyle.CN, certificateData.getCommonName() );
            builder.addRDN( BCStyle.OU, certificateData.getOrganizationUnit() );
            builder.addRDN( BCStyle.O, certificateData.getOrganizationName() );
            builder.addRDN( BCStyle.C, certificateData.getCountry() );
            builder.addRDN( BCStyle.L, certificateData.getLocalityName() );
            builder.addRDN( BCStyle.ST, certificateData.getState() );
            builder.addRDN( BCStyle.EmailAddress, certificateData.getEmail() );

            BigInteger serial = BigInteger.valueOf( System.currentTimeMillis() );

            Calendar cal = Calendar.getInstance();
            Date notBefore = cal.getTime();
            cal.add( Calendar.YEAR, 10 );
            Date notAfter = cal.getTime();

            X509v3CertificateBuilder certGen =
                    new JcaX509v3CertificateBuilder( builder.build(), serial, notBefore, notAfter, builder.build(),
                            keyPair.getPublic() );
            ContentSigner sigGen = new JcaContentSignerBuilder( "SHA256WithRSAEncryption" ).setProvider( "BC" ).
                    build( caKey.getPrivate() );
            X509Certificate x509cert = new JcaX509CertificateConverter().setProvider( "BC" ).
                    getCertificate( certGen.build( sigGen ) );
            x509cert.checkValidity( new Date() );
            x509cert.verify( caKey.getPublic() );
            return x509cert;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to generate self-signed certificate", e );
        }
    }


    /**
     * Converts private key to password-protected PEM form
     *
     * @param privateKey - key to protect
     * @param password - key password
     *
     * @return - key in PEM format
     */
    public String encryptKey( PrivateKey privateKey, String password )
    {
        try
        {
            JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder =
                    new JceOpenSSLPKCS8EncryptorBuilder( PKCS8Generator.PBE_SHA1_3DES );
            encryptorBuilder.setRandom( new SecureRandom() );
            encryptorBuilder.setPasssword( password.toCharArray() );
            OutputEncryptor oe = encryptorBuilder.build();
            JcaPKCS8Generator gen = new JcaPKCS8Generator( privateKey, oe );
            PemObject obj = gen.generate();
            StringWriter sw = new StringWriter();
            JcaPEMWriter pemWrt = new JcaPEMWriter( sw );
            pemWrt.writeObject( obj );
            pemWrt.close();

            return sw.toString();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to encrypt key", e );
        }
    }


    /**
     * Loads encrypted private RSA key
     *
     * @param privateKeyFilePath - path to key
     * @param password - key password
     *
     * @return - keypair generated from the private key
     */
    public KeyPair loadEncryptedRsaPrivateKey( String privateKeyFilePath, String password )
    {
        try
        {
            Security.addProvider( new org.bouncycastle.jce.provider.BouncyCastleProvider() );

            PEMParser pemParser = new PEMParser( new FileReader( privateKeyFilePath ) );
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider( "BC" );


            InputDecryptorProvider pkcs8decoder =
                    new JceOpenSSLPKCS8DecryptorProviderBuilder().build( password.toCharArray() );
            object = converter
                    .getPrivateKey( ( ( PKCS8EncryptedPrivateKeyInfo ) object ).decryptPrivateKeyInfo( pkcs8decoder ) );

            RSAPrivateCrtKey privk = ( RSAPrivateCrtKey ) object;
            RSAPublicKeySpec publicKeySpec =
                    new java.security.spec.RSAPublicKeySpec( privk.getModulus(), privk.getPublicExponent() );

            KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
            PublicKey myPublicKey = keyFactory.generatePublic( publicKeySpec );
            return new KeyPair( myPublicKey, privk );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to load private key", e );
        }
    }


    /**
     * Generates a key-pair
     *
     * @param algorithm - key algorithm
     * @param keySize - key size
     */
    public KeyPair generateKeyPair( String algorithm, int keySize )
    {
        try
        {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance( algorithm );
            keyPairGen.initialize( keySize );
            return keyPairGen.genKeyPair();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to generate key-pair", e );
        }
    }


    /**
     * Returns certificate by alias from keystore
     *
     * @param keyStorePath - path to keystore to search
     * @param alias - alias to search by
     */
    public X509Certificate getCertificateByAlias( String keyStorePath, String keyStorePassword, String alias )
    {
        try
        {
            KeyStore ts = KeyStore.getInstance( KeyStore.getDefaultType() );
            InputStream in = null;
            try
            {
                in = new FileInputStream( keyStorePath );
                ts.load( in, keyStorePassword.toCharArray() );

                if ( ts.isCertificateEntry( alias ) )
                {
                    return ( X509Certificate ) ts.getCertificate( alias );
                }
                else
                {
                    return ( X509Certificate ) ts.getCertificateChain( alias )[0];
                }
            }
            finally
            {
                if ( in != null )
                {
                    in.close();
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to obtain certificate by alias", e );
        }
    }
}
