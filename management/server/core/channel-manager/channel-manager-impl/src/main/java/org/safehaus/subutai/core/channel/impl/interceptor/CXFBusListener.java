package org.safehaus.subutai.core.channel.impl.interceptor;


import org.safehaus.subutai.core.channel.impl.ChannelManagerImpl;
import org.safehaus.subutai.core.channel.impl.token.ChannelTokenManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;


/**
 * Created by talas on 2/23/15.
 */
public class CXFBusListener extends AbstractFeature
{
    private final static Logger LOG = LoggerFactory.getLogger( CXFBusListener.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public void busRegistered( Bus bus )
    {
        LOG.info( "Adding LoggingFeature interceptor on bus: " + bus );

        // initialise the feature on the bus, which will add the interceptors
        bus.getInInterceptors().add( new CXFInterceptor(channelManagerImpl) );


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
