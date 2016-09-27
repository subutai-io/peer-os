package io.subutai.core.channel.impl.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.http.asyncclient.AsyncHTTPConduit;

import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


/**
 * Bus listener class
 */
public class ServerBusListener extends AbstractFeature
{
    private final static Logger LOG = LoggerFactory.getLogger( ServerBusListener.class );
    private SecurityManager securityManager;
    private IdentityManager identityManager;
    private PeerManager peerManager;


    public void busRegistered( Bus bus )
    {
        LOG.info( "Adding LoggingFeature interceptor on bus: " + bus.getId() );

        //********Set BUS Message Size to 500 KB ************************
        bus.setProperty( "bus.io.CachedOutputStream.Threshold", "500000" );
        System.setProperty( "org.apache.cxf.io.CachedOutputStream.Threshold", "500000" );
        LOG.info( "Setting CXF CachedOutputStream.Threshold size to: 500Kb " );
        //***************************************************************
        bus.setProperty( AsyncHTTPConduit.USE_ASYNC, Boolean.TRUE );
        //***************************************************************

        // initialise the feature on the bus, which will add the interceptors

        //***** RECEIVE    **********************************
        bus.getInInterceptors().add( new AccessControlInterceptor( identityManager ) );

        //***** PRE_STREAM **********************************
        bus.getOutInterceptors().add( new ClientOutInterceptor( securityManager, peerManager ) );

        //***** POST_LOGICAL **********************************
        bus.getOutInterceptors().add( new ClientHeaderInterceptor( peerManager ) );

        //***** RECEIVE    **********************************
        bus.getInInterceptors().add( new ServerInInterceptor( securityManager, peerManager ) );

        //***** PRE_STREAM **********************************
        bus.getOutInterceptors().add( new ServerOutInterceptor( securityManager, peerManager ) );

        //***** RECEIVE    **********************************
        bus.getInInterceptors().add( new ClientInInterceptor( securityManager, peerManager ) );


        LOG.info( "Successfully added LoggingFeature interceptor on bus: " + bus.getId() );
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }
}
