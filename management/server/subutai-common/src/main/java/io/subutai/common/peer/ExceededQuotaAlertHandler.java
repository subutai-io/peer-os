package io.subutai.common.peer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.metric.QuotaAlertValue;


/**
 * Exceeded quota alert handler implementation
 */
public abstract class ExceededQuotaAlertHandler extends AbstractAlertHandler<QuotaAlertValue>
{
    protected static final Logger LOGGER = LoggerFactory.getLogger( ExceededQuotaAlertHandler.class );


    @Override
    abstract public String getDescription();


    @Override
    public Class<QuotaAlertValue> getSupportedAlertValue()
    {
        return QuotaAlertValue.class;
    }


    @Override
    public void preProcess( final QuotaAlertValue alert ) throws AlertHandlerException {}


    @Override
    abstract public void process( final QuotaAlertValue alert ) throws AlertHandlerException;


    @Override
    public void postProcess( final QuotaAlertValue alert ) throws AlertHandlerException {}
}
