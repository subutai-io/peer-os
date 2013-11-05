/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.persistence;

import java.util.List;

/**
 *
 * @author dilshat
 */
public interface ResponseFacade {

    public Response getResponse(long id);

    public Response getResponse(String uuid, long requestSequenceNumber, long responseSequenceNumber);

    public List<Response> getResponses(String uuid, long requestSequenceNumber, int limit);

    public void removeResponse(long id);

    public void createResponse(String uuid, long requestSequenceNumber, long responseSequenceNumber, String body);

    public int getResponseCount();
}
