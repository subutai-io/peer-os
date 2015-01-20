package org.safehaus.subutai.env.api.build;


import java.util.Set;


/**
 * Blueprint for environment creation
 */
public interface Blueprint
{
    public Set<NodeGroup> getNodeGroups();
}
