/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.accumulo;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


/**
 * @author dilshat
 */
public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Accumulo";
    private String clusterName = "";
    private String instanceName = "";
    private String password = "";
    private String confirmPassword = "";
    private Agent masterNode;
    private Agent gcNode;
    private Agent monitor;
    private Set<Agent> tracers;
    private Set<Agent> slaves;


    public Set<Agent> getAllNodes() {
        Set<Agent> allNodes = new HashSet<>();

        allNodes.add( masterNode );
        allNodes.add( gcNode );
        allNodes.add( monitor );
        allNodes.addAll( tracers );
        allNodes.addAll( slaves );

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
        return PRODUCT_KEY;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword( String confirmPassword ) {
        this.confirmPassword = confirmPassword;
    }

    public void reset() {
        clusterName = "";
        instanceName = "";
        password = "";
        confirmPassword = "";
        masterNode = null;
        gcNode = null;
        monitor = null;
        tracers = null;
        slaves = null;
    }


    @Override
    public String toString() {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", instanceName='" + instanceName + '\'' +
                ", password='" + password + '\'' +
                ", confirmPassword='" + confirmPassword + '\'' +
                ", masterNode=" + masterNode +
                ", gcNode=" + gcNode +
                ", monitor=" + monitor +
                ", tracers=" + tracers +
                ", slaves=" + slaves +
                '}';
    }
}
