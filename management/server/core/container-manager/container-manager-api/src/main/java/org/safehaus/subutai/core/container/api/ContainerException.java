/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.api;


/**
 * Exception which can be thrown while destroying containers
 */
public class ContainerException extends Exception {

	public ContainerException( String message ) {
		super(message);
	}
}
