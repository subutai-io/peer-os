/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.common.util;


import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.google.common.base.Preconditions;


/**
 * Service Locator allows to locate OSGi services by interface
 */
public class ServiceLocator
{

    /**
     * Returns service by Interface
     *
     * @param clazz Service Interface class to look up for
     *
     * @return service reference
     */
    public static <T> T getServiceOrNull( Class<T> clazz )
    {
        Preconditions.checkNotNull( clazz, "Class is null" );

        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle( clazz ).getBundleContext();
        if ( ctx != null )
        {
            ServiceReference serviceReference = ctx.getServiceReference( clazz.getName() );
            if ( serviceReference != null )
            {
                Object service = ctx.getService( serviceReference );
                if ( clazz.isInstance( service ) )
                {
                    return clazz.cast( service );
                }
            }
        }

        return null;
    }


    public static <T> T lookup( Class<T> clazz )
    {
        T service = getServiceOrNull( clazz );

        if ( service == null )
        {
            throw new IllegalStateException( String.format( "Failed to lookup %s service", clazz.getSimpleName() ) );
        }

        return service;
    }


    /**
     * Returns service by Interface
     *
     * @param clazz - Service Interface class to look up for
     *
     * @return - service reference
     */

    public <T> T getService( Class<T> clazz )
    {
        return lookup( clazz );
    }
}
