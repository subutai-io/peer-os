/*
 *  @author : Slim Ouertani
 *  @mail : ouertani@gmail.com
 */
package io.subutai.core.test.dp;


import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


public class LoggerProxy implements InvocationHandler, Serializable
{

    private ServiceReference serviceReference;
    private BundleContext bundleContext;


    public LoggerProxy( BundleContext bundleContext, ServiceReference serviceReference )
    {
        this.serviceReference = serviceReference;
        this.bundleContext = bundleContext;
    }


    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        System.out.println( "-->Methode : [" + method.getName() + "] " );
        System.out.println( "-->Parameters : " );
        for ( Object object : args )
        {
            System.out.println( "->" + object + " : " );
        }
        System.out.println( "" );
        Object invoke = method.invoke( bundleContext.getService( serviceReference ), args );

        System.out.println( "-->Return : " + invoke );
        return invoke;
    }
}
