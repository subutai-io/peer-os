/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.mongodb;

import org.safehaus.subutai.common.protocol.ApiBase;

import java.util.UUID;

/**
 * @author dilshat
 */
public interface Mongo extends ApiBase<Config> {

	/**
	 * adds node to the specified cluster
	 *
	 * @param clusterName - name of cluster
	 * @param nodeType    - type of node to add
	 * @return - UUID of operation to track
	 */
	public UUID addNode(String clusterName, NodeType nodeType);

	/**
	 * destroys node in the specified cluster
	 *
	 * @param clusterName - name of cluster
	 * @param lxcHostName - hostname of node
	 * @return - UUID of operation to track
	 */
	public UUID destroyNode(String clusterName, String lxcHostName);

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
