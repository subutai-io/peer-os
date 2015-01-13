package org.safehaus.subutai.core.environment.impl.builder;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;


public abstract class EnvironmentBuildProcessBuilder
{

    EnvironmentManagerImpl environmentManager;


    public EnvironmentBuildProcessBuilder( final EnvironmentManagerImpl environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public abstract EnvironmentBuildProcess prepareBuildProcess( TopologyData topologyData )
            throws ProcessBuilderException;


    public List<Template> fetchRequiredTemplates( UUID sourcePeerId, final String templateName )
            throws ProcessBuilderException
    {
        List<Template> requiredTemplates = new ArrayList<>();
        List<Template> templates = environmentManager.getTemplateRegistry().getParentTemplates( templateName );

        Template installationTemplate = environmentManager.getTemplateRegistry().getTemplate( templateName );
        if ( installationTemplate != null )
        {
            templates.add( installationTemplate );
        }
        else
        {
            throw new ProcessBuilderException( String.format( "Template %s is not found in registry", templateName ) );
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


    protected CloneContainersMessage makeContainerCloneMessage( NodeGroup nodeGroup, UUID peerId ) throws ProcessBuilderException
    {
        UUID localPeerId = environmentManager.getPeerManager().getLocalPeer().getId();
        List<Template> templates = fetchRequiredTemplates( localPeerId, nodeGroup.getTemplateName() );
        if ( templates.isEmpty() )
        {
            throw new ProcessBuilderException( "No templates provided" );
        }

        CloneContainersMessage ccm = new CloneContainersMessage();
        ccm.setTargetPeerId( peerId );
        ccm.setNodeGroupName( nodeGroup.getName() );
        ccm.setNumberOfNodes( nodeGroup.getNumberOfNodes() );
        ccm.setStrategy( nodeGroup.getPlacementStrategy() );
        ccm.setTemplates( templates );
        return ccm;
    }
}

