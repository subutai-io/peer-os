package io.subutai.common.peer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Environment;
import io.subutai.common.metric.AlertValue;


/**
 * Base implementation of AlertHandler interface
 */
public abstract class AbstractAlertHandler<T extends AlertValue> implements AlertHandler<T>
{
    protected static final Logger LOGGER = LoggerFactory.getLogger( AbstractAlertHandler.class );


    @Override
    abstract public String getDescription();


    @Override
    public void preProcess( final Environment environment, final T alert ) throws AlertHandlerException {}


    @Override
    abstract public void process( final Environment environment, final T alert ) throws AlertHandlerException;


    @Override
    public void postProcess( final Environment environment, final T alert ) throws AlertHandlerException {}
}
