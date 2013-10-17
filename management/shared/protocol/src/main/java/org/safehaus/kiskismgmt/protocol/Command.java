/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskismgmt.protocol;

/**
 *
 * @author dilshat
 */
public class Command implements ICommand {

    Request command;
    Response response;

    public Command(Object message) {
        if (message instanceof Request) {
            this.command = (Request) message;
        } else if (message instanceof Response) {
            this.response = (Response) message;
        }
    }
}
