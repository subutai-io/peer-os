package org.safehaus.subutai.common.peer;


import java.util.Set;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;


/**
 * Container host interface.
 */
public interface ContainerHost extends Host
{

    //---deprecated
    @Deprecated
    public String getParentHostname();

    @Deprecated
    public void setNodeGroupName( String nodeGroupName );

    @Deprecated
    public void setEnvironmentId( String environmentId );

    @Deprecated
    public void setCreatorPeerId( String creatorPeerId );

    @Deprecated
    public void setTemplateName( String templateName );


    @Deprecated
    void setPeer( Peer peer );

    @Deprecated
    public PeerQuotaInfo getQuota( QuotaType quotaType ) throws PeerException;

    @Deprecated
    public void setQuota( QuotaInfo quota ) throws PeerException;

    @Deprecated
    public void setDataService( DataService dataService );

    @Deprecated
    public String getInitiatorPeerId();

    //----------------------

    public String getEnvironmentId();

    public String getNodeGroupName();

    public ContainerHostState getState() throws PeerException;


    public void dispose() throws PeerException;

    public void start() throws PeerException;

    public void stop() throws PeerException;

    public Peer getPeer();


    public Template getTemplate() throws PeerException;

    public String getTemplateName();

    public void addTag( String tag );

    public void removeTag( String tag );

    public Set<String> getTags();


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
