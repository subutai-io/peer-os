/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.solr;

import org.safehaus.subutai.common.protocol.ApiBase;

import java.util.UUID;

/**
 * @author dilshat
 */
public interface Solr extends ApiBase<Config> {

	public UUID startNode(String clusterName, String lxcHostname);

	public UUID stopNode(String clusterName, String lxcHostname);

	public UUID checkNode(String clusterName, String lxcHostname);

	public UUID addNode(String clusterName);

	public UUID destroyNode(String clusterName, String lxcHostname);

}
