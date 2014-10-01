package org.safehaus.subutai.plugin.oozie.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.SetupType;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;

import java.util.UUID;


public class InstallHandler extends AbstractOperationHandler<OozieImpl>
{

    private OozieClusterConfig config;
    private HadoopClusterConfig hadoopConfig;
    private final ProductOperation productOperation;


    public InstallHandler( final OozieImpl manager, final OozieClusterConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        productOperation = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }

    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }

    @Override
    public void run()
    {
        Environment env = null;

        if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {

            if ( hadoopConfig == null )
            {
                productOperation.addLogFailed( "No Hadoop configuration specified" );
                return;
            }

            productOperation.addLog( "Preparing environment..." );
            hadoopConfig.setTemplateName( OozieClusterConfig.PRODUCT_NAME_SERVER );
            try
            {
                EnvironmentBuildTask eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                env = manager.getEnvironmentManager().buildEnvironmentAndReturn( eb );
            }
            catch ( ClusterSetupException ex )
            {
                productOperation.addLogFailed( "Failed to prepare environment: " + ex.getMessage() );
                return;
            }
            catch ( EnvironmentBuildException ex )
            {
                productOperation.addLogFailed( "Failed to build environment: " + ex.getMessage() );
                return;
            }
            productOperation.addLog( "Environment preparation completed" );
        }

        ClusterSetupStrategy s = manager.getClusterSetupStrategy( env, config, productOperation );
        try
        {
            if ( s == null )
            {
                throw new ClusterSetupException( "No setup strategy" );
            }

            s.setup();
            productOperation.addLogDone( "Done" );
        }
        catch ( ClusterSetupException ex )
        {
            productOperation.addLogFailed( "Failed to setup cluster: " + ex.getMessage() );
        }
    }
}
