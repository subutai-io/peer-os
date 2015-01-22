package org.safehaus.subutai.core.environment.rest;


import java.util.UUID;

import org.safehaus.subutai.common.host.ContainerHostState;


/**
 * Trimmed container for REST
 */
public class ContainerJson
{
    private UUID id;
    private UUID environmentId;
    private String hostname;
    private ContainerHostState state;
    private String ip;
    private String templateName;


    public ContainerJson( final UUID id, final UUID environmentId, final String hostname,
                          final ContainerHostState state, final String ip, final String templateName )
    {
        this.id = id;
        this.environmentId = environmentId;
        this.hostname = hostname;
        this.state = state;
        this.ip = ip;
        this.templateName = templateName;
    }
}
