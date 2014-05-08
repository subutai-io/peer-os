package org.safehaus.kiskis.mgmt.cassandra.services;

import org.safehaus.kiskis.mgmt.api.cassandra.Cassandra;
import org.safehaus.kiskis.mgmt.api.cassandra.Config;

import java.util.List;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {
    //Just like Spring.  Please add Getters/Setters. Blueprint annotations are still work in progress
//    private HelloService helloService;
    private Cassandra cassandraManager;

    public void setCassandraManager(Cassandra cassandraManager) {
        this.cassandraManager = cassandraManager;
    }

    public Cassandra getCassandraManager() {
        return cassandraManager;
    }

    public String handleGet(String name) {
//        return helloService.sayHello(name);
        List<Config> list = cassandraManager.getClusters();
            StringBuilder sb = new StringBuilder();
        if (list.size() > 0) {
            for (Config config : list) {
                sb.append(config.getClusterName()).append("\n");
            }
        };
        return sb.toString();
    }

    /*
        Constructor
     */
    public RestServiceImpl() {
    }

    /*
        Getters and Setters
     */
//    public HelloService getHelloService() {
//        return helloService;
//    }
//
//    public void setHelloService(HelloService helloService) {
//        this.helloService = helloService;
//    }
}