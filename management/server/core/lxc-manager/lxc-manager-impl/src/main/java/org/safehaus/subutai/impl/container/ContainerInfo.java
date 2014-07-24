package org.safehaus.subutai.impl.container;


import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by dilshat on 7/23/14.
 */
public class ContainerInfo {

    private final Agent physicalAgent;
    private final String lxcHostname;
    private boolean result;


    public ContainerInfo( final Agent physicalAgent, final String lxcHostname ) {
        this.physicalAgent = physicalAgent;
        this.lxcHostname = lxcHostname;
    }


    public void setResult( final boolean result ) {
        this.result = result;
    }


    public Agent getPhysicalAgent() {
        return physicalAgent;
    }


    public String getLxcHostname() {
        return lxcHostname;
    }


    public boolean isResult() {
        return result;
    }
}
