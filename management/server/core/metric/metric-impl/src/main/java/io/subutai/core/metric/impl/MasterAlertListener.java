package io.subutai.core.metric.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;


/**
 * Alert listener example implementation for template master
 */
public class MasterAlertListener implements AlertListener
{
    private static final Logger LOG = LoggerFactory.getLogger( MasterAlertListener.class );


    @Override
    public void onAlert( final AlertPack alert )
    {
        LOG.debug( "Master alert listener started" );

        LOG.debug( alert.toString() );

        LOG.debug( "Master alert listener done." );
    }


    @Override
    public String getTemplateName()
    {
        return "master";
    }
}
