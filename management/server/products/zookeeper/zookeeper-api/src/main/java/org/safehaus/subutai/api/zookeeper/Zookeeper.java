/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.zookeeper;

import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;

/**
 * @author dilshat
 */
public interface Zookeeper extends ApiBase<Config> {

	public UUID startNode(String clusterName, String lxcHostname);

	public UUID stopNode(String clusterName, String lxcHostname);

	public UUID checkNode(String clusterName, String lxcHostname);

	public UUID addNode(String clusterName);

	public UUID addNode(String clusterName, String lxcHostname);

	public UUID destroyNode(String clusterName, String lxcHostname);

	public UUID addProperty(String clusterName, String fileName, String propertyName, String propertyValue);

	public UUID removeProperty(String clusterName, String fileName, String propertyName);

	public UUID install(String hostName);

    public UUID start(String hostName);

}

