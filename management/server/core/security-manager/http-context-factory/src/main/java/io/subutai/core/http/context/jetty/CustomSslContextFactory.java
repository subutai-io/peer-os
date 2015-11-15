package io.subutai.core.http.context.jetty;


import java.security.KeyStore;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * extends Jetty SSLContextFactory
 */
public class CustomSslContextFactory extends SslContextFactory
{
    private static Logger LOG = LoggerFactory.getLogger( CustomSslContextFactory.class.getName() );

    private static volatile CustomSslContextFactory lastInstance = new CustomSslContextFactory();

    private boolean customStart = false;

    //TODO take these parameters from config file
    private String _keyStorePassword = "subutai";
    private String _trustStorePassword = "subutai";


    public CustomSslContextFactory()
    {
        super();
        lastInstance = this;
    }


    public static CustomSslContextFactory getLastInstance()
    {
        return lastInstance;
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
            LOG.debug( "Reloading ssl context factory" );
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