package org.safehaus.subutai.core.environment.rest;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.environment.api.helper.EnvironmentStatusEnum;


/**
 * Trimmed environment for REST
 */
public class EnvironmentJson
{
    private UUID id;
    private String name;
    private EnvironmentStatusEnum status;
    private String publicKey;
    private Set<ContainerJson> containers;


    public EnvironmentJson( final UUID id, final String name, final EnvironmentStatusEnum status,
                            final String publicKey, final Set<ContainerJson> containers )
    {
        this.id = id;
        this.name = name;
        this.status = status;
        this.publicKey = publicKey;
        this.containers = containers;
    }
}
