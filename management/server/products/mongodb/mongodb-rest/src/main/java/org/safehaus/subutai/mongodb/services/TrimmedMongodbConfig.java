/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.mongodb.services;


import org.safehaus.subutai.common.settings.Common;


/**
 * @author dilshat
 */
public class TrimmedMongodbConfig {

	private String clusterName = "";
	private String replicaSetName = "repl";
	private String domainName = Common.DEFAULT_DOMAIN_NAME;
	private int numberOfConfigServers = 3;
	private int numberOfRouters = 2;
	private int numberOfDataNodes = 3;
	private int cfgSrvPort = 27019;
	private int routerPort = 27018;
	private int dataNodePort = 27017;


	public String getClusterName() {
		return clusterName;
	}


	public String getReplicaSetName() {
		return replicaSetName;
	}


	public String getDomainName() {
		return domainName;
	}


	public int getNumberOfConfigServers() {
		return numberOfConfigServers;
	}


	public int getNumberOfRouters() {
		return numberOfRouters;
	}


	public int getNumberOfDataNodes() {
		return numberOfDataNodes;
	}


	public int getCfgSrvPort() {
		return cfgSrvPort;
	}


	public int getRouterPort() {
		return routerPort;
	}


	public int getDataNodePort() {
		return dataNodePort;
	}
}
