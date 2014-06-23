package org.safehaus.subutai.impl.manager;


import org.safehaus.subutai.api.manager.helper.EnvironmentGroupInstance;
import org.safehaus.subutai.api.manager.helper.EnvironmentNodeGroup;
import org.safehaus.subutai.api.manager.helper.NodeGroup;
import org.safehaus.subutai.impl.manager.exception.EnvironmentInstanceDestroyException;
import org.safehaus.subutai.impl.manager.exception.InstanceCreateException;
import org.safehaus.subutai.impl.manager.exception.NodeGroupBuildException;


/**
 * Created by bahadyr on 6/24/14.
 */
public class NodeGroupBuilder {
    InstanceCreator instanceCreator = new InstanceCreator();


    public EnvironmentNodeGroup buildNodeGroup( final NodeGroup nodeGroup ) throws NodeGroupBuildException {

        EnvironmentNodeGroup environmentNodeGroup = new EnvironmentNodeGroup();
        for(int i = 0; i < nodeGroup.getNumberOfNodes(); i++) {
            try {
                EnvironmentGroupInstance environmentGroupInstance = instanceCreator.createInstance(
                        nodeGroup.getPhysicalNodes(), nodeGroup.getPlacementStrategyENUM(), nodeGroup.getTemplateName());

                // call Network bundle for
                // nodeGroup.isExchangeSshKeys();
                // nodeGroup.isLinkHosts();


                environmentNodeGroup.getEnvironmentGroupInstanceSet().add( environmentGroupInstance );
            }
            catch ( InstanceCreateException e ) {
                e.printStackTrace();
                // TODO rollback action
            }
            finally {
                throw new NodeGroupBuildException();
            }
        }
        return environmentNodeGroup;
    }


    public boolean destroyEnvironmentNodeGroup( final EnvironmentNodeGroup environmentNodeGroup ) {
        for ( EnvironmentGroupInstance environmentGroupInstance : environmentNodeGroup
                .getEnvironmentGroupInstanceSet() ) {
            try {
                instanceCreator.destroyEnvironmentInstance( environmentGroupInstance );
            }
            catch ( EnvironmentInstanceDestroyException e ) {
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
