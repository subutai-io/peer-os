package io.subutai.core.environment.rest.ui.entity;


import java.util.Set;

import io.subutai.common.peer.ContainerSize;


/**
 * Trimmed container for REST
 */
public class ContainerDto
{
    private String id;
    private String name;
    private String environmentId;
    private String hostname;
    private String ip;
    private String templateName;
    private String templateId;
    private ContainerSize type;
    private String arch;
    private Set<String> tags;

    private String peerId;
    private String hostId;
    private boolean local;

    // Where environment of container created: subutai, hub
    private String dataSource;


    public ContainerDto( final String id, final String name, final String environmentId, final String hostname,
                         final String ip, final String templateName, final ContainerSize type, final String arch,
                         final Set<String> tags, final String peerId, final String hostId, boolean local,
                         String className, final String templateId )
    {
        this.id = id;
        this.name = name;
        this.environmentId = environmentId;
        this.hostname = hostname;
        this.ip = ip;
        this.templateName = templateName;
        this.type = type;
        this.arch = arch;
        this.tags = tags;
        this.peerId = peerId;
        this.hostId = hostId;
        this.local = local;

        this.templateId = templateId;

        dataSource = className.contains( "ProxyEnvironment" ) ? "hub" : "subutai";
    }
}
