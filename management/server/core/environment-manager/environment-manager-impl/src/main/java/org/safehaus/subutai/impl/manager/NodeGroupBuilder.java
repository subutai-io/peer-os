package org.safehaus.subutai.impl.manager;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.manager.GroupInstance;
import org.safehaus.subutai.api.manager.NodeGroup;

import sun.security.jca.GetInstance;


/**
 * Created by bahadyr on 6/24/14.
 */
public class NodeGroupBuilder {
    InstanceCreator instanceCreator = new InstanceCreator();


    public NodeGroup buildNodeGroup( final NodeGroup nodeGroup ) {
        for ( GroupInstance groupInstance : nodeGroup.getGroupInstances() ) {
            GroupInstance instance = instanceCreator.createInstance( groupInstance );
            nodeGroup.getGroupInstances().add( instance );
        }
        return nodeGroup;
    }


    public void destroyNodeGroup( final NodeGroup nodeGroup ) {
        for ( GroupInstance groupInstance : nodeGroup.getGroupInstances() ) {
            instanceCreator.destroyInstance( groupInstance );
        }
    }
}
