package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;


/**
 * Created by bahadyr on 11/5/14.
 */
public interface ContainerDisributor
{

    List<ContainerDistributionMessage> distributeContainersAcrossPeers();
}
