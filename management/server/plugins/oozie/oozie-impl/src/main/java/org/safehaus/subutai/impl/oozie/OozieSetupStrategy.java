package org.safehaus.subutai.impl.oozie;


import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.oozie.OozieConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.ConfigBase;


/**
 * Created by bahadyr on 8/25/14.
 */
public class OozieSetupStrategy implements ClusterSetupStrategy {


    private Environment environment;
    private OozieConfig config;
    private OozieImpl oozieManager;
    private ProductOperation productOperation;


    public OozieSetupStrategy( final Environment environment, final OozieConfig config, final ProductOperation po,
                               final OozieImpl oozie ) {

        this.environment = environment;
        this.config = config;
        this.productOperation = po;
        this.oozieManager = oozie;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {
        return null;
    }
}
