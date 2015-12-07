package io.subutai.core.environment.impl;


import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.AlertHandlerException;
import io.subutai.common.peer.ExceededQuotaAlertHandler;


/**
 * Example implementation of exceeded quota alert handler
 */
public class ExampleAlertHandler extends ExceededQuotaAlertHandler
{

    @Override
    public String getId()
    {
        return "EXAMPLE_ALERT_HANDLER_ID";
    }


    @Override
    public String getDescription()
    {
        return "Example implementation of alert handler.";
    }


    @Override
    public void preProcess( final QuotaAlertValue alert ) throws AlertHandlerException
    {
        LOGGER.debug( "Example alert handler pre-processor started" );

        LOGGER.debug( alert.toString() );

        LOGGER.debug( "Example alert handler pre-processor finished" );
    }


    @Override
    public void process( final QuotaAlertValue alert )
    {
        LOGGER.debug( "Example alert handler main processor started" );

        LOGGER.debug( alert.toString() );

        LOGGER.debug( "Example alert handler main processor finished" );
    }


    @Override
    public void postProcess( final QuotaAlertValue alert ) throws AlertHandlerException
    {
        LOGGER.debug( "Example alert handler post-processor started" );

        LOGGER.debug( alert.toString() );

        LOGGER.debug( "Example alert handler post-processor finished" );
    }
}
