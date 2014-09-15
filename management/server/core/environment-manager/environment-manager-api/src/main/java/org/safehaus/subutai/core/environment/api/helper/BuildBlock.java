package org.safehaus.subutai.core.environment.api.helper;


/**
 * Created by bahadyr on 9/14/14.
 */
public class BuildBlock {

    int numberOfContainers;
    String templateName;
    String strategy;
    String environmentUuid;
    boolean completeState;
    int timestamp;
    String targetPeerId;


    public int getNumberOfContainers() {
        return numberOfContainers;
    }


    public void setNumberOfContainers( final int numberOfContainers ) {
        this.numberOfContainers = numberOfContainers;
    }


    public String getTemplateName() {
        return templateName;
    }


    public void setTemplateName( final String templateName ) {
        this.templateName = templateName;
    }


    public String getStrategy() {
        return strategy;
    }


    public void setStrategy( final String strategy ) {
        this.strategy = strategy;
    }


    public String getEnvironmentUuid() {
        return environmentUuid;
    }


    public void setEnvironmentUuid( final String environmentUuid ) {
        this.environmentUuid = environmentUuid;
    }


    public boolean isCompleteState() {
        return completeState;
    }


    public void setCompleteState( final boolean completeState ) {
        this.completeState = completeState;
    }


    public int getTimestamp() {
        return timestamp;
    }


    public void setTimestamp( final int timestamp ) {
        this.timestamp = timestamp;
    }


    public String getTargetPeerId() {
        return targetPeerId;
    }


    public void setTargetPeerId( final String targetPeerId ) {
        this.targetPeerId = targetPeerId;
    }
}
