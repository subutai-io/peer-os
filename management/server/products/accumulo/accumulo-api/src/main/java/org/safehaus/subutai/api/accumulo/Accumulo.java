/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.accumulo;


import org.safehaus.subutai.common.protocol.ApiBase;

import java.util.UUID;


/**
 * @author dilshat
 */
public interface Accumulo extends ApiBase<Config> {

	public UUID startCluster(String clusterName);

	public UUID stopCluster(String clusterName);

	public UUID checkNode(String clusterName, String lxcHostname);

	public UUID addNode(String clusterName, String lxcHostname, NodeType nodeType);

	public UUID destroyNode(String clusterName, String lxcHostname, NodeType nodeType);

	public UUID addProperty(String clusterName, String propertyName, String propertyValue);

	public UUID removeProperty(String clusterName, String propertyName);
}
