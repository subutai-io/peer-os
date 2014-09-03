/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.impl.lxcmanager;


import org.safehaus.subutai.common.protocol.Agent;


/**
 * Contains lxc info for LxcActor
 */
public class LxcInfo {

	private final Agent physicalAgent;
	private final String lxcHostname;
	private final String nodeType;
	private boolean result;


	public LxcInfo(Agent physicalAgent, String lxcHostname, String nodeType) {
		this.physicalAgent = physicalAgent;
		this.lxcHostname = lxcHostname;
		this.nodeType = nodeType;
	}


	public String getNodeType() {
		return nodeType;
	}


	public boolean isResult() {
		return result;
	}


	public void setResult(boolean result) {
		this.result = result;
	}


	public Agent getPhysicalAgent() {
		return physicalAgent;
	}


	public String getLxcHostname() {
		return lxcHostname;
	}
}
