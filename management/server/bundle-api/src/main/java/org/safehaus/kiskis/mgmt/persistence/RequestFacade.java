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
public interface RequestFacade {

    public Request getRequest(long id);

    public Request getRequest(String uuid, long requestSequenceNumber);

    public List<Request> getRequests(String uuid, int limit);

    public void removeRequest(long id);

    public void createRequest(String uuid, long requestSequenceNumber, String body);

    public int getRequestCount();
}
