package org.safehaus.subutai.api.container;


import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.shared.protocol.Agent;


public interface ContainerManager {

    public Set<Agent> clone( UUID envId, String templateName, int nodesCount, Collection<Agent> hosts,
                             PlacementStrategyENUM... strategy ) throws LxcCreateException;

    public Set<Agent> clone( String templateName, int nodesCount, Collection<Agent> hosts,
                             PlacementStrategyENUM... strategy ) throws LxcCreateException;

    public boolean attachAndExecute( Agent physicalHost, String cloneName, String cmd );

    public boolean attachAndExecute( Agent physicalHost, String cloneName, String cmd, long t, TimeUnit unit );

    public void cloneDestroy( String hostName, String cloneName ) throws LxcDestroyException;

    public void cloneDestroyByHostname( Set<String> cloneNames ) throws LxcDestroyException;
}
