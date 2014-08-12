package org.safehaus.subutai.hbase.services;


import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.api.hbase.HBaseConfig;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private HBase hbaseManager;


    public HBase getHbaseManager() {
        return hbaseManager;
    }


    public void setHbaseManager( HBase hbaseManager ) {
        this.hbaseManager = hbaseManager;
    }


    @Override
    public String installCluster( String clusterName ) {
        HBaseConfig config = new HBaseConfig();
        this.hbaseManager.installCluster( config );
        return null;
    }


    @Override
    public String uninstallCluster( String clusterName ) {
        return null;
    }
}