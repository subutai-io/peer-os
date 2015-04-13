package org.safehaus.subutai.core.jetty.fragment;


import java.security.KeyStore;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SslContextFactoryFragment extends SslContextFactory
{
    private static Logger LOG = LoggerFactory.getLogger( SslContextFactoryFragment.class.getName() );

    private static volatile SslContextFactoryFragment singleton;

    private boolean customStart = false;

    private String _keyStorePassword = "subutai";
    private String _trustStorePassword = "subutai";


    public static SslContextFactoryFragment getSingleton()
    {
        return singleton;
    }


    public synchronized static void setSslContextFactory( SslContextFactoryFragment instance )
    {
        singleton = instance;
    }


    @Override
    protected void doStop() throws Exception
    {
        if ( customStart )
        {
            stop();
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
            LOG.debug( String.format( "Reloading ssl context factory" ) );
            setCustomStart( true );
            doStop();
            setCustomStart( false );

            setSslContext( null );

            setKeyStore( ( KeyStore ) null );

            setTrustStore( ( KeyStore ) null );

            setKeyStorePassword( _keyStorePassword );

            setTrustStorePassword( _trustStorePassword );

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
