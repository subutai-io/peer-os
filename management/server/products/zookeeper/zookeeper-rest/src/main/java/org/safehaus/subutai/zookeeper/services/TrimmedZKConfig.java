/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.zookeeper.services;


import java.util.Set;


/**
 * @author dilshat
 */
public class TrimmedZKConfig {

	private String clusterName = "";
	private int numberOfNodes = 3;
	private Set<String> nodes;
	private boolean isStandalone;


	public String getClusterName() {
		return clusterName;
	}


	public int getNumberOfNodes() {
		return numberOfNodes;
	}


	public Set<String> getNodes() {
		return nodes;
	}


	public boolean isStandalone() {
		return isStandalone;
	}
}
