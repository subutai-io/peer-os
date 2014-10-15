package org.safehaus.subutai.plugin.jetty.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<JettyImpl>
{
    private ProductOperation po;

    public UninstallOperationHandler(final JettyImpl manager, final String clusterName) {
        super( manager, clusterName );
        po = manager.getTracker().createProductOperation( JettyConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }

    @Override
    public void run()
    {

    }
}
