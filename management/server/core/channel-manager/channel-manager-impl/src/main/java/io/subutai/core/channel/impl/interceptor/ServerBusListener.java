package io.subutai.core.channel.impl.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.http.asyncclient.AsyncHTTPConduit;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.peer.api.PeerManager;


/**
 * Bus listener class
 */
public class ServerBusListener extends AbstractFeature
{
    private final static Logger LOG = LoggerFactory.getLogger( ServerBusListener.class );
    private ChannelManagerImpl channelManagerImpl = null;
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


        if ( !ChannelSettings.SPECIAL_REST_BUS.contains( bus.getId() ) )
        {

            // initialise the feature on the bus, which will add the interceptors

            //***** RECEIVE    **********************************
            bus.getInInterceptors().add( new AccessControlInterceptor( channelManagerImpl ) );

            //***** PRE_STREAM **********************************
            bus.getOutInterceptors().add( new ClientOutInterceptor( channelManagerImpl, peerManager ) );

            //***** POST_LOGICAL **********************************
            bus.getOutInterceptors().add( new ClientHeaderInterceptor( channelManagerImpl, peerManager ) );

            //***** RECEIVE    **********************************
            bus.getInInterceptors().add( new ServerInInterceptor( channelManagerImpl, peerManager ) );

            //***** PRE_STREAM **********************************
            bus.getOutInterceptors().add( new ServerOutInterceptor( channelManagerImpl, peerManager ) );

            //***** RECEIVE    **********************************
            bus.getInInterceptors().add( new ClientInInterceptor( channelManagerImpl, peerManager ) );

            //*****************************************
            if ( bus.getId().equalsIgnoreCase( "cxfBusTemplateManager" ) )
            {
                bus.setProperty( AsyncHTTPConduit.USE_ASYNC, Boolean.FALSE );
            }

            if ( bus.getId().equalsIgnoreCase( "cxfBusRawManager" ) )
            {
                bus.setProperty( AsyncHTTPConduit.USE_ASYNC, Boolean.FALSE );
            }
            if ( bus.getId().equalsIgnoreCase( "cxfBusAptManager" ) )
            {
                bus.setProperty( AsyncHTTPConduit.USE_ASYNC, Boolean.FALSE );
            }

            //***************************************************************

            //***************************************************************
        }
        else
        {
            LOG.warn( "Channel Manager specific interceptors are skipped for the bus: " + bus.getId() );
        }

        LOG.info( "Successfully added LoggingFeature interceptor on bus: " + bus.getId() );
    }


    public ChannelManagerImpl getChannelManagerImpl()
    {
        return channelManagerImpl;
    }


    public void setChannelManager( final ChannelManagerImpl channelManagerImpl )
    {
        this.channelManagerImpl = channelManagerImpl;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }
}
