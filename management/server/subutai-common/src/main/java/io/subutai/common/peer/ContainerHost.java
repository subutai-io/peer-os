package io.subutai.common.peer;


import java.util.Set;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostId;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;


/**
 * Container host interface.
 */
public interface ContainerHost extends Host, ContainerHostInfo
{
    ContainerSize getContainerSize();

    ContainerId getContainerId();

    String getInitiatorPeerId();

    String getOwnerId();

    EnvironmentId getEnvironmentId();

    void dispose() throws PeerException;

    void start() throws PeerException;

    void stop() throws PeerException;

    Peer getPeer();

    TemplateKurjun getTemplate() throws PeerException;

    String getTemplateName();

    void addTag( String tag );

    void removeTag( String tag );

    Set<String> getTags();


    boolean isLocal();

    /**
     * Returns process's resource usage by pid
     *
     * @param processPid - pid which process usage to return
     *
     * @return - resource usage
     */
    public ProcessResourceUsage getProcessResourceUsage( int processPid ) throws PeerException;

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
     * Returns available quota values
     *
     * @return quota value
     */

    /**
     * Returns current quota values
     *
     * @return quota value
     */
    ContainerQuota getQuota() throws PeerException;

    /**
     * Sets quota values
     */
    void setQuota( ContainerQuota containerQuota ) throws PeerException;

    public HostId getResourceHostId();
}
