package io.subutai.common.security.crypto.certificate;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.security.crypto.key.KeyManager;
import io.subutai.common.security.crypto.key.KeyPairType;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CertificateToolTest
{
    private CertificateTool certificateTool;
    private KeyManager keyManager;
    private KeyPairGenerator generator;
    private KeyPair keyPair;

    @Mock
    CertificateData certificateData;


    @Before
    public void setUp() throws Exception
    {
        keyManager = new KeyManager();
        certificateTool = new CertificateTool();
        certificateTool.setDateParamaters();
        generator = keyManager.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
        keyPair = keyManager.generateKeyPair( generator );
    }


    @Test( expected = RuntimeException.class )
    public void testGenerateSelfSignedCertificateException() throws Exception
    {
        certificateTool.generateSelfSignedCertificate( keyPair, certificateData );
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


        certificateTool.generateSelfSignedCertificate( keyPair, certificateData );
    }
}