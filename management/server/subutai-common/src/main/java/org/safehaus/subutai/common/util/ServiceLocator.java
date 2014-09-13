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
public class ServiceLocator {

    private final Map<String, Object> cache;
    private final InitialContext ctx;


    public ServiceLocator() throws NamingException {
        this.ctx = new InitialContext();
        this.cache = new ConcurrentHashMap<>();
    }


    public <T> T getService( Class<T> clazz ) throws NamingException {
        Preconditions.checkNotNull( clazz, "Class is null" );

        String serviceName = clazz.getName();
        Object cachedObj = cache.get( serviceName );
        if ( cachedObj == null ) {
            String jndiName = "osgi:service/" + serviceName;
            cachedObj = ctx.lookup( jndiName );
            cache.put( serviceName, cachedObj );
        }

        return clazz.cast( cachedObj );
    }
}
