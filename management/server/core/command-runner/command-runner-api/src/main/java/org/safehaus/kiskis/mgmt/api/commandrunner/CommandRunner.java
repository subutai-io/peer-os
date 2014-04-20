/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.commandrunner;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public interface CommandRunner {

    public void runCommandAsync(Command command, CommandCallback commandCallback);

    public Command createCommand(RequestBuilder requestBuilder, Set<Agent> agents);

}
