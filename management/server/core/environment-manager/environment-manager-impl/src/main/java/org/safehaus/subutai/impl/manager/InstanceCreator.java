package org.safehaus.subutai.impl.manager;


import org.safehaus.subutai.api.manager.EnvironmentGroupInstance;
import org.safehaus.subutai.api.manager.GroupInstance;
import org.safehaus.subutai.impl.manager.exception.EnvironmentInstanceDestroyException;
import org.safehaus.subutai.impl.manager.exception.InstanceCreateException;


/**
 * Created by bahadyr on 6/24/14.
 */
public class InstanceCreator {


    public EnvironmentGroupInstance createInstance( final GroupInstance groupInstance ) throws InstanceCreateException {

        //TODO call new lxc moudle to create instance;\
        throw new InstanceCreateException();

//        return groupInstance;
    }


    public void destroyEnvironmentInstance( final EnvironmentGroupInstance environmentGroupInstance) throws EnvironmentInstanceDestroyException {

        //TODO call new lxc moudle to destroy instance;
        throw new EnvironmentInstanceDestroyException();
    }
}
