package org.safehaus.subutai.common.peer;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;


/**
 * Peer interface
 */
public interface Peer
{

    public UUID getId();

    public String getName();

    public UUID getOwnerId();

    public PeerInfo getPeerInfo();

    public Set<HostInfoModel> createContainers( final UUID environmentId, final UUID initiatorPeerId,
                                                final UUID ownerId, final List<Template> templates,
                                                final int numberOfContainers, final String strategyId,
                                                final List<Criteria> criteria ) throws PeerException;

    public void startContainer( ContainerHost containerHost ) throws PeerException;

    public void stopContainer( ContainerHost containerHost ) throws PeerException;

    public void destroyContainer( ContainerHost containerHost ) throws PeerException;

    public boolean isConnected( Host host );


    public CommandResult execute( RequestBuilder requestBuilder, Host host ) throws CommandException;

    public CommandResult execute( RequestBuilder requestBuilder, Host host, CommandCallback callback )
            throws CommandException;

    public void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException;

    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException;

    public boolean isLocal();

    @Deprecated
    public PeerQuotaInfo getQuota( ContainerHost host, QuotaType quotaType ) throws PeerException;

    @Deprecated
    public void setQuota( ContainerHost host, QuotaInfo quotaInfo ) throws PeerException;

    public Template getTemplate( String templateName ) throws PeerException;

    public boolean isOnline() throws PeerException;

    public <T, V> V sendRequest( T request, String recipient, int requestTimeout, Class<V> responseType,
                                 int responseTimeout, UUID environmentId ) throws PeerException;

    public <T> void sendRequest( T request, String recipient, int requestTimeout, UUID environmentId )
            throws PeerException;

    public ContainerHostState getContainerHostState( UUID containerId ) throws PeerException;

    //******** Quota functions ***********

    public ProcessResourceUsage getProcessResourceUsage( UUID containerId, int processPid ) throws PeerException;

    /**
     * Returns available RAM quota on container in megabytes
     *
     * @param containerId - id of container
     *
     * @return - quota in mb
     */
    public int getAvailableRamQuota( UUID containerId ) throws PeerException;

    /**
     * Returns available CPU quota on container in percent
     *
     * @param containerId - id of container
     *
     * @return - cpu quota on container in percent
     */
    public int getAvailableCpuQuota( UUID containerId ) throws PeerException;

    /**
     * Returns available disk quota
     *
     * @param diskPartition - disk partition which quota to return
     *
     * @return - disk partition quota
     */
    public DiskQuota getAvailableDiskQuota( UUID containerId, DiskPartition diskPartition ) throws PeerException;

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

    /**
     * Returns disk quota
     *
     * @param containerId - id of container
     * @param diskPartition - disk partition which quota to return
     *
     * @return - disk partition quota
     */
    public DiskQuota getDiskQuota( UUID containerId, DiskPartition diskPartition ) throws PeerException;


    /**
     * Sets disk partition quota
     *
     * @param containerId - id of container
     * @param diskQuota - quota to set
     */
    public void setDiskQuota( UUID containerId, DiskQuota diskQuota ) throws PeerException;


    /**
     * Destroys hosted part of environment
     *
     * @param environmentId - id fo environment
     *
     * @return {@code ContainersDestructionResult}
     */
    public ContainersDestructionResult destroyEnvironmentContainers( UUID environmentId ) throws PeerException;
}
