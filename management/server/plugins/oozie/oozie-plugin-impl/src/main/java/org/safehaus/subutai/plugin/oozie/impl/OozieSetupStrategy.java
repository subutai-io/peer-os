package org.safehaus.subutai.plugin.oozie.impl;


import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;


/**
 * Created by bahadyr on 8/25/14.
 */
abstract class OozieSetupStrategy implements ClusterSetupStrategy {


    //    private Environment environment;
    final OozieClusterConfig config;
    final OozieImpl oozieManager;
    final ProductOperation po;


    public OozieSetupStrategy( OozieImpl oozie, ProductOperation po, OozieClusterConfig config ) {

        //        this.environment = environment;
        this.oozieManager = oozie;
        this.po = po;
        this.config = config;
    }
}
