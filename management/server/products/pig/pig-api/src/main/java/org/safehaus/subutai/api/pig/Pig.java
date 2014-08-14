/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.pig;


import java.util.UUID;

import org.safehaus.subutai.shared.protocol.ApiBase;


public interface Pig extends ApiBase<Config> {


    public UUID destroyNode( String clusterName, String lxcHostname );
}
