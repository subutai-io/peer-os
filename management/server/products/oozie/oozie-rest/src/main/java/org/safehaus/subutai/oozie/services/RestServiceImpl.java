package org.safehaus.subutai.oozie.services;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.oozie.Oozie;
import org.safehaus.subutai.api.oozie.OozieConfig;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Oozie oozieManager;

    public Oozie getOozieManager() {
        return oozieManager;
    }

    public void setOozieManager(Oozie oozieManager) {
        this.oozieManager = oozieManager;
    }

    @Override
    public String installCluster(String clusterName, String domainInfo, String serverHostname, String... clientsHostnames) {
        OozieConfig config = new OozieConfig();
        config.setClusterName( clusterName );
        config.setDomainInfo( domainInfo );
        config.setServer( serverHostname );
        Set<String> clients = new HashSet<String>();
        for(String ch : clientsHostnames) {
            clients.add( ch );
        }
        config.setClients( clients );

        UUID uuid = this.oozieManager.installCluster(config);
        return uuid.toString();
    }

    @Override
    public String uninstallCluster(String clusterName) {
        return null;
    }
}