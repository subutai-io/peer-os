package io.subutai.common.peer;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Environment;
import io.subutai.common.metric.QuotaAlertValue;


/**
 * Exceeded quota alert handler implementation
 */
public abstract class ExceededQuotaAlertHandler extends AbstractAlertHandler<QuotaAlertValue>
{
    protected static final Logger LOGGER = LoggerFactory.getLogger( ExceededQuotaAlertHandler.class );
    private EnvironmentContainerHost sourceHost;


    @Override
    abstract public String getDescription();


    @Override
    public Class<QuotaAlertValue> getSupportedAlertValue()
    {
        return QuotaAlertValue.class;
    }


    @Override
    public void preProcess( final Environment environment, final QuotaAlertValue alert ) throws AlertHandlerException
    {
        findSourceHost( environment, alert.getValue().getHostId().getId() );
    }


    @Override
    abstract public void process( final Environment environment, final QuotaAlertValue alert )
            throws AlertHandlerException;


    @Override
    public void postProcess( final Environment environment, final QuotaAlertValue alert ) throws AlertHandlerException
    {}


    protected void findSourceHost( Environment environment, final String sourceHostId ) throws AlertHandlerException
    {
        EnvironmentContainerHost sourceHost = null;

        //get environment containers and find alert's source host
        Set<EnvironmentContainerHost> containers = environment.getContainerHosts();

        for ( EnvironmentContainerHost containerHost : containers )
        {
            if ( containerHost.getId().equalsIgnoreCase( sourceHostId ) )
            {
                sourceHost = containerHost;
                break;
            }
        }

        if ( sourceHost == null )
        {
            throw new AlertHandlerException( "Source host not found." );
        }

        this.sourceHost = sourceHost;
    }


    public EnvironmentContainerHost getSourceHost()
    {
        return sourceHost;
    }
}
