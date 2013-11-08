/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandInterface;

/**
 *
 * @author dilshat
 * Wrapper object for proper JSON generation
 */
public class Command implements CommandInterface {

    Request request;
    Response response;

    public Command(Object message) {
        if (message instanceof Request) {
            this.request = (Request) message;
        } else if (message instanceof Response) {
            this.response = (Response) message;
        }
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request requst) {
        this.request = requst;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

}
