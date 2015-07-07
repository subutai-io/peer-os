package io.subutai.common.security.crypto.certificate;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.security.SecurityProvider;
import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.common.security.crypto.certificate.CertificateManager;
import io.subutai.common.security.crypto.key.KeyManager;
import io.subutai.common.security.crypto.key.KeyPairType;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CertificateManagerTest
{
    private CertificateManager certificateManager;
    private KeyManager keyManager;
    private KeyPairGenerator generator;
    private KeyPair keyPair;

    @Mock
    KeyStore keyStore;
    @Mock
    CertificateData certificateData;


    @Before
    public void setUp() throws Exception
    {
        keyManager = new KeyManager();
        certificateManager = new CertificateManager();
        certificateManager.setDateParamaters();
        generator = keyManager.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
        keyPair = keyManager.generateKeyPair( generator );
    }


    @Test( expected = RuntimeException.class )
    public void testGenerateSelfSignedCertificateException() throws Exception
    {
        certificateManager
                .generateSelfSignedCertificate( keyStore, keyPair, SecurityProvider.BOUNCY_CASTLE, certificateData );
    }


    @Test
    public void testGenerateSelfSignedCertificate() throws Exception
    {
        when( certificateData.getCommonName() ).thenReturn( "commonName" );
        when( certificateData.getOrganizationUnit() ).thenReturn( "organizationUnit" );
        when( certificateData.getOrganizationName() ).thenReturn( "organizationName" );
        when( certificateData.getCountry() ).thenReturn( "KG" );
        when( certificateData.getLocalityName() ).thenReturn( "localityName" );
        when( certificateData.getState() ).thenReturn( "Running" );
        when( certificateData.getEmail() ).thenReturn( "email" );


        certificateManager
                .generateSelfSignedCertificate( keyStore, keyPair, SecurityProvider.BOUNCY_CASTLE, certificateData );
    }

}