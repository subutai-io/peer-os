package org.safehaus.subutai.impl.manager.builder;


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
    InstanceBuilder instanceCreator;


    public NodeGroupBuilder() {
        this.instanceCreator = new InstanceBuilder();
    }


    public EnvironmentNodeGroup build( final NodeGroup nodeGroup ) throws NodeGroupBuildException {

        EnvironmentNodeGroup environmentNodeGroup = new EnvironmentNodeGroup();
        for ( int i = 0; i < nodeGroup.getNumberOfNodes(); i++ ) {
            try {
                EnvironmentGroupInstance environmentGroupInstance = instanceCreator
                        .build( nodeGroup.getPhysicalNodes(), nodeGroup.getPlacementStrategyENUM(),
                                nodeGroup.getTemplateName() );

                // call Network bundle for
                // nodeGroup.isExchangeSshKeys();
                // nodeGroup.isLinkHosts();


                environmentNodeGroup.getEnvironmentGroupInstanceSet().add( environmentGroupInstance );
            }
            catch ( InstanceCreateException e ) {
                //                e.printStackTrace();
                System.out.println( e.getMessage() );
                // TODO rollback action

            }
            finally {
                throw new NodeGroupBuildException( "Error building environment node group" );
            }
        }
        return environmentNodeGroup;
    }


    public boolean destroy( final EnvironmentNodeGroup environmentNodeGroup )
            throws EnvironmentInstanceDestroyException {
        for ( EnvironmentGroupInstance environmentGroupInstance : environmentNodeGroup
                .getEnvironmentGroupInstanceSet() ) {
            instanceCreator.destroy( environmentGroupInstance );
            //TODO log destroy status log
        }
        return true;
    }
}
