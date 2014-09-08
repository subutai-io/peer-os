/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.pig;


import org.safehaus.subutai.common.protocol.ApiBase;

import java.util.UUID;


public interface Pig extends ApiBase<Config> {


	public UUID destroyNode(String clusterName, String lxcHostname);
}
