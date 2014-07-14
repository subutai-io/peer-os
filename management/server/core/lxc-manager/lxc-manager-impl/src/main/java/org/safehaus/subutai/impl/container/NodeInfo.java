package org.safehaus.subutai.impl.container;

import java.util.Set;
import java.util.UUID;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;

class NodeInfo {

    int envId;
    String templateName;
    Set<PlacementStrategyENUM> strategy;
    UUID instanceId;

    public int getEnvId() {
        return envId;
    }

    public void setEnvId(int envId) {
        this.envId = envId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Set<PlacementStrategyENUM> getStrategy() {
        return strategy;
    }

    public void setStrategy(Set<PlacementStrategyENUM> strategy) {
        this.strategy = strategy;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
    }

}
