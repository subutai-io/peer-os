package org.safehaus.subutai.common.security.crypto.ssl;


import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class NaiveTrustManagerTest
{
    private NaiveTrustManager naiveTrustManager;
    private X509Certificate[] certificates;


    @Before
    public void setUp() throws Exception
    {
        naiveTrustManager = new NaiveTrustManager();
    }


    @Test
    public void testCheckClientTrusted() throws Exception
    {
        naiveTrustManager.checkClientTrusted( certificates, "authType" );
    }


    @Test
    public void testCheckServerTrusted() throws Exception
    {
        naiveTrustManager.checkServerTrusted( certificates, "authtype" );
    }


    @Test
    public void testGetAcceptedIssuers() throws Exception
    {
        naiveTrustManager.getAcceptedIssuers();
    }
}