package io.subutai.core.broker.impl;


import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.common.security.crypto.key.KeyPairType;


public class SslUtil
{

    /**
     * Converts certificates and keys to PEM format
     *
     * @param obj - certificate / private key to convert, usually in DER format
     *
     * @return - certificate / private key in PEM format
     */
    public static String convertToPem( Object obj )
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
     * Generates a key-pair
     *
     * @param keyPairType - type of algorithm
     * @param keySize - key size
     */
    public KeyPair generateKeyPair( KeyPairType keyPairType, int keySize )
    {
        try
        {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance( keyPairType.jce() );
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
