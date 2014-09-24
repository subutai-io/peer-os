/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.common.util;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.common.base.Preconditions;


/**
 * Service Locator allows to locate osgi services by interface and caches them locally
 */
public class ServiceLocator
{

    private final Map<String, Object> cache;


    public ServiceLocator()
    {
        this.cache = new ConcurrentHashMap<>();
    }


    public static <T> T getServiceNoCache( Class<T> clazz ) throws NamingException
    {
        Preconditions.checkNotNull( clazz, "Class is null" );

        String serviceName = clazz.getName();
        InitialContext ctx = new InitialContext();
        String jndiName = "osgi:service/" + serviceName;
        return clazz.cast( ctx.lookup( jndiName ) );
    }


    /**
     * @param clazz - Service Interface class to look up for
     *
     * @return - service reference
     *
     * @throws NamingException - thrown if service is not found
     */
    public <T> T getService( Class<T> clazz ) throws NamingException
    {
        Preconditions.checkNotNull( clazz, "Class is null" );

        String serviceName = clazz.getName();
        Object cachedObj = cache.get( serviceName );
        if ( cachedObj == null )
        {
            InitialContext ctx = new InitialContext();
            String jndiName = "osgi:service/" + serviceName;
            cachedObj = ctx.lookup( jndiName );
            cache.put( serviceName, cachedObj );
        }

        return clazz.cast( cachedObj );
    }
}
