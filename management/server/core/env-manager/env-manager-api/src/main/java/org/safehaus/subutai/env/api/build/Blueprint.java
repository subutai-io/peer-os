package org.safehaus.subutai.env.api.build;


import java.util.Set;


/**
 * Blueprint for environment creation
 */
public interface Blueprint
{
    public String getName();
    public Set<NodeGroup> getNodeGroups();
}
