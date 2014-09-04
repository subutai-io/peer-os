/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.lxcmanager;


import org.safehaus.subutai.api.lxcmanager.LxcManager;

import java.util.concurrent.Callable;


/**
 * Clones or destroys lxc
 */
public class LxcActor implements Callable<LxcInfo> {

	private final LxcInfo info;
	private final LxcManager lxcManager;
	private final LxcAction lxcAction;


	public LxcActor(LxcInfo info, LxcManager lxcManager, LxcAction lxcAction) {
		this.info = info;
		this.lxcManager = lxcManager;
		this.lxcAction = lxcAction;
	}


	public LxcInfo call() throws Exception {
		if (lxcAction == LxcAction.CLONE) {
			info.setResult(lxcManager.cloneLxcOnHost(info.getPhysicalAgent(), info.getLxcHostname()));
		} else if (lxcAction == LxcAction.START) {
			info.setResult(lxcManager.startLxcOnHost(info.getPhysicalAgent(), info.getLxcHostname()));
		} else if (lxcAction == LxcAction.DESTROY) {
			info.setResult(lxcManager.destroyLxcOnHost(info.getPhysicalAgent(), info.getLxcHostname()));
		}
		return info;
	}
}
