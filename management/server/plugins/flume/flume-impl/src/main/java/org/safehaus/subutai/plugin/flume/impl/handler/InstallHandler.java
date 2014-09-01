package org.safehaus.subutai.plugin.flume.impl.handler;

import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.impl.*;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.*;

public class InstallHandler extends AbstractOperationHandler<FlumeImpl> {

    private final FlumeConfig config;
    private HadoopClusterConfig hadoopConfig;

    public InstallHandler(FlumeImpl manager, FlumeConfig config) {
        super(manager, config.getClusterName());
        this.config = config;
        this.productOperation = manager.getTracker().createProductOperation(
                FlumeConfig.PRODUCT_KEY, "Install Flume instances: " + config.getClusterName());
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

        if(config.getSetupType() == SetupType.WITH_HADOOP) {

            if(hadoopConfig == null) {
                po.addLogFailed("No Hadoop configuration specified");
                return;
            }

            po.addLog("Preparing environment...");
            hadoopConfig.setTemplateName(FlumeConfig.TEMPLATE_NAME);
            try {
                EnvironmentBlueprint eb = manager.getHadoopManager()
                        .getDefaultEnvironmentBlueprint(hadoopConfig);
                env = manager.getEnvironmentManager().buildEnvironmentAndReturn(eb);
            } catch(ClusterSetupException ex) {
                po.addLogFailed("Failed to prepare environment: " + ex.getMessage());
                return;
            } catch(EnvironmentBuildException ex) {
                po.addLogFailed("Failed to build environment: " + ex.getMessage());
                return;
            }
            po.addLog("Environment preparation completed");
        }

        ClusterSetupStrategy s = manager.getClusterSetupStrategy(env, config, po);
        try {
            if(s == null) throw new ClusterSetupException("No setup strategy");

            s.setup();
            po.addLogDone("Done");
        } catch(ClusterSetupException ex) {
            po.addLogFailed("Failed to setup cluster: " + ex.getMessage());
        }
    }

}
