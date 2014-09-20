package org.safehaus.subutai.core.strategy.api;


/**
 * Thrown when container placement strategy not available
 */
public class StrategyNotAvailable extends StrategyException
{
    public StrategyNotAvailable( String msg )
    {
        super( msg );
    }
}
