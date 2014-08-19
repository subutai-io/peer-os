/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;


/**
 * @author dilshat
 */
public class AccumuloClusterConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Accumulo2";
    public static final int DEFAULT_ACCUMULO_MASTER_NODES_QUANTITY = 3;
    public static final String PRODUCT_NAME = "accumulo";
    private String clusterName = "";
    private String instanceName = "";
    private String password = "";
    private Agent masterNode;
    private Agent gcNode;
    private Agent monitor;
    private Set<Agent> tracers;
    private Set<Agent> slaves;
    private int numberOfTracers = 1;
    private int numberOfSlaves = 3;
    private SetupType setupType;
    private String hadoopClusterName;
    private String zookeeperClusterName;


    public String getHadoopClusterName() {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( final String hadoopClusterName ) {
        this.hadoopClusterName = hadoopClusterName;
    }


    public String getZookeeperClusterName() {
        return zookeeperClusterName;
    }


    public void setZookeeperClusterName( final String zookeeperClusterName ) {
        this.zookeeperClusterName = zookeeperClusterName;
    }


    public SetupType getSetupType() {
        return setupType;
    }


    public void setSetupType( final SetupType setupType ) {
        this.setupType = setupType;
    }


    public int getNumberOfTracers() {
        return numberOfTracers;
    }


    public void setNumberOfTracers( final int numberOfTracers ) {
        this.numberOfTracers = numberOfTracers;
    }


    public int getNumberOfSlaves() {
        return numberOfSlaves;
    }


    public void setNumberOfSlaves( final int numberOfSlaves ) {
        this.numberOfSlaves = numberOfSlaves;
    }


    public Set<Agent> getAllNodes() {
        Set<Agent> allNodes = new HashSet<>();

        if ( masterNode != null ) {
            allNodes.add( masterNode );
        }
        if ( gcNode != null ) {
            allNodes.add( gcNode );
        }
        if ( monitor != null ) {
            allNodes.add( monitor );
        }
        if ( tracers != null ) {
            allNodes.addAll( tracers );
        }
        if ( slaves != null ) {
            allNodes.addAll( slaves );
        }

        return allNodes;
    }


    public Agent getMasterNode() {
        return masterNode;
    }


    public void setMasterNode( Agent masterNode ) {
        this.masterNode = masterNode;
    }


    public Agent getGcNode() {
        return gcNode;
    }


    public void setGcNode( Agent gcNode ) {
        this.gcNode = gcNode;
    }


    public Agent getMonitor() {
        return monitor;
    }


    public void setMonitor( Agent monitor ) {
        this.monitor = monitor;
    }


    public Set<Agent> getTracers() {
        return tracers;
    }


    public void setTracers( Set<Agent> tracers ) {
        this.tracers = tracers;
    }


    public Set<Agent> getSlaves() {
        return slaves;
    }


    public void setSlaves( Set<Agent> slaves ) {
        this.slaves = slaves;
    }


    public String getClusterName() {
        return clusterName;
    }


    public void setClusterName( String clusterName ) {
        this.clusterName = clusterName;
    }


    @Override
    public String getProductName() {
        return PRODUCT_NAME;
    }


    public String getInstanceName() {
        return instanceName;
    }


    public void setInstanceName( String instanceName ) {
        this.instanceName = instanceName;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword( String password ) {
        this.password = password;
    }


    public void reset() {
        clusterName = "";
        instanceName = "";
        password = "";
        masterNode = null;
        gcNode = null;
        monitor = null;
        tracers = null;
        slaves = null;
        numberOfTracers = 1;
        numberOfSlaves = 3;
    }


    @Override
    public String toString() {
        return "AccumuloClusterConfig{" +
                "clusterName='" + clusterName + '\'' +
                ", instanceName='" + instanceName + '\'' +
                ", password='" + password + '\'' +
                ", masterNode=" + masterNode +
                ", gcNode=" + gcNode +
                ", monitor=" + monitor +
                ", tracers=" + tracers +
                ", slaves=" + slaves +
                ", numberOfTracers=" + numberOfTracers +
                ", numberOfSlaves=" + numberOfSlaves +
                ", setupType=" + setupType +
                ", hadoopClusterName='" + hadoopClusterName + '\'' +
                ", zookeeperClusterName='" + zookeeperClusterName + '\'' +
                '}';
    }
}
