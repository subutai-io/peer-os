/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.broker;

import org.safehaus.kiskis.mgmt.shared.protocol.commands.CommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.CommandBuilderImp;

/**
 *
 * @author bahadyr
 */
public class CommandBuilder implements CommandBuilderImp {

    public Request buildRequest(CommandEnum commandEnum) {
        Request r = new Request();
        switch (commandEnum) {
            case CLONING_DEFAULT_LXC: {
                String[] argument = {"-n"};
                r.setEnvironment(null);
            }
            case INSTALLING_DEFAULT_LXC_CONTAINER_IN_A_HOST: {
                return null;
            }
            case INSTALLING_HADOOP_NODE: {
                return null;
            }
            case INSTALLING_LXC_IN_A_HOST: {
                return null;
            }
            case STARTING_LXC_CONTAINERS: {
                return null;
            }
            case STOPPING_LXC_CONTAINERS: {
                return null;
            }

        }
        return null;
    }
}
