package io.subutai.common.peer;


import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostId;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.protocol.Template;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerSize;


/**
 * Container host interface.
 */
public interface ContainerHost extends Host, ContainerHostInfo
{
    ContainerSize getContainerSize();

    void setContainerSize( ContainerSize containerSize );

    ContainerId getContainerId();

    String getInitiatorPeerId();

    String getOwnerId();

    EnvironmentId getEnvironmentId();

    void dispose() throws PeerException;

    void start() throws PeerException;

    void stop() throws PeerException;

    Template getTemplate() throws PeerException;

    String getTemplateName();

    String getTemplateId();

    boolean isLocal();

    /**
     * Returns process's resource usage by pid
     *
     * @param processPid - pid which process usage to return
     *
     * @return - resource usage
     */
    ProcessResourceUsage getProcessResourceUsage( int processPid ) throws PeerException;


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

    void setContainerQuota( ContainerQuota containerQuota ) throws PeerException;

    HostId getResourceHostId();

    String getIp();
}
