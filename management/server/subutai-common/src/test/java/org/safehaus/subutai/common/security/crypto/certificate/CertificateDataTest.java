package org.safehaus.subutai.common.security.crypto.certificate;


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


    @Test
    public void testEnumAccessMethodType()
    {
        AccessMethodType ocsp = AccessMethodType.OCSP;
        ocsp.friendly();
        ocsp.oid();
        ocsp.resolveOid( "OCSP" );
        ocsp.resolveOid( "1.3.6.1.5.5.7.48.1" );
    }


    @Test
    public void testEnumAttributeType()
    {
        AttributeTypeType ocsp = AttributeTypeType.COMMON_NAME;
        ocsp.friendly();
        ocsp.oid();
        ocsp.resolveOid( "OCSP" );
        ocsp.resolveOid( "2.5.4.3" );
    }

}