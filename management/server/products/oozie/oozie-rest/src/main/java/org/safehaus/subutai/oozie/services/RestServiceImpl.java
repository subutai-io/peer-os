package org.safehaus.subutai.oozie.services;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.oozie.Oozie;
import org.safehaus.subutai.api.oozie.OozieConfig;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Oozie oozieManager;
    private Hadoop hadoopManager;


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager ) {
        this.hadoopManager = hadoopManager;
    }


    public Oozie getOozieManager() {
        return oozieManager;
    }


    public void setOozieManager( Oozie oozieManager ) {
        this.oozieManager = oozieManager;
    }


    @Override
    public String installCluster( String clusterName, String serverHostname,
                                  String hadoopClusterName ) {
        OozieConfig config = new OozieConfig();
        config.setClusterName( clusterName );
        config.setServer( serverHostname );
        Set<String> clients = new HashSet<String>();
        Config hadoopConfig = hadoopManager.getCluster( hadoopClusterName );
        for ( Agent agent : hadoopConfig.getAllNodes() ) {
            clients.add( agent.getHostname() );
        }
        clients.remove( serverHostname );
        config.setClients( clients );

        UUID uuid = this.oozieManager.installCluster( config );
        return uuid.toString();
    }


    @Override
    public String uninstallCluster( String clusterName ) {
        return null;
    }
}