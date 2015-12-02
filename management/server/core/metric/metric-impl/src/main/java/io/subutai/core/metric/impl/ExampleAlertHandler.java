package io.subutai.core.metric.impl;


import io.subutai.common.peer.AbstractAlertHandler;
import io.subutai.common.peer.AlertHandlerException;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.AlertPack;


/**
 * Default implementation of AlertHandler interface
 */
public class ExampleAlertHandler extends AbstractAlertHandler
{
    @Override
    public String getHandlerId()
    {
        return "EXAMPLE_ALERT_HANDLER_ID";
    }

    @Override
    public void preProcess( final AlertPack alert ) throws AlertHandlerException
    {
        LOGGER.debug( "Example alert handler pre-processor started" );

        LOGGER.debug( alert.toString() );

        LOGGER.debug( "Example alert handler pre-processor finished" );
    }


    @Override
    public void process( final AlertPack alert )
    {
        LOGGER.debug( "Example alert handler main processor started" );

        LOGGER.debug( alert.toString() );

        LOGGER.debug( "Example alert handler main processor finished" );
    }


    @Override
    public void postProcess( final AlertPack alert ) throws AlertHandlerException
    {
        LOGGER.debug( "Example alert handler post-processor started" );

        LOGGER.debug( alert.toString() );

        LOGGER.debug( "Example alert handler post-processor finished" );
    }
}
