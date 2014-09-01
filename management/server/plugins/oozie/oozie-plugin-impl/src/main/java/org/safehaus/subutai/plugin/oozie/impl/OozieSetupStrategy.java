package org.safehaus.subutai.plugin.oozie.impl;


import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.plugin.oozie.api.OozieConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import com.google.common.base.Strings;


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

        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getDomainName() ) ||
                Strings.isNullOrEmpty( config.getProductName() ) ||
                Strings.isNullOrEmpty( config.getTemplateName() ) ) {
            throw new ClusterSetupException( "Malformed cluster configuration" );
        }

        try {
            new ClusterConfiguration( productOperation, oozieManager ).configureCluster( config );
        } catch (ClusterConfigurationException e) {
            throw new ClusterSetupException( e.getMessage() );
        }
        return config;
    }
}
