/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.broker;

import org.safehaus.kiskis.mgmt.shared.protocol.commands.CommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.CommandBuilderInterface;

/**
 *
 * @author bahadyr
 */
public class CommandBuilder implements CommandBuilderInterface {

    public Request buildRequest(CommandEnum commandEnum) {
        Request r = new Request();
        switch (commandEnum) {
            case INSTALL: {
                // building command
                String[] argument = {"-n, hsadood ,adhfas df"};
                r.setEnvironment(null);
            }
            case CLONE: {
                return null;
            }
            case START: {
                return null;
            }
            case STOP: {
                return null;
            }

        }
        return null;
    }
}
