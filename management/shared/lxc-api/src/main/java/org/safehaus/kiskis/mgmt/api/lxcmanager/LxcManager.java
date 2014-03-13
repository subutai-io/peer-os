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
 */
public interface LxcManager {

    public Map<Agent, Integer> getBestHostServers(int numberOfLxcsRequired);

    public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers();

    public boolean cloneLxcOnHost(Agent physicalAgent, String lxcHostname);
}
