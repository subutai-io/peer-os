package org.safehaus.subutai.core.peer.impl.container;


import java.util.List;
import java.util.concurrent.Callable;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class ContainerCloneTask implements Callable<ContainerHost>
{
    private final ResourceHost resourceHost;
    private final String hostname;
    private final List<Template> templates;
    private final int timeoutSec;


    public ContainerCloneTask( final ResourceHost resourceHost, final String hostname, final List<Template> templates,
                               final int timeoutSec )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( templates ) );
        Preconditions.checkArgument( timeoutSec > 0 );

        this.resourceHost = resourceHost;
        this.hostname = hostname;
        this.templates = templates;
        this.timeoutSec = timeoutSec;
    }


    @Override
    public ContainerHost call() throws Exception
    {
        //prepare templates
        resourceHost.prepareTemplates( templates );

        //take last template as target template
        Template targetTemplate = templates.get( templates.size() - 1 );

        //create container
        return resourceHost.createContainer( targetTemplate.getTemplateName(), hostname, timeoutSec );
    }
}
