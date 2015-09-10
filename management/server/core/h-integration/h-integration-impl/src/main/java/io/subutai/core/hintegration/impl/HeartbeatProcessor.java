package io.subutai.core.hintegration.impl;


import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.core.hintegration.api.Integration;
import io.subutai.hub.common.dto.HeartbeatResponseDTO;


/**
 * Hearbeat processor
 */
public class HeartbeatProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( HeartbeatProcessor.class );
    private List<CommandProcessor> processors = new CopyOnWriteArrayList<CommandProcessor>();
    private Integration integration;


    public HeartbeatProcessor( final Integration integration )
    {
        this.integration = integration;
    }


    public void addProcessor( CommandProcessor commandProcessor )
    {
        this.processors.add( commandProcessor );
    }


    @Override
    public void run()
    {
        LOG.debug( "Hearbeat processor started..." );
        try
        {
            Set<String> response = integration.sendHeartbeat();
            if ( response != null )
            {
                for ( String l : response )
                {

                }

                for ( CommandProcessor processor : this.processors )
                {
                    // here the body of runner
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
        LOG.debug( "Hearbeat processor done." );
    }
}
