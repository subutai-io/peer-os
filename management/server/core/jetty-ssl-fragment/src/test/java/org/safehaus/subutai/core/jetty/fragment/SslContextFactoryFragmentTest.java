package org.safehaus.subutai.core.jetty.fragment;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;


public class SslContextFactoryFragmentTest
{

    @Mock
    private SslContextFactoryFragment mockedFragment;


    @Before
    public void setUp() throws Exception
    {
        SslContextFactoryFragment fragment = new SslContextFactoryFragment();
    }


    @Test
    public void testGetSingleton() throws Exception
    {
        assertNotNull( SslContextFactoryFragment.getLastInstance() );
    }


    @Test
    public void testDoStop() throws Exception
    {
        SslContextFactoryFragment.getLastInstance().doStop();
    }


    @Test
    public void testReloadStores() throws Exception
    {
        SslContextFactoryFragment.getLastInstance().reloadStores();
    }


    @Test
    public void testReloadStoresThrowsException() throws Exception
    {
        SslContextFactoryFragment.getLastInstance().reloadStores();
    }


    private class TestSslContextFactoryFragment extends SslContextFactoryFragment
    {
        @Override
        protected void doStart() throws Exception
        {
            throw new Exception( "Exception for test purposes." );
        }
    }
}