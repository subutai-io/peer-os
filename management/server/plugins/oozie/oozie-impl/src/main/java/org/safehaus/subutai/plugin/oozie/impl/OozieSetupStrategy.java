package org.safehaus.subutai.plugin.oozie.impl;


import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;


/**
 * Created by bahadyr on 8/25/14.
 */
abstract class OozieSetupStrategy implements ClusterSetupStrategy
{


    //    private Environment environment;
    final OozieClusterConfig config;
    final OozieImpl oozieManager;
    final TrackerOperation po;


    public OozieSetupStrategy( OozieImpl oozie, TrackerOperation po, OozieClusterConfig config )
    {

        //        this.environment = environment;
        this.oozieManager = oozie;
        this.po = po;
        this.config = config;
    }
}
