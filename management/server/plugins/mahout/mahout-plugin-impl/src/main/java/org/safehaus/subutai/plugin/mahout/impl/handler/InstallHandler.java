package org.safehaus.subutai.plugin.mahout.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;
import org.safehaus.subutai.plugin.mahout.api.SetupType;
import org.safehaus.subutai.plugin.mahout.impl.MahoutImpl;


/**
 * Created by dilshat on 5/6/14.
 */
public class InstallHandler extends AbstractOperationHandler<MahoutImpl> {
    private final ProductOperation po;
    private final MahoutClusterConfig config;
    private HadoopClusterConfig hadoopClusterConfig;


    public InstallHandler( MahoutImpl manager, MahoutClusterConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( MahoutClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", MahoutClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {

        if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
            setupOverHadoop();
        }
        else {
            setupWithHadoop();
        }
    }


    private void setupOverHadoop() {
        try {
            //setup up Accumulo cluster
            ClusterSetupStrategy setupStrategy = manager.getClusterSetupStrategy( null, config, productOperation );
            setupStrategy.setup();

            productOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e ) {
            productOperation
                    .addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    private void setupWithHadoop() {
        try {
            final String COMBO_TEMPLATE_NAME = "hadoopnzknaccumulo";
            hadoopClusterConfig.setTemplateName( COMBO_TEMPLATE_NAME );
            //create environment
            Environment env = manager.getEnvironmentManager().buildEnvironmentAndReturn(
                    manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopClusterConfig ) );

            //setup Hadoop cluster
            ClusterSetupStrategy hadoopClusterSetupStrategy =
                    manager.getHadoopManager().getClusterSetupStrategy( productOperation, hadoopClusterConfig, env );
            hadoopClusterSetupStrategy.setup();


            //setup up Mahout cluster
            ClusterSetupStrategy ss =
                    manager.getClusterSetupStrategy( env, config, productOperation );
            ss.setup();

            productOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            productOperation
                    .addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
