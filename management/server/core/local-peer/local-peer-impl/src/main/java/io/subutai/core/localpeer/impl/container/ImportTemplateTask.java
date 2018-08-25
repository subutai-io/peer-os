package io.subutai.core.localpeer.impl.container;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Template;
import io.subutai.common.util.HostUtil;


public class ImportTemplateTask extends HostUtil.Task<Object>
{
    private final Template template;
    private final ResourceHost resourceHost;
    private final String environmentId;
    private final String token;


    public ImportTemplateTask( final Template template, final ResourceHost resourceHost, final String environmentId,
                               final String cdnToken )
    {
        Preconditions.checkNotNull( template );
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        this.template = template;
        this.resourceHost = resourceHost;
        this.environmentId = environmentId;
        this.token = cdnToken;
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
        resourceHost.importTemplate( template, environmentId );

        return null;
    }
}
