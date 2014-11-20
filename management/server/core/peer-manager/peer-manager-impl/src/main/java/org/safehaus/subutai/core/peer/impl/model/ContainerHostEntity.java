package org.safehaus.subutai.core.peer.impl.model;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.peer.api.ContainerState;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.SubutaiHost;


/**
 * ContainerHost class.
 */
public class ContainerHostEntity extends SubutaiHost
{
    private UUID environmentId;
    private UUID creatorPeerId;
    private String templateName;
    private String templateArch;
    private ContainerState state = ContainerState.UNKNOWN;
    private String nodeGroupName;


    public ContainerHostEntity( final Agent agent, UUID peerId, UUID creatorPeerId, UUID environmentId )
    {
        super( agent, peerId );
        this.creatorPeerId = creatorPeerId;
        this.environmentId = environmentId;
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }


    public void setNodeGroupName( final String nodeGroupName )
    {
        this.nodeGroupName = nodeGroupName;
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


    public void updateHeartbeat()
    {
        lastHeartbeat = System.currentTimeMillis();
        setState( ContainerState.RUNNING );
    }


    public String getQuota( final QuotaEnum quota ) throws PeerException
    {
        Peer peer = getPeer();
        return peer.getQuota( this, quota );
    }


    public void setQuota( final QuotaEnum quota, final String value ) throws PeerException
    {
        Peer peer = getPeer();
        peer.setQuota( this, quota, value );
    }


    public Template getTemplate() throws PeerException
    {
        Peer peer = getPeer();
        return peer.getTemplate( getTemplateName() );
    }


    public void dispose() throws PeerException
    {
        Peer peer = getPeer();
        peer.destroyContainer( this );
    }
}
