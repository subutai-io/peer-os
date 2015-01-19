package org.safehaus.subutai.core.peer.api;


import java.util.Set;

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

    @Deprecated
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
     * Returns process's resource usage by pid
     *
     * @param processPid - pid which process usage to return
     *
     * @return - resource usage
     */
    public ProcessResourceUsage getProcessResourceUsage( int processPid ) throws PeerException;

    /**
     * Returns RAM quota on container in megabytes
     *
     * @return - quota in mb
     */
    public int getRamQuota() throws PeerException;

    /**
     * Sets RAM quota on container in megabytes
     *
     * @param ramInMb - quota in mb
     */
    public void setRamQuota( int ramInMb ) throws PeerException;


    /**
     * Returns CPU quota on container in percent
     *
     * @return - cpu quota on container in percent
     */
    public int getCpuQuota() throws PeerException;

    /**
     * Sets CPU quota on container in percent
     *
     * @param cpuPercent - cpu quota in percent
     */
    public void setCpuQuota( int cpuPercent ) throws PeerException;

    /**
     * Returns allowed cpus/cores ids on container
     *
     * @return - allowed cpu set
     */
    public Set<Integer> getCpuSet() throws PeerException;

    /**
     * Sets allowed cpus/cores on container
     *
     * @param cpuSet - allowed cpu set
     */
    public void setCpuSet( Set<Integer> cpuSet ) throws PeerException;

    /**
     * Returns disk quota
     *
     * @param diskPartition - disk partition which quota to return
     *
     * @return - disk partition quota
     */
    public DiskQuota getDiskQuota( DiskPartition diskPartition ) throws PeerException;

    /**
     * Sets disk partition quota
     *
     * @param diskQuota - quota to set
     */
    public void setDiskQuota( DiskQuota diskQuota ) throws PeerException;
}
