/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.presto;

import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;

/**
 * @author dilshat
 */
public interface Presto extends ApiBase<Config> {

	public UUID addWorkerNode(String clusterName, String lxcHostname);

	public UUID destroyWorkerNode(String clusterName, String lxcHostname);

	public UUID changeCoordinatorNode(String clusterName, String newMasterHostname);

	/**
	 * Starts the specified node
	 *
	 * @param clusterName - name of cluster
	 * @param lxcHostName - hostname of node
	 * @return - UUID of operation to track
	 */
	public UUID startNode(String clusterName, String lxcHostName);

	/**
	 * Stops the specified node
	 *
	 * @param clusterName - name of cluster
	 * @param lxcHostName - hostname of node
	 * @return - UUID of operation to track
	 */
	public UUID stopNode(String clusterName, String lxcHostName);

	/**
	 * Checks status of the specified node
	 *
	 * @param clusterName - name of cluster
	 * @param lxcHostName - hostname of node
	 * @return - UUID of operation to track
	 */
	public UUID checkNode(String clusterName, String lxcHostName);
}
