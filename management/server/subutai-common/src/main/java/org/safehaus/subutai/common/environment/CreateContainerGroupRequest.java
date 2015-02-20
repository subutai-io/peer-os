package org.safehaus.subutai.common.environment;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;


public class CreateContainerGroupRequest
{
    private final Set<String> remotePeerIps;
    private final UUID environmentId;
    private final UUID initiatorPeerId;
    private final UUID ownerId;
    private final long vni;
    private final List<Template> templates;
    private final int numberOfContainers;
    private final String strategyId;
    private final List<Criteria> criteria;


    public CreateContainerGroupRequest( final Set<String> remotePeerIps, final UUID environmentId,
                                        final UUID initiatorPeerId, final UUID ownerId, final long vni,
                                        final List<Template> templates, final int numberOfContainers,
                                        final String strategyId, final List<Criteria> criteria )
    {
        this.remotePeerIps = remotePeerIps;
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.vni = vni;
        this.templates = templates;
        this.numberOfContainers = numberOfContainers;
        this.strategyId = strategyId;
        this.criteria = criteria;
    }


    public Set<String> getRemotePeerIps()
    {
        return remotePeerIps;
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


    public long getVni()
    {
        return vni;
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
}
