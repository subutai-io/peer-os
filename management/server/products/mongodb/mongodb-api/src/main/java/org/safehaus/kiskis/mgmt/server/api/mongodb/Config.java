/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.api.mongodb;

import java.util.List;

/**
 *
 * @author dilshat
 */
public class Config {

    private String clusterName;
    private String replicaSetName;
    private String domain;
    private String dbPath;
    private String configFile;
    private String logFile;
    private Integer configServerPort;
    private Integer routerPort;
    private Integer dataNodePort;

    private List<String> dataNodes;
    private List<String> configServers;
    private List<String> routers;

    public List<String> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(List<String> dataNodes) {
        this.dataNodes = dataNodes;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<String> getConfigServers() {
        return configServers;
    }

    public void setConfigServers(List<String> configServers) {
        this.configServers = configServers;
    }

    public List<String> getRouters() {
        return routers;
    }

    public void setRouters(List<String> routers) {
        this.routers = routers;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public Integer getConfigServerPort() {
        return configServerPort;
    }

    public void setConfigServerPort(Integer configServerPort) {
        this.configServerPort = configServerPort;
    }

    public Integer getRouterPort() {
        return routerPort;
    }

    public void setRouterPort(Integer routerPort) {
        this.routerPort = routerPort;
    }

    public Integer getDataNodePort() {
        return dataNodePort;
    }

    public void setDataNodePort(Integer dataNodePort) {
        this.dataNodePort = dataNodePort;
    }

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", domain=" + domain + ", dbPath=" + dbPath + ", configFile=" + configFile + ", logFile=" + logFile + ", configServerPort=" + configServerPort + ", routerPort=" + routerPort + ", dataNodePort=" + dataNodePort + ", dataNodes=" + dataNodes + ", configServers=" + configServers + ", routers=" + routers + '}';
    }

}
