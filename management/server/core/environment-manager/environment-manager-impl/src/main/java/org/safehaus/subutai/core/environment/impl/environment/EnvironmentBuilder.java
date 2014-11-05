package org.safehaus.subutai.core.environment.impl.environment;


import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.impl.topologies.Topology;


/**
 * Created by bahadyr on 11/5/14.
 */
public interface EnvironmentBuilder
{

    public void build(EnvironmentBlueprint blueprint, Topology topology) throws BuildException;

}
