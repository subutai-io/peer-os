/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;

/**
 * @author dilshat
 *         Wrapper object for proper JSON generation
 */
public class CommandImpl implements Command {

    Request command;
    Response response;

    public CommandImpl(Object message) {
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

    public Request getRequest() {
        return command;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

}
