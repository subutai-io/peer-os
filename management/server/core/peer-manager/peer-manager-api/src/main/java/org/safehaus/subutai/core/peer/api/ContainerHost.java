package org.safehaus.subutai.core.peer.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
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


    /**
     * Returns RAM quota on container in megabytes
     *
     * @param containerId - id of container
     *
     * @return - quota in mb
     */
    public int getRamQuota( UUID containerId ) throws PeerException;

    /**
     * Sets RAM quota on container in megabytes
     *
     * @param containerId - id of container
     * @param ramInMb - quota in mb
     */
    public void setRamQuota( UUID containerId, int ramInMb ) throws PeerException;


    /**
     * Returns CPU quota on container in percent
     *
     * @param containerId - id of container
     *
     * @return - cpu quota on container in percent
     */
    public int getCpuQuota( UUID containerId ) throws PeerException;

    /**
     * Sets CPU quota on container in percent
     *
     * @param containerId - id of container
     * @param cpuPercent - cpu quota in percent
     */
    public void setCpuQuota( UUID containerId, int cpuPercent ) throws PeerException;

    /**
     * Returns allowed cpus/cores ids on container
     *
     * @param containerId - id of container
     *
     * @return - allowed cpu set
     */
    public Set<Integer> getCpuSet( UUID containerId ) throws PeerException;

    /**
     * Sets allowed cpus/cores on container
     *
     * @param containerId - id of container
     * @param cpuSet - allowed cpu set
     */
    public void setCpuSet( UUID containerId, Set<Integer> cpuSet ) throws PeerException;


    public DiskQuota getDiskQuota( UUID containerId, DiskPartition diskPartition ) throws PeerException;

    public void setDiskQuota( UUID containerId, DiskQuota diskQuota ) throws PeerException;
}
