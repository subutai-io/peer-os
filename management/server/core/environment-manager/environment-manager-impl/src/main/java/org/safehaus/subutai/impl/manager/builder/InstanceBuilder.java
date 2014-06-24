package org.safehaus.subutai.impl.manager.builder;


import java.util.Set;

import org.safehaus.subutai.api.manager.helper.EnvironmentGroupInstance;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.impl.manager.exception.EnvironmentInstanceDestroyException;
import org.safehaus.subutai.impl.manager.exception.InstanceCreateException;


/**
 * Created by bahadyr on 6/24/14.
 */
public class InstanceBuilder {


    public void destroy( final EnvironmentGroupInstance environmentGroupInstance )
            throws EnvironmentInstanceDestroyException {

        //TODO call new lxc moudle to destroy instance;
        throw new EnvironmentInstanceDestroyException("Error while destroying instance.");
    }


    public EnvironmentGroupInstance build( final Set<String> physicalNodes,
                                                    final PlacementStrategyENUM placementStrategyENUM,
                                                    final String templateName ) throws InstanceCreateException {
        //TODO call new lxc moudle to create instance;
        // EnvironmentGroupInstance instance = new EnvironmentGroupInstance();

        throw new InstanceCreateException("Error creating instance");

        //        return instance;
    }
}
