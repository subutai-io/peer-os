package org.safehaus.subutai.core.jetty.fragment;


import java.util.UUID;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSslContextFactory extends SslContextFactory
{
    private static Logger LOG = LoggerFactory.getLogger( TestSslContextFactory.class.getName() );

    private static UUID id;


    public TestSslContextFactory()
    {
        super();
        id = UUID.randomUUID();
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
    }


    public static void DO_IT()
    {
        LOG.error( "THE ID >>>> " + id.toString() );
    }


    @Override
    public void setTrustStore( final String trustStorePath )
    {
        super.setTrustStore( trustStorePath );

        LOG.error( "TURST STORE PATH >>>" + trustStorePath );
    }
}
