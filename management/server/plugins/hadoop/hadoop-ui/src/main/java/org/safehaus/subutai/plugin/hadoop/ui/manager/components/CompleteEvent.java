/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import org.safehaus.subutai.common.enums.NodeState;


/**
 * @author dilshat
 */
public interface CompleteEvent
{

    public void onComplete( String operationLog );

}
