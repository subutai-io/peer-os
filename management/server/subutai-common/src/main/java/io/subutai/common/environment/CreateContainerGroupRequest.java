package io.subutai.common.environment;


import java.util.List;
import java.util.Map;

import io.subutai.common.protocol.Criteria;
import io.subutai.common.protocol.Template;


public class CreateContainerGroupRequest
{
    private final Map<String, String> peerIps;
    private final String environmentId;
    private final String initiatorPeerId;
    private final String ownerId;
    private final String subnetCidr;
    private final List<Template> templates;
    private final int numberOfContainers;
    private final String strategyId;
    private final List<Criteria> criteria;
    private final int ipAddressOffset;


    public CreateContainerGroupRequest( final Map<String, String> peerIps, final String environmentId,
                                        final String initiatorPeerId, final String ownerId, final String subnetCidr,
                                        final List<Template> templates, final int numberOfContainers,
                                        final String strategyId, final List<Criteria> criteria,
                                        final int ipAddressOffset )
    {
        this.peerIps = peerIps;
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.subnetCidr = subnetCidr;
        this.templates = templates;
        this.numberOfContainers = numberOfContainers;
        this.strategyId = strategyId;
        this.criteria = criteria;
        this.ipAddressOffset = ipAddressOffset;
    }


    public Map<String, String> getPeerIps()
    {
        return peerIps;
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


    public List<Template> getTemplates()
    {
        return templates;
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
