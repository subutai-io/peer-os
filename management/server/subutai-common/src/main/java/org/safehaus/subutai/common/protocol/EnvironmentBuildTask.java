package org.safehaus.subutai.common.protocol;


import java.util.Set;
import java.util.UUID;


/**
 * Created by bahadyr on 9/9/14.
 */
public class EnvironmentBuildTask {

    private UUID uuid;
    private String peerUuid;
    private EnvironmentBlueprint environmentBlueprint;
    private Set<String> physicalNodes;


    public EnvironmentBuildTask() {
        this.uuid = UUID.randomUUID();
    }


    public String getPeerUuid() {
        return peerUuid;
    }


    public void setPeerUuid( final String peerUuid ) {
        this.peerUuid = peerUuid;
    }


    public UUID getUuid() {
        return uuid;
    }


    public void setUuid( final UUID uuid ) {
        this.uuid = uuid;
    }


    public EnvironmentBlueprint getEnvironmentBlueprint() {
        return environmentBlueprint;
    }


    public void setEnvironmentBlueprint( final EnvironmentBlueprint environmentBlueprint ) {
        this.environmentBlueprint = environmentBlueprint;
    }


    public Set<String> getPhysicalNodes() {
        return physicalNodes;
    }


    public void setPhysicalNodes( final Set<String> physicalNodes ) {
        this.physicalNodes = physicalNodes;
    }
}
