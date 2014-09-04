package org.safehaus.subutai.plugin.oozie.impl;


import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.oozie.api.OozieConfig;


/**
 * Created by bahadyr on 8/25/14.
 */
abstract class OozieSetupStrategy implements ClusterSetupStrategy {


    //    private Environment environment;
    final OozieConfig config;
    final OozieImpl oozieManager;
    final ProductOperation po;


    public OozieSetupStrategy( OozieImpl oozie, ProductOperation po, OozieConfig config ) {

        //        this.environment = environment;
        this.oozieManager = oozie;
        this.po = po;
        this.config = config;
    }


    /*@Override
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
    }*/
}
