package org.safehaus.subutai.core.jetty.fragment;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.mock;


public class SslContextFactoryFragmentTest
{

    @Mock
    private SslContextFactoryFragment mockedFragment;


    @Before
    public void setUp() throws Exception
    {
        SslContextFactoryFragment fragment = new SslContextFactoryFragment();
        SslContextFactoryFragment.setSslContextFactory( fragment );
    }


    @Test
    public void testGetSingleton() throws Exception
    {
        SslContextFactoryFragment.setSslContextFactory( mockedFragment );
        assertEquals( mockedFragment, SslContextFactoryFragment.getSingleton() );
    }


    @Test
    public void testDoStop() throws Exception
    {
        SslContextFactoryFragment.getSingleton().doStop();
    }


    @Test
    public void testReloadStores() throws Exception
    {
        SslContextFactoryFragment.getSingleton().reloadStores();
    }


    @Test
    public void testReloadStoresThrowsException() throws Exception
    {
        SslContextFactoryFragment.setSslContextFactory( new TestSslContextFactoryFragment() );
        SslContextFactoryFragment.getSingleton().reloadStores();
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