package io.subutai.common.peer;


import java.util.Set;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.protocol.Template;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Container host interface.
 */
public interface ContainerHost extends Host, ContainerHostInfo
{
    ContainerId getContainerId();

    String getInitiatorPeerId();

    String getOwnerId();

    String getEnvironmentId();

    String getNodeGroupName();

    void dispose() throws PeerException;

    void start() throws PeerException;

    void stop() throws PeerException;

    Peer getPeer();

    Template getTemplate() throws PeerException;

    String getTemplateName();

    void addTag( String tag );

    void removeTag( String tag );

    Set<String> getTags();

    void setDefaultGateway( String gatewayIp ) throws PeerException;

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
     * Returns available quota value by resource type
     *
     * @param resourceType resource type
     *
     * @return quota value
     */
    ResourceValue getAvailableQuota( ResourceType resourceType ) throws PeerException;

    /**
     * Returns current quota value by resource type
     *
     * @param resourceType resource type
     *
     * @return quota value
     */
    ResourceValue getQuota( ResourceType resourceType ) throws PeerException;

    /**
     * Sets quota value by resource type to new value
     *
     * @param resourceType resource type
     * @param newValue new quota value
     */
    void setQuota( ResourceType resourceType, ResourceValue newValue ) throws PeerException;


    ContainerType getContainerType();
}
