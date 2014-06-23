package org.safehaus.subutai.impl.manager;


import org.safehaus.subutai.api.manager.GroupInstance;
import org.safehaus.subutai.impl.manager.org.safehaus.subutai.impl.manager.exception.InstanceCreateException;
import org.safehaus.subutai.impl.manager.org.safehaus.subutai.impl.manager.exception.InstanceDestroyException;


/**
 * Created by bahadyr on 6/24/14.
 */
public class InstanceCreator {


    public GroupInstance createInstance( final GroupInstance groupInstance ) throws InstanceCreateException {

        //TODO call new lxc moudle to create instance;\
        throw new InstanceCreateException();

//        return groupInstance;
    }


    public void destroyInstance( final GroupInstance groupInstance ) throws InstanceDestroyException {

        //TODO call new lxc moudle to destroy instance;
        throw new InstanceDestroyException();
    }
}
