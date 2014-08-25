package org.safehaus.subutai.plugin.flume.impl.handler;

import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.impl.*;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.*;

public class InstallHandler extends AbstractOperationHandler<FlumeImpl> {

    private final FlumeConfig config;
    private HadoopClusterConfig hadoopConfig;

    public InstallHandler(FlumeImpl manager, FlumeConfig config) {
        super(manager, config.getClusterName());
        this.config = config;
        this.productOperation = manager.getTracker().createProductOperation(
                FlumeConfig.PRODUCT_KEY, "Install Flume cluster " + config.getClusterName());
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
        ClusterSetupStrategy s = null;
        if(config.getSetupType() == SetupType.OVER_HADOOP)
            s = manager.getClusterSetupStrategy(null, config, po);

        else if(config.getSetupType() == SetupType.WITH_HADOOP) {

            if(hadoopConfig == null) {
                po.addLogFailed("No Hadoop configuration specified");
                return;
            }

            // TODO: composite template name for Hadoop and Flume
            String t = String.format("%s_%s", hadoopConfig.getTemplateName(),
                    FlumeConfig.TEMPLATE_NAME);
            hadoopConfig.setTemplateName(t);
            try {
                EnvironmentBlueprint eb = manager.getHadoopManager()
                        .getDefaultEnvironmentBlueprint(hadoopConfig);
                Environment env = manager.getEnvironmentManager().buildEnvironmentAndReturn(eb);
                s = manager.getClusterSetupStrategy(env, config, po);
            } catch(ClusterSetupException ex) {
                po.addLogFailed("Failed to prepare environment: " + ex.getMessage());
                return;
            } catch(EnvironmentBuildException ex) {
                po.addLogFailed("Failed to build environment: " + ex.getMessage());
                return;
            }
        }

        try {
            if(s == null) throw new ClusterSetupException("No setup strategy");

            s.setup();
            po.addLogDone("Done");
        } catch(ClusterSetupException ex) {
            po.addLogFailed("Failed to setup cluster: " + ex.getMessage());
        }
    }

}
