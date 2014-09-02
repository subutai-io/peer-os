package org.safehaus.subutai.plugin.lucene.impl.handler;


import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.lucene.api.Config;
import org.safehaus.subutai.plugin.lucene.api.SetupType;
import org.safehaus.subutai.plugin.lucene.impl.LuceneImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;


public class InstallOperationHandler extends AbstractOperationHandler<LuceneImpl>
{
    private final Config config;
    private HadoopClusterConfig hadoopConfig;

    public InstallOperationHandler( LuceneImpl manager, Config config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Installing %s", Config.PRODUCT_KEY ) );
    }

    public HadoopClusterConfig getHadoopConfig() {
        return hadoopConfig;
    }

    public void setHadoopConfig(HadoopClusterConfig hadoopConfig) {
        this.hadoopConfig = hadoopConfig;
    }


    @Override
    public void run() {
        ProductOperation po = productOperation;
        Environment env = null;

        if ( config.getSetupType() == SetupType.WITH_HADOOP ) {

            if ( hadoopConfig == null ) {
                po.addLogFailed( "No Hadoop configuration specified" );
                return;
            }

            po.addLog( "Preparing environment..." );
            hadoopConfig.setTemplateName( Config.TEMPLATE_NAME );
            try {
                EnvironmentBlueprint eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                env = manager.getEnvironmentManager().buildEnvironmentAndReturn( eb );
            }
            catch ( ClusterSetupException ex ) {
                po.addLogFailed( "Failed to prepare environment: " + ex.getMessage() );
                return;
            }
            catch ( EnvironmentBuildException ex ) {
                po.addLogFailed( "Failed to build environment: " + ex.getMessage() );
                return;
            }
            po.addLog( "Environment preparation completed" );
        }

        ClusterSetupStrategy s = manager.getClusterSetupStrategy( env, config, po );
        try {
            if ( s == null ) {
                throw new ClusterSetupException( "No setup strategy" );
            }

            s.setup();
            po.addLogDone( "Done" );
        }
        catch ( ClusterSetupException ex ) {
            po.addLogFailed( "Failed to setup cluster: " + ex.getMessage() );
        }
    }
}
