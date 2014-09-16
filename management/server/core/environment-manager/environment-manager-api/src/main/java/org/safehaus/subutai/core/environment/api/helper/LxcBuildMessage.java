package org.safehaus.subutai.core.environment.api.helper;


import java.util.UUID;


/**
 * Created by bahadyr on 9/12/14.
 */
public class LxcBuildMessage {

    private int numberOfContainers;
    private String templateName;
    private String strategyName;
    private UUID environmentId;


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


    public String getStrategyName() {
        return strategyName;
    }


    public void setStrategyName( final String strategyName ) {
        this.strategyName = strategyName;
    }


    public UUID getEnvironmentId() {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId ) {
        this.environmentId = environmentId;
    }
}
