package io.subutai.common.environment;


import java.util.List;

import io.subutai.common.peer.ContainerType;
import io.subutai.common.protocol.Criteria;


public class CreateEnvironmentContainerGroupRequest
{
    private final String environmentId;
    private final String initiatorPeerId;
    private final String ownerId;
    private final String subnetCidr;
    private final int numberOfContainers;
    private final String strategyId;
    private final List<Criteria> criteria;
    private final String host;
    private final int ipAddressOffset;
    private final String templateName;
    private final ContainerDistributionType containerDistributionType;
    private final ContainerType containerType;


    public CreateEnvironmentContainerGroupRequest( final String environmentId, final String initiatorPeerId,
                                                   final String ownerId, final String subnetCidr,
                                                   final int numberOfContainers, final String strategyId,
                                                   final List<Criteria> criteria, final int ipAddressOffset,
                                                   final String templateName, final ContainerType containerType )
    {
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.subnetCidr = subnetCidr;
        this.numberOfContainers = numberOfContainers;
        this.strategyId = strategyId;
        this.criteria = criteria;
        this.ipAddressOffset = ipAddressOffset;
        this.templateName = templateName;
        this.containerDistributionType = ContainerDistributionType.AUTO;
        this.host = null;
        this.containerType = containerType;
    }


    public CreateEnvironmentContainerGroupRequest( final String environmentId, final String initiatorPeerId,
                                                   final String ownerId, final String subnetCidr,
                                                   final int numberOfContainers, final int ipAddressOffset,
                                                   final String templateName, String host, ContainerType containerType )
    {
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.subnetCidr = subnetCidr;
        this.numberOfContainers = numberOfContainers;
        this.strategyId = null;
        this.criteria = null;
        this.ipAddressOffset = ipAddressOffset;
        this.templateName = templateName;
        this.host = host;
        this.containerDistributionType = ContainerDistributionType.CUSTOM;
        this.containerType = containerType;
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


    public int getNumberOfContainers()
    {
        return numberOfContainers;
    }


    public String getStrategyId()
    {
        return strategyId;
    }


    public List<Criteria> getCriteria()
    {
        return criteria;
    }


    public int getIpAddressOffset()
    {
        return ipAddressOffset;
    }


    public String getHost()
    {
        return host;
    }


    public ContainerDistributionType getContainerDistributionType()
    {
        return containerDistributionType;
    }


    public ContainerType getContainerType()
    {
        return containerType;
    }
}
