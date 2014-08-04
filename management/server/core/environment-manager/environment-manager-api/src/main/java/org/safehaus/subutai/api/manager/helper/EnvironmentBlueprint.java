package org.safehaus.subutai.api.manager.helper;


import java.util.Set;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBlueprint {


    private String name;
    Set<NodeGroup> nodeGroups;
    private boolean linkHosts;
    private boolean exchangeSshKeys;


    public boolean isLinkHosts() {
        return linkHosts;
    }


    public void setLinkHosts( final boolean linkHosts ) {
        this.linkHosts = linkHosts;
    }


    public boolean isExchangeSshKeys() {
        return exchangeSshKeys;
    }


    public void setExchangeSshKeys( final boolean exchangeSshKeys ) {
        this.exchangeSshKeys = exchangeSshKeys;
    }


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }


    public Set<NodeGroup> getNodeGroups() {
        return nodeGroups;
    }


    public void setNodeGroups( final Set<NodeGroup> nodeGroups ) {
        this.nodeGroups = nodeGroups;
    }
}
