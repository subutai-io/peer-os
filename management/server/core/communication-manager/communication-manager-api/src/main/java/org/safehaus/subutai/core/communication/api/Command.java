/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.communication.api;


import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;


/**
 * Implementations of this interface are wrappers for Request and Response objects. Used for serializing POJOs to json
 * commands for sending via communication manager.
 */
public interface Command {

    /**
     * Returns contained request object if any
     *
     * @return - returns wrapped request or null
     */
    public Request getRequest();

    /**
     * Returns contained response object if any
     *
     * @return - returns wrapped response or null
     */
    public Response getResponse();
}
