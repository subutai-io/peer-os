package org.safehaus.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;


/**
 * Created by bahadyr on 9/9/14.
 */
public class EnvironmentBuildTask
{

    private EnvironmentBlueprint environmentBlueprint;
    private Set<String> physicalNodes;


    public EnvironmentBuildTask()
    {
        this.physicalNodes = new HashSet<>();
    }


    public EnvironmentBlueprint getEnvironmentBlueprint()
    {
        return environmentBlueprint;
    }


    public void setEnvironmentBlueprint( final EnvironmentBlueprint environmentBlueprint )
    {
        this.environmentBlueprint = environmentBlueprint;
    }


    public Set<String> getPhysicalNodes()
    {
        return physicalNodes;
    }


    public void setPhysicalNodes( final Set<String> physicalNodes )
    {
        this.physicalNodes = physicalNodes;
    }
}
