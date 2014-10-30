package org.safehaus.subutai.common.protocol.api;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;


/**
 * Created by talas on 10/30/14.
 */
public interface EnvironmentBlueprintService
{
    public EnvironmentBlueprint getEnvironmentBlueprint( long id );

    public List<EnvironmentBlueprint> getEnvironmentBlueprintsAgents();

    public void deleteEnvironmentBlueprint( long id );

    public EnvironmentBlueprint createEnvironmentBlueprint( EnvironmentBlueprint agent );
}
