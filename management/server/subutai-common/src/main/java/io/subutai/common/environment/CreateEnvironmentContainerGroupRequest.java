package io.subutai.common.environment;


import io.subutai.common.peer.ContainerSize;


public class CreateEnvironmentContainerGroupRequest
{
    private final String hostname;
    private final String templateName;
    private final String environmentId;
    private final String initiatorPeerId;
    private final String ownerId;
    private final String subnetCidr;
    private final String host;
    private final int ipAddressOffset;
    private final ContainerSize containerSize;


    public CreateEnvironmentContainerGroupRequest( final String hostname, final String environmentId,
                                                   final String initiatorPeerId, final String ownerId,
                                                   final String subnetCidr, final int ipAddressOffset,
                                                   final String templateName, String host, ContainerSize containerSize )
    {
        this.hostname = hostname;
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.subnetCidr = subnetCidr;
        this.ipAddressOffset = ipAddressOffset;
        this.templateName = templateName;
        this.host = host;
        this.containerSize = containerSize;
    }


    public String getHostname()
    {
        return hostname;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getInitiatorPeerId()
    {
        return initiatorPeerId;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public String getSubnetCidr()
    {
        return subnetCidr;
    }


    public int getIpAddressOffset()
    {
        return ipAddressOffset;
    }


    public String getHost()
    {
        return host;
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }
}
