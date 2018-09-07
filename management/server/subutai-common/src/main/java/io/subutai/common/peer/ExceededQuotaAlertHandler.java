package io.subutai.common.peer;


import java.util.Set;

import io.subutai.common.environment.Environment;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.bazaar.share.quota.ContainerCpuResource;
import io.subutai.bazaar.share.quota.ContainerDiskResource;
import io.subutai.bazaar.share.quota.ContainerRamResource;


/**
 * Exceeded quota alert handler implementation
 */
public abstract class ExceededQuotaAlertHandler extends AbstractAlertHandler<QuotaAlertValue>
{
    private EnvironmentContainerHost sourceHost;
    protected ContainerCpuResource cpuResource;
    protected ContainerRamResource ramResource;
    protected ContainerDiskResource hddResource;


    @Override
    public Class<QuotaAlertValue> getSupportedAlertValue()
    {
        return QuotaAlertValue.class;
    }


    @Override
    public void preProcess( final Environment environment, final QuotaAlertValue alert ) throws AlertHandlerException
    {
        findSourceHost( environment, alert.getValue().getHostId().getId() );
        cpuResource = new ContainerCpuResource( "100%" );
        ramResource = new ContainerRamResource( alert.getValue().getResourceHostMetric().getAvailableRam().toString() );
        hddResource =
                new ContainerDiskResource( alert.getValue().getResourceHostMetric().getAvailableSpace().toString() );
    }


    @Override
    abstract public void process( final Environment environment, final QuotaAlertValue alert )
            throws AlertHandlerException;


    @Override
    public void postProcess( final Environment environment, final QuotaAlertValue alert ) throws AlertHandlerException
    {
    }


    protected void findSourceHost( Environment environment, final String sourceHostId ) throws AlertHandlerException
    {
        EnvironmentContainerHost aSourceHost = null;

        //get environment containers and find alert's source host
        Set<EnvironmentContainerHost> containers = environment.getContainerHosts();

        for ( EnvironmentContainerHost containerHost : containers )
        {
            if ( containerHost.getId().equalsIgnoreCase( sourceHostId ) )
            {
                aSourceHost = containerHost;
                break;
            }
        }

        if ( aSourceHost == null )
        {
            throw new AlertHandlerException( "Source host not found." );
        }

        this.sourceHost = aSourceHost;
    }


    public EnvironmentContainerHost getSourceHost()
    {
        return sourceHost;
    }
}
