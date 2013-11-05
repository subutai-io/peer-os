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
public interface AgentFacade {

    public Agent getAgent(String uuid);

    public List<Agent> getAgents();

    public void removeAgent(String uuid);

    public void createAgent(String uuid);
    
    public int getAgentCount();
}
