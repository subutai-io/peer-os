package org.safehaus.subutai.core.container.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * Contains information for Completion service
 */
public class ContainerInfo
{

    private final Agent physicalAgent;
    private final String cloneName;
    private final String templateName;
    private final UUID envId;
    private boolean ok;
    private ContainerAction containerAction;


    public ContainerInfo( final Agent physicalAgent, final UUID envId, final String templateName,
                          final String cloneName, final ContainerAction containerAction )
    {
        this.physicalAgent = physicalAgent;
        this.cloneName = cloneName;
        this.templateName = templateName;
        this.envId = envId;
        this.containerAction = containerAction;
    }


    public Agent getPhysicalAgent()
    {
        return physicalAgent;
    }


    public String getCloneName()
    {
        return cloneName;
    }


    public boolean isOk()
    {
        return ok;
    }


    public void fail()
    {
        this.ok = false;
    }


    public void success()
    {
        this.ok = true;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public UUID getEnvId()
    {
        return envId;
    }


    public ContainerAction getContainerAction()
    {
        return containerAction;
    }


    public void setContainerAction( final ContainerAction containerAction )
    {
        this.containerAction = containerAction;
    }
}
