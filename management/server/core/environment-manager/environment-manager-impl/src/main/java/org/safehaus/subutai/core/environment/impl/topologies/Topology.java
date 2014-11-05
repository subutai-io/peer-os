package org.safehaus.subutai.core.environment.impl.topologies;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.impl.environment.ContainerDistributionMessage;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;


/**
 * Created by bahadyr on 11/5/14.
 */
public abstract class Topology
{
    public final TemplateRegistry templateRegistry;


    protected Topology( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public abstract List<ContainerDistributionMessage> digestBlueprint( final EnvironmentBlueprint blueprint,
                                                                        UUID environmentId );


    /**
     * Fetches the template information required to build environment
     */
    public List<Template> fetchRequiredTempaltes( UUID sourcePeerId, final String templateName )
    {
        List<Template> requiredTemplates = new ArrayList<>();
        List<Template> templates = templateRegistry.getParentTemplates( templateName );

        Template installationTemplate = templateRegistry.getTemplate( templateName );
        if ( installationTemplate != null )
        {
            templates.add( installationTemplate );
        }


        for ( Template t : templates )
        {
            requiredTemplates.add( t.getRemoteClone( sourcePeerId ) );
        }

        return requiredTemplates;
    }
}
