package org.safehaus.subutai.impl.manager.builder;


import org.safehaus.subutai.api.manager.helper.EnvironmentNodeGroup;
import org.safehaus.subutai.api.manager.helper.NodeGroup;
import org.safehaus.subutai.impl.manager.exception.EnvironmentInstanceDestroyException;
import org.safehaus.subutai.impl.manager.exception.NodeGroupBuildException;


/**
 * Created by bahadyr on 6/24/14.
 */
public class NodeGroupBuilder {


    public NodeGroupBuilder() {

    }


    public EnvironmentNodeGroup build( final NodeGroup nodeGroup ) throws NodeGroupBuildException {

        EnvironmentNodeGroup environmentNodeGroup = new EnvironmentNodeGroup();
//        try {

            //TODO call new lxcManager (waiting for Azilet's module)
            /*List<Agent> instances = lxcManager
                    .createNodeGroup( nodeGroup.getNumberOfNodes(), nodeGroup.getPhysicalNodes(),
                            nodeGroup.getPlacementStrategyENUM(), nodeGroup.getTemplateName() );
*/
            // call Network bundle for
            // nodeGroup.isExchangeSshKeys();
            // nodeGroup.isLinkHosts();

            //            environmentNodeGroup.setInstances( instances );
            environmentNodeGroup.setInstances( null );
//        }
//        catch ( NodeGroupBuildException e ) {
//            System.out.println( e.getMessage() );
//            // TODO take some action
//        }
//        finally {
//            //            throw new NodeGroupBuildException( "Error building environment node group" );
//        }
        return environmentNodeGroup;
    }


    public boolean destroy( final EnvironmentNodeGroup environmentNodeGroup )
            throws EnvironmentInstanceDestroyException {
        //TODO lxcManager.destroyNodeGroup( environmentNodeGroup.getInstances() );
        return true;
    }
}
