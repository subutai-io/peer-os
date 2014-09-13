/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.api;


/**
 * Possible states of containermanager
 */
public enum ContainerState {

	UNKNOWN, RUNNING, STOPPED, FROZEN;


	public static ContainerState parseState(String state) {
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
