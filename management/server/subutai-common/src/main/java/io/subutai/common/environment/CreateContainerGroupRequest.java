package io.subutai.common.environment;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.subutai.common.protocol.Criteria;
import io.subutai.common.protocol.Template;


public class CreateContainerGroupRequest
{
    private final Set<String> peerIps;
    private final UUID environmentId;
    private final UUID initiatorPeerId;
    private final UUID ownerId;
    private final String subnetCidr;
    private final List<Template> templates;
    private final int numberOfContainers;
    private final String strategyId;
    private final List<Criteria> criteria;
    private final int ipAddressOffset;


    public CreateContainerGroupRequest( final Set<String> peerIps, final UUID environmentId, final UUID initiatorPeerId,
                                        final UUID ownerId, final String subnetCidr, final List<Template> templates,
                                        final int numberOfContainers, final String strategyId,
                                        final List<Criteria> criteria, final int ipAddressOffset )
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


    public Set<String> getPeerIps()
    {
        return peerIps;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public UUID getInitiatorPeerId()
    {
        return initiatorPeerId;
    }


    public UUID getOwnerId()
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
