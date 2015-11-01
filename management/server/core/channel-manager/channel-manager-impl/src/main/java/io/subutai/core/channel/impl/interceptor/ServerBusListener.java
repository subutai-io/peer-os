package io.subutai.core.channel.impl.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;

import io.subutai.core.channel.impl.ChannelManagerImpl;


/**
 * Bus listener class
 */
public class ServerBusListener extends AbstractFeature
{
    private final static Logger LOG = LoggerFactory.getLogger( ServerBusListener.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public void busRegistered( Bus bus )
    {
        LOG.info( "Adding LoggingFeature interceptor on bus: " + bus );

        //********Set BUS Message Size to 500 KB ************************
        bus.setProperty( "bus.io.CachedOutputStream.Threshold", "500000" );
        System.setProperty( "org.apache.cxf.io.CachedOutputStream.Threshold", "500000" );
        LOG.info( "Setting CXF CachedOutputStream.Threshold size to: 500Kb ");
        //***************************************************************

        // initialise the feature on the bus, which will add the interceptors

        //***** RECEIVE    **********************************
        bus.getInInterceptors().add( new AccessControlInterceptor(channelManagerImpl) );

        //***** PRE_STREAM **********************************
        bus.getOutInterceptors().add( new ClientOutInterceptor(channelManagerImpl) );

        //***** RECEIVE    **********************************
        bus.getInInterceptors().add( new ServerInInterceptor(channelManagerImpl) );

        //***** PRE_STREAM **********************************
        bus.getOutInterceptors().add( new ServerOutInterceptor(channelManagerImpl) );

        //***** RECEIVE    **********************************
        bus.getInInterceptors().add( new ClientInInterceptor(channelManagerImpl) );

        LOG.info( "Successfully added LoggingFeature interceptor on bus: " + bus );
    }


    public ChannelManagerImpl getChannelManagerImpl()
    {
        return channelManagerImpl;
    }


    public void setChannelManager( final ChannelManagerImpl channelManagerImpl )
    {
        this.channelManagerImpl = channelManagerImpl;
    }
}
