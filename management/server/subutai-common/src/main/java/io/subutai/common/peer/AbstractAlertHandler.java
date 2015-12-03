package io.subutai.common.peer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base implementation of AlertHandler interface
 */
public abstract class AbstractAlertHandler implements AlertHandler
{
    protected static final Logger LOGGER = LoggerFactory.getLogger( AbstractAlertHandler.class );


    @Override
    abstract public String getDescription();


    @Override
    public void preProcess( final AlertPack alert ) throws AlertHandlerException {}


    @Override
    abstract public void process( final AlertPack alert ) throws AlertHandlerException ;


    @Override
    public void postProcess( final AlertPack alert ) throws AlertHandlerException {}
}
