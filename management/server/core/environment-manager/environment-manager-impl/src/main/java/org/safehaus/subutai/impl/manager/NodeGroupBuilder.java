package org.safehaus.subutai.impl.manager;


import org.safehaus.subutai.api.manager.GroupInstance;
import org.safehaus.subutai.api.manager.NodeGroup;
import org.safehaus.subutai.impl.manager.org.safehaus.subutai.impl.manager.exception.InstanceCreateException;
import org.safehaus.subutai.impl.manager.org.safehaus.subutai.impl.manager.exception.InstanceDestroyException;
import org.safehaus.subutai.impl.manager.org.safehaus.subutai.impl.manager.exception.NodeGroupBuildException;


/**
 * Created by bahadyr on 6/24/14.
 */
public class NodeGroupBuilder {
    InstanceCreator instanceCreator = new InstanceCreator();


    public NodeGroup buildNodeGroup( final NodeGroup nodeGroup ) throws NodeGroupBuildException {
        for ( GroupInstance groupInstance : nodeGroup.getGroupInstances() ) {
            GroupInstance instance = null;
            try {
                instance = instanceCreator.createInstance( groupInstance );
                nodeGroup.getGroupInstances().add( instance );
            }
            catch ( InstanceCreateException e ) {
                e.printStackTrace();
            }
            finally {
                throw new NodeGroupBuildException();
            }
        }
        return nodeGroup;
    }


    public boolean destroyNodeGroup( final NodeGroup nodeGroup ) {
        for ( GroupInstance groupInstance : nodeGroup.getGroupInstances() ) {
            try {
                instanceCreator.destroyInstance( groupInstance );
            }
            catch ( InstanceDestroyException e ) {
                e.printStackTrace();
                //TODO log destroy status log
            }
            finally {
                return false;
            }
        }
        return true;
    }
}
