/*
 *  @author : Slim Ouertani
 *  @mail : ouertani@gmail.com
 */
package io.subutai.core.test.dp;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.framework.hooks.service.ListenerHook;

import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.MODIFIED_ENDMATCH;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;


public class LoggerHooks implements FindHook, EventListenerHook
{

    //    private static final Logger logger = LoggerFactory.getLogger( LoggerHooks.class );
    private BundleContext bc;
    private static final String PROXY = "proxied";


    public LoggerHooks( final BundleContext context )
    {
        this.bc = context;
    }


    @Override
    public void find( BundleContext bc, String name, String filter, boolean allServices, Collection references )
    {
        try
        {
            if ( this.bc.equals( bc ) || bc.getBundle().getBundleId() == 0 )
            {
                return;
            }

            System.out
                    .println( " bundle : [" + bc.getBundle().getSymbolicName() + "] try to get reference  of " + name );
            Iterator iterator = references.iterator();

            while ( iterator.hasNext() )
            {
                ServiceReference sr = ( ServiceReference ) iterator.next();

                String symbolicName = sr.getBundle().getSymbolicName();
                System.out.println( "from bundle" + symbolicName );

                if ( sr.getProperty( "proxied" ) == null && symbolicName.startsWith( "io.subutai" ) )
                {
                    iterator.remove();
                }
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }


    @Override
    public void event( final ServiceEvent event,
                       final Map<BundleContext, Collection<ListenerHook.ListenerInfo>> listeners )
    {
        final ServiceReference serviceReference = event.getServiceReference();
        System.out.println( "" + serviceReference.getBundle().getSymbolicName() );
        String symbolicName = serviceReference.getBundle().getSymbolicName();
        if ( serviceReference.getProperty( PROXY ) == null && serviceReference.getBundle().getBundleContext() != bc
                && symbolicName.startsWith( "io.subutai" ) )
        {
            Bundle bundle = serviceReference.getBundle();

            switch ( event.getType() )
            {
                case REGISTERED:
                {
                    String[] propertyKeys = serviceReference.getPropertyKeys();
                    Dictionary<String, String> properties = buildProps( propertyKeys, event );
                    String[] interfaces = ( String[] ) serviceReference.getProperty( "objectClass" );

                    Class<?>[] toClass = toClass( interfaces, bundle );
                    proxyService( bundle, toClass, properties, this.getClass().getClassLoader(),
                            new LoggerProxy( bc, serviceReference ) );
                    break;
                }
                case UNREGISTERING:
                {
                    //TODO
                    break;
                }
                case MODIFIED:
                case MODIFIED_ENDMATCH:
                {
                    //TODO
                    break;
                }
            }
        }
    }


    private Dictionary<String, String> buildProps( String[] propertyKeys, ServiceEvent event )
    {
        Dictionary<String, String> props = new Hashtable<>();
        for ( String string : propertyKeys )
        {
            props.put( string, String.valueOf( event.getServiceReference().getProperty( string ) ) );
        }
        return props;
    }


    private static String[] toString( Class<?>[] interfaces )
    {
        String[] names = new String[interfaces.length];
        int i = 0;
        for ( Class clazz : interfaces )
        {
            names[i++] = clazz.getName();
        }
        return names;
    }


    private static Class<?>[] toClass( String[] interfaces, Bundle bl )
    {
        Class<?>[] names = new Class<?>[interfaces.length];
        int i = 0;
        for ( String clazz : interfaces )
        {
            try
            {
                names[i++] = bl.loadClass( clazz );
            }
            catch ( ClassNotFoundException ex )
            {
                System.out.println( "No class found" );
            }
        }
        return names;
    }


    private static ServiceRegistration proxyService( Bundle bundleSource, Class<?>[] interfaces,
                                                     Dictionary<String, String> prop, ClassLoader cl,
                                                     InvocationHandler proxy )
    {
        try
        {
            prop.put( PROXY, "true" );
            Object loggerProxy = Proxy.newProxyInstance( cl, interfaces, proxy );
            return bundleSource.getBundleContext().registerService( toString( interfaces ), loggerProxy, prop );
        }
        catch ( Exception ex )
        {
            System.out.println( "Error to proxy cl" );
            return null;
        }
    }
}
