package org.safehaus.subutai.core.peer.api;


import java.util.Set;

import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;


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

    public ContainerHostState getState() throws PeerException;

    public ProcessResourceUsage getProcessResourceUsage( ContainerHost containerHost, int processPid )
            throws PeerException;

    public PeerQuotaInfo getQuota( QuotaType quotaType ) throws PeerException;

    public void setQuota( QuotaInfo quota ) throws PeerException;

    String getCreatorPeerId();

    void dispose() throws PeerException;

    Peer getPeer();

    void setPeer( Peer peer );

    Template getTemplate() throws PeerException;

    String getTemplateName();

    public void addTag( String tag );

    public void removeTag( String tag );

    public Set<String> getTags();

    public void setDataService( DataService dataService );
}
