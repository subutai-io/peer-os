package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class OverHadoopSetupStrategy extends HBaseSetupStrategy {


    public OverHadoopSetupStrategy( HBaseImpl manager, ProductOperation po, HBaseConfig config ) {
        super( manager, po, config );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {


        return config;
    }
}
