/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.spark.api;

import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ApiBase;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import java.util.UUID;

/**
 * @author dilshat
 */
public interface Spark extends ApiBase<SparkClusterConfig> {


	public UUID addSlaveNode(String clusterName, String lxcHostname);

	public UUID destroySlaveNode(String clusterName, String lxcHostname);

	public UUID changeMasterNode(String clusterName, String newMasterHostname, boolean keepSlave);

	/**
	 * Starts the specified node
	 *
	 * @param clusterName - name of cluster
	 * @param lxcHostName - hostname of node
	 * @param master      - specifies if this commands affects master or slave
	 *                    running on this node true - master, false - slave
	 * @return - UUID of operation to track
	 */
	public UUID startNode(String clusterName, String lxcHostName, boolean master);

	/**
	 * Stops the specified node
	 *
	 * @param clusterName - name of cluster
	 * @param lxcHostName - hostname of node
	 * @param master      - specifies if this commands affects master or slave
	 *                    running on this node true - master, false - slave
	 * @return - UUID of operation to track
	 */
	public UUID stopNode(String clusterName, String lxcHostName, boolean master);

	/**
	 * Checks status of the specified node
	 *
	 * @param clusterName - name of cluster
	 * @param lxcHostName - hostname of node
	 * @return - UUID of operation to track
	 */
	public UUID checkNode(String clusterName, String lxcHostName);

    public ClusterSetupStrategy getClusterSetupStrategy(ProductOperation po, SparkClusterConfig sparkClusterConfig);

    public ClusterSetupStrategy getClusterSetupStrategy( ProductOperation po, SparkClusterConfig prestoClusterConfig,
                                                         Environment environment );
}
