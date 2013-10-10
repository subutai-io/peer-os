/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.interfaces;

import org.safehaus.kiskis.mgmt.shared.protocol.commands.CommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;

/**
 *
 * @author bahadyr
 */
public interface CommandBuilderInterface {
 
        public Request buildRequest(CommandEnum commandEnum);
}
