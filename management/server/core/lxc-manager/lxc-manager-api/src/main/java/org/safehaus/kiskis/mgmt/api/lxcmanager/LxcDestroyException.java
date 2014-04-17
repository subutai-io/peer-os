/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.lxcmanager;

/**
 * Exception which can be thrown while destroying lxcs
 *
 * @author dilshat
 */
public class LxcDestroyException extends Exception {

    public LxcDestroyException(String message) {
        super(message);
    }

}
