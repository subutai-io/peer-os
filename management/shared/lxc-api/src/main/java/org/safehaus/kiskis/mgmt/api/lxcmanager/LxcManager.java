/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.lxcmanager;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 *
 * TODO: add async analogs
 */
public interface LxcManager {

    public Map<Agent, Integer> getPhysicalServersWithLxcSlots();

    public Map<Agent, ServerMetric> getPhysicalServerMetrics();

    public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers();

    public boolean cloneLxcOnHost(Agent physicalAgent, String lxcHostname);

    public boolean startLxcOnHost(Agent physicalAgent, String lxcHostname);

    public boolean stopLxcOnHost(Agent physicalAgent, String lxcHostname);

    public boolean destroyLxcOnHost(Agent physicalAgent, String lxcHostname);
}
