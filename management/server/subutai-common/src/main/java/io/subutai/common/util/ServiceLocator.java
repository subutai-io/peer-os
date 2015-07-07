/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.common.util;


import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.google.common.base.Preconditions;


/**
 * Service Locator allows to locate OSGi services by interface and caches them locally
 */
public class ServiceLocator
{


    /**
     * Returns service by Interface, bypasses cache
     *
     * @param clazz Service Interface class to look up for
     *
     * @return service reference
     *
     * @throws NamingException thrown if service is not found
     */
    public static <T> T getServiceNoCache( Class<T> clazz ) throws NamingException
    {
        Preconditions.checkNotNull( clazz, "Class is null" );

        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle( clazz ).getBundleContext();
        if ( ctx != null )
        {
            ServiceReference serviceReference = ctx.getServiceReference( clazz.getName() );
            if ( serviceReference != null )
            {
                return clazz.cast( ctx.getService( serviceReference ) );
            }
        }

        return null;
    }


    /**
     * Returns service by Interface, bypasses cache
     *
     * @param clazz Service Interface class to look up for
     *
     * @return service reference
     *
     * @throws NamingException thrown if service is not found
     */
    public static <T> T getJNDIServiceNoCache( Class<T> clazz ) throws NamingException
    {
        Preconditions.checkNotNull( clazz, "Class is null" );

        String serviceName = clazz.getName();
        InitialContext ctx = new InitialContext();
        String jndiName = "osgi:service/" + serviceName;
        return clazz.cast( ctx.lookup( jndiName ) );
    }


    /**
     * Returns service by Interface
     *
     * @param clazz - Service Interface class to look up for
     *
     * @return - service reference
     *
     * @throws NamingException - thrown if service is not found
     */

    public <T> T getService( Class<T> clazz ) throws NamingException
    {
        return getServiceNoCache( clazz );
    }
}
