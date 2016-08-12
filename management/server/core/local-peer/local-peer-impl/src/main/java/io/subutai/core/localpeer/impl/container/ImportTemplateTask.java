package io.subutai.core.localpeer.impl.container;


import com.google.common.base.Preconditions;

import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Template;
import io.subutai.common.util.HostUtil;


public class ImportTemplateTask extends HostUtil.Task<Object>
{
    private final Template template;
    private final ResourceHost resourceHost;


    public ImportTemplateTask( final Template template, final ResourceHost resourceHost )
    {
        Preconditions.checkNotNull( template );
        Preconditions.checkNotNull( resourceHost );

        this.template = template;
        this.resourceHost = resourceHost;
    }


    @Override
    public int maxParallelTasks()
    {
        return 0;
    }


    @Override
    public String name()
    {
        return String.format( "Import %s", template.getName() );
    }


    @Override
    public Object call() throws Exception
    {
        resourceHost.importTemplate( template );

        return null;
    }
}
