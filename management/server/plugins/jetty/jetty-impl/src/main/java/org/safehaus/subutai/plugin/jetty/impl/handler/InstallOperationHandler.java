package org.safehaus.subutai.plugin.jetty.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;


public class InstallOperationHandler extends AbstractOperationHandler<JettyImpl>
{
    private JettyConfig config;
    private ProductOperation po;


    public InstallOperationHandler( final JettyImpl manager, final JettyConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( JettyConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {

    }
}
