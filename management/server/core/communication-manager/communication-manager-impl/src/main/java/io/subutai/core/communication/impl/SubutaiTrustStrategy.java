package io.subutai.core.communication.impl;


import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.TrustStrategy;

import io.subutai.core.communication.api.InvalidServerCertificate;
import io.subutai.core.communication.api.SecurityMaterials;


/**
 * Created by tzhamakeev on 8/3/15.
 */
public class SubutaiTrustStrategy implements TrustStrategy
{
    private static final Logger log = LoggerFactory.getLogger( SubutaiTrustStrategy.class );

    private String fingerPrint;

    private PGPPublicKey pgpPublicKey;

    private SecurityMaterials securityMaterials;


    public SubutaiTrustStrategy( SecurityMaterials securityMaterials )
    {
        this.securityMaterials = securityMaterials;
    }


    @Override
    public boolean isTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException
    {
        log.debug( String.format( "Chain length: %d. AuthType: %s", chain.length, authType ) );

        if ( chain.length != 1 )
        {
            return false;
        }

        X509Certificate cert = chain[0];
        log.debug( cert.toString() );

        try
        {
            cert.checkValidity();
            Principal p = cert.getSubjectDN();
            fingerPrint = StringUtils.substringBetween( p.getName(), "CN=", "," );
            log.debug( "Fingerprint: " + fingerPrint );

            pgpPublicKey = securityMaterials.getRecipientGPGPublicKey();

            if ( !Arrays.equals( pgpPublicKey.getFingerprint(), Hex.decodeHex( fingerPrint.toCharArray() ) ) )
            {
                throw new InvalidServerCertificate( fingerPrint );
            }
        }
        catch ( Exception e )
        {
            log.error( e.toString() );
            return false;
        }

        return true;
    }


    public PGPPublicKey getPgpPublicKey()
    {
        return pgpPublicKey;
    }


    public String getFingerPrint()
    {
        return fingerPrint;
    }
}
