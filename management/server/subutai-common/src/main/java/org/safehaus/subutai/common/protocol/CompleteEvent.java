/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.common.protocol;


import org.safehaus.subutai.common.enums.NodeState;


public interface CompleteEvent {
    public void onComplete( NodeState state );
}
