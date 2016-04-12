package io.subutai.core.localpeer.impl.container;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.HostUtil;


public class ImportTemplateTask extends HostUtil.Task<Object>
{
    private final String templateName;
    private final ResourceHost resourceHost;


    public ImportTemplateTask( final String templateName, final ResourceHost resourceHost )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ) );
        Preconditions.checkNotNull( resourceHost );

        this.templateName = templateName;
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
        return String.format( "Import %s", templateName );
    }


    @Override
    public Object call() throws Exception
    {
        resourceHost.importTemplate( templateName );

        return null;
    }
}
