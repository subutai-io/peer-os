package io.subutai.common.environment;


import java.util.List;

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
    private final int ipAddressOffset;
    private final String templateName;


    public CreateEnvironmentContainerGroupRequest( final String environmentId, final String initiatorPeerId,
                                                   final String ownerId, final String subnetCidr,
                                                   final int numberOfContainers, final String strategyId,
                                                   final List<Criteria> criteria, final int ipAddressOffset,
                                                   final String templateName )
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
}
