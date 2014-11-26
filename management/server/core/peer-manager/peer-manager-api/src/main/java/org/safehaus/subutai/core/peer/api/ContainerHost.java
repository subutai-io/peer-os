package org.safehaus.subutai.core.peer.api;


import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;


/**
 * Container host interface.
 */
public interface ContainerHost extends Host
{

    public String getParentHostname();

    public String getEnvironmentId();

    public void setNodeGroupName( String nodeGroupName );

    public void setEnvironmentId( String environmentId );

    public void setCreatorPeerId( String creatorPeerId );

    public void setTemplateName( String templateName );

    public String getNodeGroupName();

    String getTemplateArch();

    void setTemplateArch( String templateArch );

    public ContainerHostState getState();

    //    public void setParent( ResourceHost resourceHost );

    String getQuota( QuotaEnum memoryLimitInBytes ) throws PeerException;

    void setQuota( QuotaEnum memoryLimitInBytes, String memoryLimit ) throws PeerException;

    String getCreatorPeerId();

    void dispose() throws PeerException;

    Peer getPeer() throws PeerException;

    Template getTemplate() throws PeerException;

    String getTemplateName();
}
