/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskismgmt.protocol;

/**
 *
 * @author dilshat
 * Wrapper object for proper JSON generation
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

    public Request getCommand() {
        return command;
    }

    public void setCommand(Request command) {
        this.command = command;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

}
