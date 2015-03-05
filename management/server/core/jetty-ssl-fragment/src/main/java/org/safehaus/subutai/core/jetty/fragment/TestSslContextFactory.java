package org.safehaus.subutai.core.jetty.fragment;


import java.security.KeyStore;
import java.util.UUID;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSslContextFactory extends SslContextFactory
{
    private static Logger LOG = LoggerFactory.getLogger( TestSslContextFactory.class.getName() );

    private static UUID id = UUID.randomUUID();

    private static TestSslContextFactory singleton;

    private boolean customStart = false;

    private String _keyStorePassword = "subutai";
    private String _trustStorePassword = "subutai";


    public TestSslContextFactory()
    {
        super();
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
        TestSslContextFactory.singleton = this;
    }


    public TestSslContextFactory( final boolean trustAll )
    {
        super( trustAll );
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
        TestSslContextFactory.singleton = this;
    }


    public TestSslContextFactory( final String keyStorePath )
    {
        super( keyStorePath );
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
        TestSslContextFactory.singleton = this;
    }


    public static TestSslContextFactory getSingleton()
    {
        return singleton;
    }


    public static void DO_IT()
    {
        LOG.error( "THE ID >>>> " + id.toString() );
    }


    @Override
    protected void doStop() throws Exception
    {
        if ( customStart )
        {
            stop();
            super.doStop();
        }
        else
        {
            super.doStop();
        }
    }


    public void reloadStores()
    {
        try
        {
            setCustomStart( true );
            doStop();
            setCustomStart( false );

            setSslContext( null );

            setKeyStore( ( KeyStore ) null );

            setTrustStore( ( KeyStore ) null );

            setKeyStorePassword( _keyStorePassword );

            setTrustStorePassword( _trustStorePassword );

            Thread.sleep( 1000 );

            doStart();
        }
        catch ( Exception e )
        {
            LOG.error( "Error restarting ssl context factory", e );
        }
    }


    public void setCustomStart( final boolean customStart )
    {
        this.customStart = customStart;
    }
}
