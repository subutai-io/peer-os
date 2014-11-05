package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;

import org.safehaus.subutai.common.protocol.NodeGroup;


/**
 * Created by bahadyr on 11/5/14.
 */
public interface NodeGroupExecutor
{
    List<ContainerDistributionMessage> disributeNodeGroup(NodeGroup nodeGroup);
}
