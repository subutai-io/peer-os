package org.safehaus.subutai.core.environment.impl.builder;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;


/**
 * Created by bahadyr on 10/21/14.
 */
public abstract class EnvironmentBuildProcessFactory
{

    EnvironmentManagerImpl environmentManager;


    public EnvironmentBuildProcessFactory( final EnvironmentManagerImpl environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public abstract EnvironmentBuildProcess prepareBuildProcess( TopologyData topologyData )
            throws ProcessBuilderException;


    public List<Template> fetchRequiredTemplates( UUID sourcePeerId, final String templateName ) throws ProcessBuilderException
    {
        List<Template> requiredTemplates = new ArrayList<>();
        List<Template> templates = environmentManager.getTemplateRegistry().getParentTemplates( templateName );

        Template installationTemplate = environmentManager.getTemplateRegistry().getTemplate( templateName );
        if ( installationTemplate != null )
        {
            templates.add( installationTemplate );
        }


        for ( Template t : templates )
        {
            requiredTemplates.add( t.getRemoteClone( sourcePeerId ) );
        }

        if ( requiredTemplates.isEmpty() )
        {
            throw new ProcessBuilderException( "Can not fetch template information." );
        }

        return requiredTemplates;
    }
}
