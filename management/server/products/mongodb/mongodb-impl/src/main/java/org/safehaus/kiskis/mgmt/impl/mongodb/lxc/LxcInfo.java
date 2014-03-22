/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mongodb.lxc;

import org.safehaus.kiskis.mgmt.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class LxcInfo {

    private final Agent physicalAgent;
    private final String lxcHostname;
    private NodeType nodeType;
    private boolean result;

    public LxcInfo(Agent physicalAgent, String lxcHostname, NodeType nodeType) {
        this.physicalAgent = physicalAgent;
        this.lxcHostname = lxcHostname;
        this.nodeType = nodeType;
    }

    public LxcInfo(Agent physicalAgent, String lxcHostname) {
        this.physicalAgent = physicalAgent;
        this.lxcHostname = lxcHostname;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Agent getPhysicalAgent() {
        return physicalAgent;
    }

    public String getLxcHostname() {
        return lxcHostname;
    }

    public NodeType getNodeType() {
        return nodeType;
    }
}
