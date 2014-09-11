package org.safehaus.subutai.plugin.presto.impl.handler;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;

import java.util.HashSet;
import java.util.Set;

public class InstallOperationHandler extends AbstractOperationHandler<PrestoImpl> {

    private final PrestoClusterConfig config;
    private HadoopClusterConfig hadoopConfig;

    public InstallOperationHandler(PrestoImpl manager, PrestoClusterConfig config) {

        super(manager, config.getClusterName());
        this.config = config;
        productOperation = manager.getTracker().createProductOperation(
                PrestoClusterConfig.PRODUCT_KEY,
                String.format("Installing %s", PrestoClusterConfig.PRODUCT_KEY));
    }

    public void setHadoopConfig(HadoopClusterConfig hadoopConfig) {
        this.hadoopConfig = hadoopConfig;
    }

    @Override
    public void run() {

        ProductOperation po = productOperation;
        Environment env = null;
        if (config.getSetupType() == SetupType.WITH_HADOOP) {

            if (hadoopConfig == null) {
                po.addLogFailed("No Hadoop configuration specified");
                return;
            }

            po.addLog("Preparing environment...");
            hadoopConfig.setTemplateName(PrestoClusterConfig.TEMAPLTE_NAME);
            try {
                EnvironmentBuildTask eb = manager.getHadoopManager()
                        .getDefaultEnvironmentBlueprint(hadoopConfig);
                env = manager.getEnvironmentManager().buildEnvironmentAndReturn(eb);
            } catch (ClusterSetupException ex) {
                po.addLogFailed("Failed to prepare environment: " + ex.getMessage());
                destroyNodes(env);
                return;
            } catch (EnvironmentBuildException ex) {
                po.addLogFailed("Failed to build environment: " + ex.getMessage());
                destroyNodes(env);
                return;
            }
            po.addLog("Environment preparation completed");
        }

        ClusterSetupStrategy s = manager.getClusterSetupStrategy(po, config, env);
        try {
            if (s == null) throw new ClusterSetupException("No setup strategy");

            po.addLog("Installing cluster...");
            s.setup();
            po.addLogDone("Installing cluster completed");

        } catch (ClusterSetupException ex) {
            po.addLogFailed("Failed to setup cluster: " + ex.getMessage());
            destroyNodes(env);
        }

    }

    void destroyNodes(Environment env) {

        if (env == null || env.getNodes().isEmpty()) return;

        Set<Agent> set = new HashSet<>(env.getNodes().size());
        for (Node n : env.getNodes()) set.add(n.getAgent());

        productOperation.addLog("Destroying node(s)...");
        try {
            manager.getContainerManager().clonesDestroy(set);
            productOperation.addLog("Destroying node(s) completed");
        } catch (LxcDestroyException ex) {
            productOperation.addLog("Failed to destroy node(s): " + ex.getMessage());
        }
    }

}
