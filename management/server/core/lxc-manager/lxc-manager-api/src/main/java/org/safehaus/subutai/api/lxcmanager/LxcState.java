/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.lxcmanager;


/**
 * Possible states of lxc
 */
public enum LxcState {

	UNKNOWN, RUNNING, STOPPED, FROZEN;


	public static LxcState parseState(String state) {
		if (RUNNING.name().equalsIgnoreCase(state)) {
			return RUNNING;
		} else if (STOPPED.name().equalsIgnoreCase(state)) {
			return STOPPED;
		} else if (FROZEN.name().equalsIgnoreCase(state)) {
			return FROZEN;
		}

		return UNKNOWN;
	}
}
