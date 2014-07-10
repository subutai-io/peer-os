package org.safehaus.subutai.impl.container;

import java.util.Set;
import java.util.UUID;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;

class NodeGroupInfo {

    String name;
    String templateName;
    Set<PlacementStrategyENUM> strategy;
    Set<UUID> instanceIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Set<UUID> getInstanceIds() {
        return instanceIds;
    }

    public void setInstanceIds(Set<UUID> instanceIds) {
        this.instanceIds = instanceIds;
    }

}
