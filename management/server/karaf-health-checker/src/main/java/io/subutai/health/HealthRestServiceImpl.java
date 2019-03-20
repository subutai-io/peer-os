package io.subutai.health;


import javax.ws.rs.core.Response;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.bundle.core.BundleState;
import org.apache.karaf.bundle.core.BundleStateService;


public class HealthRestServiceImpl implements HealthRestService, HealthService
{

    private static final Logger LOG = LoggerFactory.getLogger( HealthRestServiceImpl.class );


    @Override
    public Response isReady()
    {
        State state = getState();

        return state == State.FAILED ? Response.serverError().build() : state == State.READY ? Response.ok().build() :
                                                                        Response.status(
                                                                                Response.Status.SERVICE_UNAVAILABLE )
                                                                                .build();
    }


    @Override
    public State getState()
    {
        boolean failed = false;

        boolean ready = true;

        BundleContext ctx = getBundleContext();

        BundleStateService bundleStateService = getBundleStateService();

        Bundle[] bundles = ctx.getBundles();

        if ( bundles.length < BUNDLE_COUNT )
        {
            LOG.warn( "Bundle count is {}", bundles.length );

            return State.LOADING;
        }

        for ( Bundle bundle : bundles )
        {
            BundleState bundleState = bundleStateService.getState( bundle );

            if ( bundleState == BundleState.Failure )
            {
                failed = true;

                break;
            }

            int bundleIntState = bundle.getState();
            String bundleDiag = bundleStateService.getDiag( bundle );
            if ( bundleState == BundleState.Waiting && bundleIntState == Bundle.ACTIVE && bundleDiag != null
                    && bundleDiag.contains( "Missing" ) )
            {
                failed = true;

                break;
            }

            if ( !( ( bundleIntState == Bundle.ACTIVE ) || ( bundleIntState == Bundle.RESOLVED ) ) )
            {
                ready = false;

                break;
            }
        }

        return failed ? State.FAILED : ready ? State.READY : State.LOADING;
    }


    protected BundleContext getBundleContext()
    {
        return FrameworkUtil.getBundle( HealthRestServiceImpl.class ).getBundleContext();
    }


    protected BundleStateService getBundleStateService()
    {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle( BundleStateService.class ).getBundleContext();
        if ( ctx != null )
        {
            ServiceReference serviceReference = ctx.getServiceReference( BundleStateService.class.getName() );
            if ( serviceReference != null )
            {
                Object service = ctx.getService( serviceReference );
                if ( BundleStateService.class.isInstance( service ) )
                {
                    return BundleStateService.class.cast( service );
                }
            }
        }

        throw new IllegalStateException( "Can not obtain handle of BundleStateService" );
    }
}
