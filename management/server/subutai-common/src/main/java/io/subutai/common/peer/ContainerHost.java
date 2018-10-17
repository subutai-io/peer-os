package io.subutai.common.peer;


import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInterface;
import io.subutai.common.protocol.Template;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;


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

    void start() throws PeerException;

    void stop() throws PeerException;

    Template getTemplate() throws PeerException;

    String getTemplateName();

    String getTemplateId();

    boolean isLocal();


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

    HostId getResourceHostId();

    String getIp();

    HostInterface getInterfaceByName( String interfaceName );

    /**
     * Returns creation timestamp
     */
    long getCreationTimestamp();
}
