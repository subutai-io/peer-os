package io.subutai.core.hintegration.impl;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.hintegration.api.HIntegrationException;
import io.subutai.core.hintegration.api.Integration;


/**
 * Hearbeat processor
 */
public class HeartbeatProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( HeartbeatProcessor.class );
    private Integration integration;


    public HeartbeatProcessor( final Integration integration )
    {
        this.integration = integration;
    }




    @Override
    public void run()
    {
        try
        {
            LOG.debug( "Heartbeat sending started..." );
            Set<String> stateLinks = integration.sendHeartbeat();

            for ( String link : stateLinks )
            {
                LOG.debug( "Processing state link: " + link );
                integration.processStateLink( link );
            }

            LOG.debug( "Heartbeat sending finished successfully." );
        }
        catch ( Exception e )
        {
            LOG.debug( "Heartbeat sending failed." );
            LOG.error( e.getMessage(), e );
        }
    }
}
