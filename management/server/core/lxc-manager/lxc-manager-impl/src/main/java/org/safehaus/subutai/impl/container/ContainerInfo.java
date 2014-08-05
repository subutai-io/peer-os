package org.safehaus.subutai.impl.container;


import java.util.Set;

import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Contains information for Completion service
 */
public class ContainerInfo {

    private final Agent physicalAgent;
    private final Set<String> lxcHostnames;
    private boolean result;


    public ContainerInfo( final Agent physicalAgent, final Set<String> lxcHostnames ) {
        this.physicalAgent = physicalAgent;
        this.lxcHostnames = lxcHostnames;
    }


    public void setResult( final boolean result ) {
        this.result = result;
    }


    public Agent getPhysicalAgent() {
        return physicalAgent;
    }


    public Set<String> getLxcHostnames() {
        return lxcHostnames;
    }


    public boolean isResult() {
        return result;
    }
}
