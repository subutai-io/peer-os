/*
 *  @author : Slim Ouertani
 *  @mail : ouertani@gmail.com
 */
package io.subutai.core.test.dp;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;


/**
 * @author slim ouertani
 */
public class Activator implements BundleActivator
{

    @Override
    public void start( BundleContext context ) throws Exception
    {
        try
        {
            LoggerHooks loggerHooks = new LoggerHooks( context );
            context.registerService( new String[] { FindHook.class.getName(), EventListenerHook.class.getName() },
                    loggerHooks, null );
        }
        catch ( Exception ex )
        {
            // ignore
        }
    }


    @Override
    public void stop( BundleContext context ) throws Exception
    {
    }
}
