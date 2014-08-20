/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.lxcmanager;


/**
 * Exception which can be thrown while destroying lxcs
 */
public class LxcDestroyException extends Exception {

	public LxcDestroyException(String message) {
		super(message);
	}
}
