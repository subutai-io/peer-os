/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.communicationmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 * Implementations of this interface are wrappers for Request and Response
 * objects. Used for serializing POJOs to json commands for sending via
 * communication manager.
 *
 * @author dilshat
 */
public interface Command {

    /**
     * Returns contained request object if any
     *
     * @return
     */
    public Request getRequest();

    /**
     * Returns contained response object if any
     *
     * @return
     */
    public Response getResponse();
}
