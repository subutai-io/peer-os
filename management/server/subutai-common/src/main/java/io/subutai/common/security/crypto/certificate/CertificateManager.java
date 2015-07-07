package io.subutai.common.security.crypto.certificate;


import java.math.BigInteger;
import java.security.KeyPair;
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
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import io.subutai.common.security.SecurityProvider;


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
public class CertificateManager
{
    private Date notBefore = null;
    private Date notAfter = null;
    private X509Certificate x509cert = null;


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


    /**
     * *********************************************************************************** Generate x509 Certificate
     *
     * @param keyStore KeyStore
     * @param keyPair KeyPair
     * @param securityProvider SecurityProvider
     * @param certificateData CertificateData
     *
     * @return X509Certificate
     */
    public X509Certificate generateSelfSignedCertificate( KeyStore keyStore, KeyPair keyPair,
                                                          SecurityProvider securityProvider,
                                                          CertificateData certificateData )
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
            ContentSigner sigGen =
                    new JcaContentSignerBuilder( "SHA256WithRSAEncryption" ).setProvider( securityProvider.jce() ).
                            build( keyPair.getPrivate() );
            x509cert = new JcaX509CertificateConverter().setProvider( securityProvider.jce() ).
                    getCertificate( certGen.build( sigGen ) );
            x509cert.checkValidity( new Date() );
            x509cert.verify( x509cert.getPublicKey() );
        }
        catch ( Exception t )
        {
            throw new RuntimeException( "Failed to generate self-signed certificate!", t );
        }

        return x509cert;
    }
}