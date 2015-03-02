package org.safehaus.subutai.common.security.utils;


import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 2/28/15.
 */
public class RestartCoreServlet implements Runnable
{
    private final String PAX_WEB_JETTY_BUNDLE_NAME = "org.ops4j.pax.web.pax-web-jetty";
    private static final Logger LOGGER = LoggerFactory.getLogger( RestartCoreServlet.class );


    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's <code>run</code> method to be called in that separately executing thread. <p> The general
     * contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run()
    {
        LOGGER.error( "########################    Restarting servlet." );
        try
        {
            Thread.sleep( 5 * 1000 );
        }
        catch ( InterruptedException e )
        {
            LOGGER.error( "Interruption error while restarting thread", e );
            return;
        }
        //        getBundleContext().getBundles()
        BundleContext ctx = FrameworkUtil.getBundle( RestartCoreServlet.class ).getBundleContext();
        Bundle[] bundles = ctx.getBundles();
        for ( Bundle bundle : bundles )
        {
            if ( PAX_WEB_JETTY_BUNDLE_NAME.equals( bundle.getSymbolicName() ) )
            {
                try
                {
                    //                    bundle.stop();
                    //                    bundle.start();
                    bundle.update();
                }
                catch ( BundleException e )
                {
                    LOGGER.error( "Error restarting jetty servlet.", e );
                }
                break;
            }
        }
    }
}
