/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;

/**
 * @author dilshat Wrapper object for proper JSON generation
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

    @Override
    public Response getResponse() {
        return response;
    }

    @Override
    public Request getRequest() {
        return command;
    }

}
