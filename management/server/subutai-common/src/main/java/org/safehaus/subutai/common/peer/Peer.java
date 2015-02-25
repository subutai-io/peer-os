package org.safehaus.subutai.common.peer;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.environment.CreateContainerGroupRequest;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.CpuQuotaInfo;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.MemoryQuotaInfo;
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

    public Set<HostInfoModel> createContainerGroup( CreateContainerGroupRequest request ) throws PeerException;

    public void startContainer( ContainerHost containerHost ) throws PeerException;

    public void stopContainer( ContainerHost containerHost ) throws PeerException;

    public void destroyContainer( ContainerHost containerHost ) throws PeerException;

    public void setDefaultGateway( ContainerHost host, String gatewayIp ) throws PeerException;


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


    /**
     * Get quota for enum specified
     *
     * @param host - Target container host whose quota details been requested
     * @param quotaType - QuotaType needed
     *
     * @return - Abstract QuotaInfo class extended by quota classes
     */
    public QuotaInfo getQuotaInfo( ContainerHost host, QuotaType quotaType ) throws PeerException;


    @Deprecated
    public void setQuota( ContainerHost host, QuotaInfo quotaInfo ) throws PeerException;

    public Template getTemplate( String templateName ) throws PeerException;

    public boolean isOnline() throws PeerException;

    public <T, V> V sendRequest( T request, String recipient, int requestTimeout, Class<V> responseType,
                                 int responseTimeout, Map<String, String> headers ) throws PeerException;

    public <T> void sendRequest( T request, String recipient, int requestTimeout, Map<String, String> headers )
            throws PeerException;

    public ContainerHostState getContainerHostState( ContainerHost host ) throws PeerException;

    //******** Quota functions ***********

    public ProcessResourceUsage getProcessResourceUsage( ContainerHost host, int processPid ) throws PeerException;

    /**
     * Returns available RAM quota on container in megabytes
     *
     * @param host - container
     *
     * @return - quota in mb
     */
    public int getAvailableRamQuota( ContainerHost host ) throws PeerException;

    /**
     * Returns available CPU quota on container in percent
     *
     * @param host - container
     *
     * @return - cpu quota on container in percent
     */
    public int getAvailableCpuQuota( ContainerHost host ) throws PeerException;

    /**
     * Returns available disk quota
     *
     * @param host - container
     * @param diskPartition - disk partition which quota to return
     *
     * @return - disk partition quota
     */
    public DiskQuota getAvailableDiskQuota( ContainerHost host, DiskPartition diskPartition ) throws PeerException;

    /**
     * Returns RAM quota on container in megabytes
     *
     * @param host -  container
     *
     * @return - quota in mb
     */
    public int getRamQuota( ContainerHost host ) throws PeerException;


    /**
     * Returns RAM quota on container with details
     *
     * @param host -  container
     *
     * @return - MemoryQuotaInfo with quota details
     */
    public MemoryQuotaInfo getRamQuotaInfo( ContainerHost host ) throws PeerException;


    /**
     * Sets RAM quota on container in megabytes
     *
     * @param host - container
     * @param ramInMb - quota in mb
     */
    public void setRamQuota( ContainerHost host, int ramInMb ) throws PeerException;


    /**
     * Returns CPU quota on container in percent
     *
     * @param host - container
     *
     * @return - cpu quota on container in percent
     */
    public int getCpuQuota( ContainerHost host ) throws PeerException;


    /**
     * Returns CPU quota on container in brief description
     *
     * @param host - container
     *
     * @return - cpu quota object on container
     */
    public CpuQuotaInfo getCpuQuotaInfo( ContainerHost host ) throws PeerException;


    /**
     * Sets CPU quota on container in percent
     *
     * @param host - container
     * @param cpuPercent - cpu quota in percent
     */
    public void setCpuQuota( ContainerHost host, int cpuPercent ) throws PeerException;

    /**
     * Returns allowed cpus/cores ids on container
     *
     * @param host - container
     *
     * @return - allowed cpu set
     */
    public Set<Integer> getCpuSet( ContainerHost host ) throws PeerException;

    /**
     * Sets allowed cpus/cores on container
     *
     * @param host - container
     * @param cpuSet - allowed cpu set
     */
    public void setCpuSet( ContainerHost host, Set<Integer> cpuSet ) throws PeerException;

    /**
     * Returns disk quota
     *
     * @param host - container
     * @param diskPartition - disk partition which quota to return
     *
     * @return - disk partition quota
     */
    public DiskQuota getDiskQuota( ContainerHost host, DiskPartition diskPartition ) throws PeerException;


    /**
     * Sets disk partition quota
     *
     * @param host - container
     * @param diskQuota - quota to set
     */
    public void setDiskQuota( ContainerHost host, DiskQuota diskQuota ) throws PeerException;


    /**
     * Destroys hosted part of environment
     *
     * @param environmentId - id fo environment
     *
     * @return {@code ContainersDestructionResult}
     */
    public ContainersDestructionResult destroyEnvironmentContainers( UUID environmentId ) throws PeerException;

    //networking

    public void reserveVni( Vni vni ) throws PeerException;

    public Set<Vni> getReservedVnis() throws PeerException;
}
