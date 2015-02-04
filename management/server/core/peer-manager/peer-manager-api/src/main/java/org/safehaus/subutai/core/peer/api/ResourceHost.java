package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.strategy.api.ServerMetric;


/**
 * Resource host interface.
 */
public interface ResourceHost extends Host
{

    void prepareTemplates( List<Template> templates ) throws ResourceHostException;

    void prepareTemplate( Template p ) throws ResourceHostException;

    boolean templateExists( Template template ) throws ResourceHostException;

    void importTemplate( Template template ) throws ResourceHostException;

    void updateRepository( Template template ) throws ResourceHostException;

    public ServerMetric getMetric() throws ResourceHostException;

    public Set<ContainerHost> getContainerHosts();

    public ContainerHost getContainerHostByName( String hostname );

    public ContainerHost getContainerHostById( String id );

    public boolean startContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    public boolean stopContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    public void destroyContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    public void removeContainerHost( ContainerHost result ) throws ResourceHostException;

    public void queue( HostTask hostTask );

    public void cloneContainer( String templateName, String hostname ) throws ResourceHostException;

    public ContainerHost createContainer( String templateName, String hostname, int timeout )
            throws ResourceHostException;
}
