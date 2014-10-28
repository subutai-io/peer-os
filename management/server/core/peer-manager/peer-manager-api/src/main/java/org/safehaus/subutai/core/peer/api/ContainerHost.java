package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.ContainerState;


/**
 * ContainerHost class.
 */
public class ContainerHost extends SubutaiHost
{
    private UUID environmentId;
    private UUID creatorPeerId;
    private String templateName;
    private String templateArch;
    private ContainerState state = ContainerState.UNKNOWN;


    public ContainerHost( final Agent agent )
    {
        super( agent );
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public UUID getCreatorPeerId()
    {
        return creatorPeerId;
    }


    public void setCreatorPeerId( final UUID creatorPeerId )
    {
        this.creatorPeerId = creatorPeerId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getTemplateArch()
    {
        return templateArch;
    }


    public void setTemplateArch( final String templateArch )
    {
        this.templateArch = templateArch;
    }


    public ContainerState getState()
    {
        return state;
    }


    public void setState( final ContainerState state )
    {
        this.state = state;
    }


    @Override
    public boolean isConnected()
    {
        return ContainerState.RUNNING.equals( state ) && super.isConnected();
    }
}
