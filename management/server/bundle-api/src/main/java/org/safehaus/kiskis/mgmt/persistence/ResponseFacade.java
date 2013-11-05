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

    public Response getResponse(String source, String uuid, long requestSequenceNumber, long responseSequenceNumber);

    public List<Response> getResponses(String source, String uuid, long requestSequenceNumber, boolean desc, int limit);

    public void removeResponse(long id);

    public void createResponse(String source, String uuid, long requestSequenceNumber, long responseSequenceNumber, String body);

    public int getResponseCount();
}
