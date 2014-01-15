/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.api;

import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

/**
 *
 * @author dilshat
 */
public interface CommandManager {

    boolean executeCommand(Command command);

    void addListener(CommandListener listener);

    void removeListener(CommandListener listener);
}
