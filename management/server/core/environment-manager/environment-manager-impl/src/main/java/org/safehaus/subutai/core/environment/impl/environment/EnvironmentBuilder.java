package org.safehaus.subutai.core.environment.impl.environment;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.peer.api.ContainerHost;


/**
 * Created by bahadyr on 11/5/14.
 */
public interface EnvironmentBuilder
{

    public Environment build( final EnvironmentBlueprint blueprint, final EnvironmentBuildProcess process )
            throws BuildException;

    public void addNodeGroup( UUID environmentId, NodeGroup nodeGroup ) throws BuildException;

    public void addContainerToNodeGroup( CloneContainersMessage message );

    public void removeContainer( ContainerHost containerHost );
}
