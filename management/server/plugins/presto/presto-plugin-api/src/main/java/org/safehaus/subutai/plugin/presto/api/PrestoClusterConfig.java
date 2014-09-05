/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.presto.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


/**
 * @author dilshat
 */
public class PrestoClusterConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Presto";
    private String clusterName = "";

    private Agent coordinatorNode;
    private Set<Agent> workers;


    public Agent getCoordinatorNode() {
        return coordinatorNode;
    }


    public void setCoordinatorNode( Agent coordinatorNode ) {
        this.coordinatorNode = coordinatorNode;
    }


    public String getClusterName() {
        return clusterName;
    }


    public void setClusterName( String clusterName ) {
        this.clusterName = clusterName;
    }


    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }


    public Set<Agent> getWorkers() {
        return workers;
    }


    public void setWorkers( Set<Agent> workers ) {
        this.workers = workers;
    }


    public Set<Agent> getAllNodes() {
        Set<Agent> allNodes = new HashSet<Agent>();
        if ( workers != null ) {
            allNodes.addAll( workers );
        }
        if ( coordinatorNode != null ) {
            allNodes.add( coordinatorNode );
        }

        return allNodes;
    }


    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", coordinatorNode=" + coordinatorNode + ", workers="
                + workers + '}';
    }
}
