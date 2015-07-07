package io.subutai.core.strategy.api;


/**
 * Thrown when container placement strategy not found
 */
public class StrategyNotFoundException extends StrategyException
{
    public StrategyNotFoundException( String msg )
    {
        super( msg );
    }
}
