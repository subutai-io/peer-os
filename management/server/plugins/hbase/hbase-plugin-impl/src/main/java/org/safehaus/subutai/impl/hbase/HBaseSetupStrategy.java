package org.safehaus.subutai.impl.hbase;


import org.safehaus.subutai.api.hbase.HBaseConfig;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.ConfigBase;


/**
 * Created by bahadyr on 8/25/14.
 */
public class HBaseSetupStrategy implements ClusterSetupStrategy {

    private Environment environment;
    private HBaseConfig config;
    private ProductOperation productOperation;
    private HBaseImpl hbase;

    public HBaseSetupStrategy( final Environment environment, final HBaseConfig config, final ProductOperation po,
                               final HBaseImpl hBase ) {
        this.environment = environment;
        this.config = config;
        this.productOperation = po;
        this.hbase = hBase;

    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {
        return null;
    }
}
