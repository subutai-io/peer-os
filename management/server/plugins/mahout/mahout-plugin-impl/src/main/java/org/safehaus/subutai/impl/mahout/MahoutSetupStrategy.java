package org.safehaus.subutai.impl.mahout;


import org.safehaus.subutai.api.mahout.MahoutConfig;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.ConfigBase;


/**
 * Created by bahadyr on 8/26/14.
 */
public class MahoutSetupStrategy implements ClusterSetupStrategy {

    private Environment environment;
    private MahoutConfig config;
    private ProductOperation productOperation;
    private MahoutImpl mahout;

    public MahoutSetupStrategy( final Environment environment, final MahoutConfig config, final ProductOperation po,
                                final MahoutImpl mahout ) {
        this.environment = environment;
        this.config = config;
        this.productOperation = po;
        this.mahout = mahout;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {
        return config;
    }
}
