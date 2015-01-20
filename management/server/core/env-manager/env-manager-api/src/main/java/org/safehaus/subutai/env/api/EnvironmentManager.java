package org.safehaus.subutai.env.api;


import java.util.UUID;

import org.safehaus.subutai.env.api.build.NodeGroup;
import org.safehaus.subutai.env.api.build.Topology;
import org.safehaus.subutai.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.env.api.exception.EnvironmentModificationException;


/**
 * Environment Manager
 */
public interface EnvironmentManager
{
    public void createEnvironment( Topology topology ) throws EnvironmentCreationException;

    public void destroyEnvironment( UUID environmentId ) throws EnvironmentDestructionException;

    public void addNodeGroup2Environment( UUID environmentId, NodeGroup nodeGroup )
            throws EnvironmentModificationException;

    public void destroyContainer( UUID containerId ) throws EnvironmentModificationException;
}
