/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.shared.protocol;

import org.safehaus.subutai.shared.protocol.enums.NodeState;

/**
 * @author dilshat
 */
public interface CompleteEvent {

	public void onComplete(NodeState state);
}
