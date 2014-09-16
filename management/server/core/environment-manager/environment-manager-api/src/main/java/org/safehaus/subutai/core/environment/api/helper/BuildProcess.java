package org.safehaus.subutai.core.environment.api.helper;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by bahadyr on 9/14/14.
 */
public class BuildProcess {

    String environmentName;
    private UUID uuid;
    private boolean completeStatus;
    private int timestamp;
    private List<BuildBlock> buildBlocks;


    public BuildProcess() {
        this.uuid = UUID.randomUUID();
        this.buildBlocks = new ArrayList<BuildBlock>();
    }


    public UUID getUuid() {
        return uuid;
    }


    public void setUuid( final UUID uuid ) {
        this.uuid = uuid;
    }


    public int getTimestamp() {
        return timestamp;
    }


    public void setTimestamp( final int timestamp ) {
        this.timestamp = timestamp;
    }


    public boolean isCompleteStatus() {
        return completeStatus;
    }


    public void setCompleteStatus( final boolean completeStatus ) {
        this.completeStatus = completeStatus;
    }


    public List<BuildBlock> getBuildBlocks() {
        return buildBlocks;
    }


    public void setBuildBlocks( final List<BuildBlock> buildBlocks ) {
        this.buildBlocks = buildBlocks;
    }


    public void addBuildBlock( final BuildBlock buildBlock ) {
        this.buildBlocks.add( buildBlock );
    }


    public String getEnvironmentName() {
        return environmentName;
    }


    public void setEnvironmentName( final String environmentName ) {
        this.environmentName = environmentName;
    }
}
