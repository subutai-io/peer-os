package org.safehaus.subutai.core.ssl.manager.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.jetty.fragment.SslContextFactoryFragment;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiSslContextFactoryImplTest
{
    SubutaiSslContextFactoryImpl sslContextFactory;


    @Before
    public void setUp() throws Exception
    {
        SslContextFactoryFragment.setSslContextFactory( new SslContextFactoryFragment() );
        sslContextFactory = new SubutaiSslContextFactoryImpl();
    }


    @Test
    public void testReloadKeyStore() throws Exception
    {
        sslContextFactory.reloadKeyStore();
    }


    @Test
    public void testReloadTrustStore() throws Exception
    {
        sslContextFactory.reloadTrustStore();
    }


    @Test
    public void testGetSSLContext() throws Exception
    {
        sslContextFactory.getSSLContext();
    }
}