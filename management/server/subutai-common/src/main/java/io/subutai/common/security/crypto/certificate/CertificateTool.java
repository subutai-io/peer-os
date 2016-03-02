package io.subutai.common.security.crypto.certificate;


import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import io.subutai.common.security.crypto.key.KeyManager;
import io.subutai.common.security.crypto.key.KeyPairType;


/**
 * Utility class to generate self-signed certificates.
 *
 * @author James Moger
 *
 */
/**
 * @author nisakov
 *
 */


/**
 * @author nisakov
 */
public class CertificateTool
{
    private Date notBefore = null;
    private Date notAfter = null;


    /**
     * *********************************************************************************** CertificateManager
     * Constructor
     */
    public void setDateParamaters()
    {
        Calendar cal = Calendar.getInstance();
        notBefore = cal.getTime();
        cal.add( Calendar.YEAR, 1 );
        notAfter = cal.getTime();
    }


    public X509Certificate generateSelfSignedCertificate( CertificateData certificateData )
    {
        KeyManager keyManager = new KeyManager();
        KeyPairGenerator generator = keyManager.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
        KeyPair keyPair = keyManager.generateKeyPair( generator );

        return generateSelfSignedCertificate( keyPair, certificateData );
    }


    /**
     * *********************************************************************************** Generate x509 Certificate
     *
     * @param keyPair KeyPair
     * @param certificateData CertificateData
     *
     * @return X509Certificate
     */
    public X509Certificate generateSelfSignedCertificate( KeyPair keyPair, CertificateData certificateData )
    {
        try
        {
            Security.addProvider( new org.bouncycastle.jce.provider.BouncyCastleProvider() );

            setDateParamaters();

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

            X509v3CertificateBuilder certGen =
                    new JcaX509v3CertificateBuilder( builder.build(), serial, notBefore, notAfter, builder.build(),
                            keyPair.getPublic() );
            ContentSigner sigGen = new JcaContentSignerBuilder( "SHA256WithRSAEncryption" ).
                                                                                                   build( keyPair
                                                                                                           .getPrivate() );
            X509Certificate x509cert = new JcaX509CertificateConverter().
                                                                                getCertificate(
                                                                                        certGen.build( sigGen ) );
            x509cert.checkValidity( new Date() );
            x509cert.verify( x509cert.getPublicKey() );
            return x509cert;
        }
        catch ( Exception t )
        {
            throw new RuntimeException( "Failed to generate self-signed certificate!", t );
        }
    }


    /**
     * Convert X509 certificate in PEM format to X509Certificate object
     *
     * @param x509InPem X509 certificate in PEM format
     *
     * @return {@code X509Certificate}
     */
    public X509Certificate convertX509PemToCert( String x509InPem )
    {
        try
        {
            PEMParser pemParser = new PEMParser( new StringReader( x509InPem ) );
            JcaX509CertificateConverter x509CertificateConverter = new JcaX509CertificateConverter();

            Object o = pemParser.readObject();
            return x509CertificateConverter.getCertificate( ( X509CertificateHolder ) o );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to convert object to PEM format", e );
        }
    }
}