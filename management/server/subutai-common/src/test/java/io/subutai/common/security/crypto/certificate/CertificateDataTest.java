package io.subutai.common.security.crypto.certificate;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CertificateDataTest
{
    private CertificateData certificateData;


    @Before
    public void setUp() throws Exception
    {
        certificateData = new CertificateData();
        certificateData.setCommonName( "commonName" );
        certificateData.setCountry( "KG" );
        certificateData.setEmail( "email" );
        certificateData.setLocalityName( "localityName" );
        certificateData.setOrganizationName( "organization" );
        certificateData.setOrganizationUnit( "organizationUnit" );
        certificateData.setState( "running" );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( certificateData.getCommonName() );
        assertNotNull( certificateData.getCountry() );
        assertNotNull( certificateData.getEmail() );
        assertNotNull( certificateData.getLocalityName() );
        assertNotNull( certificateData.getOrganizationName() );
        assertNotNull( certificateData.getOrganizationUnit() );
        assertNotNull( certificateData.getState() );
    }
}