package org.safehaus.subutai.plugin.presto.impl.handler;

import java.util.UUID;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;

public class InstallOperationHandler extends AbstractOperationHandler<PrestoImpl> {

    private final ProductOperation po;
    private final PrestoClusterConfig config;
    private HadoopClusterConfig hadoopConfig;

    public InstallOperationHandler(PrestoImpl manager, PrestoClusterConfig config) {

        super(manager, config.getClusterName());
        this.config = config;
        po = PrestoImpl.getTracker().createProductOperation(PrestoClusterConfig.PRODUCT_KEY,
                String.format("Installing %s", PrestoClusterConfig.PRODUCT_KEY));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    public void setHadoopConfig(HadoopClusterConfig hadoopConfig) {
        this.hadoopConfig = hadoopConfig;
    }

    @Override
    public void run() {

        Environment env = null;
        if(config.getSetupType() == SetupType.WITH_HADOOP) {

            if(hadoopConfig == null) {
                po.addLogFailed("No Hadoop configuration specified");
                return;
            }

            po.addLog("Preparing environment...");
            hadoopConfig.setTemplateName(PrestoClusterConfig.TEMAPLTE_NAME);
            try {
                EnvironmentBlueprint eb = PrestoImpl.getHadoopManager()
                        .getDefaultEnvironmentBlueprint(hadoopConfig);
                env = PrestoImpl.getEnvironmentManager().buildEnvironmentAndReturn(eb);
            } catch(ClusterSetupException ex) {
                po.addLogFailed("Failed to prepare environment: " + ex.getMessage());
                return;
            } catch(EnvironmentBuildException ex) {
                po.addLogFailed("Failed to build environment: " + ex.getMessage());
                return;
            }
            po.addLog("Environment preparation completed");
        }

        ClusterSetupStrategy s = manager.getClusterSetupStrategy(po, config, env);
        try {
            if(s == null) throw new ClusterSetupException("No setup strategy");
            s.setup();
        } catch(ClusterSetupException ex) {
            po.addLogFailed("Failed to setup cluster: " + ex.getMessage());
        }

    }

    /**
     * Sets up a Presto cluster ovre Hadoop
     */
    private void setupWithHadoop() {

        try {
            ClusterSetupStrategy prestoClusterStrategy = manager.getClusterSetupStrategy(po, config);
            prestoClusterStrategy.setup();

            po.addLogDone(String.format("Cluster %s set up successfully", clusterName));
        } catch(ClusterSetupException e) {
            po.addLogFailed(String.format("Failed to setup Presto cluster %s : %s", clusterName, e.getMessage()));
        }
    }
}
