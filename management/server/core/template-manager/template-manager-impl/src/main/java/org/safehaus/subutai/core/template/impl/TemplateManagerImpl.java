package org.safehaus.subutai.core.template.impl;


import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.template.api.ActionType;
import org.safehaus.subutai.core.template.api.TemplateException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class TemplateManagerImpl extends TemplateManagerBase
{

    private static final Logger logger = Logger.getLogger( TemplateManagerImpl.class.getName() );


    @Override
    public String getMasterTemplateName()
    {
        return "master";
    }


    @Override
    public boolean setup( String hostName )
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        return scriptExecutor.execute( a, ActionType.SETUP, 15, TimeUnit.MINUTES );
    }


    @Override
    public boolean clone( String hostName, String templateName, String cloneName, String environmentId )
            throws TemplateException
    {
        return clone( hostName, templateName, Sets.newHashSet( cloneName ), environmentId );
    }


    @Override
    public boolean clone( String hostName, String templateName, Set<String> cloneNames, String environmentId )
            throws TemplateException
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        prepareTemplates( a, templateName );

        boolean result = true;
        for ( String cloneName : cloneNames )
        {
            result &= scriptExecutor.execute( a, ActionType.CLONE, templateName, cloneName, environmentId );
            // TODO: script does not return w/o redirecting outputs!!!
            // for now, script is run in background
        }
        return result;
    }


    @Override
    public boolean cloneDestroy( String hostName, String cloneName )
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        return scriptExecutor.execute( a, ActionType.DESTROY, cloneName );
    }


    @Override
    public boolean cloneDestroy( String hostName, Set<String> cloneNames )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostName ), "Host server name is null or empty" );
        Preconditions.checkArgument( cloneNames != null && !cloneNames.isEmpty(), "Clone names is null or empty" );

        Agent a = agentManager.getAgentByHostname( hostName );

        StringBuilder cmdBuilder = new StringBuilder();
        for ( String cloneName : cloneNames )
        {
            if ( cmdBuilder.length() > 0 )
            {
                cmdBuilder.append( " & " );
            }
            cmdBuilder.append( ActionType.DESTROY.buildCommand( cloneName ) );
        }

        Command cmd = getCommandRunner().createCommand( new RequestBuilder( cmdBuilder.toString() ).withTimeout( 180 ),
                new HashSet<>( Arrays.asList( a ) ) );
        getCommandRunner().runCommand( cmd );

        return cmd.hasSucceeded();
    }


    @Override
    public boolean cloneRename( String hostName, String oldName, String newName )
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        return scriptExecutor.execute( a, ActionType.RENAME, oldName, newName );
    }


    @Override
    public boolean promoteClone( String hostName, String cloneName )
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        return scriptExecutor.execute( a, ActionType.PROMOTE, cloneName );
    }


    @Override
    public boolean promoteClone( String hostName, String cloneName, String newName, boolean copyit )
    {
        List<String> args = new ArrayList<>();
        if ( newName != null && newName.length() > 0 )
        {
            args.add( "-n " + newName );
        }
        if ( copyit )
        {
            args.add( "-c" );
        }
        args.add( cloneName );
        String[] arr = args.toArray( new String[args.size()] );

        Agent a = agentManager.getAgentByHostname( hostName );
        return scriptExecutor.execute( a, ActionType.PROMOTE, arr );
    }


    @Override
    public boolean importTemplate( String hostName, String templateName )
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        // check parents first
        List<Template> parents = templateRegistry.getParentTemplates( templateName );
        for ( Template p : parents )
        {
            boolean temp_exists = scriptExecutor.execute( a, ActionType.LIST_TEMPLATES, p.getTemplateName() );
            if ( !temp_exists )
            {
                boolean installed = scriptExecutor.execute( a, ActionType.IMPORT, p.getTemplateName() );
                if ( !installed )
                {
                    logger.log( Level.SEVERE, "Failed to install parent templates: {0}", p.getTemplateName() );
                    return false;
                }
            }
        }
        return scriptExecutor.execute( a, ActionType.IMPORT, templateName );
    }


    @Override
    public String exportTemplate( String hostName, String templateName )
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        boolean b = scriptExecutor.execute( a, ActionType.EXPORT, templateName );
        if ( b )
        {
            return getExportedPackageFilePath( a, templateName );
        }
        return null;
    }


    @Override
    public String getPackageName( String templateName )
    {

        Set<Agent> phys = agentManager.getPhysicalAgents();
        if ( phys.isEmpty() )
        {
            logger.severe( "No physical agents connected" );
            return null;
        }
        // run on each physical server one by one until we get successful result
        String s = ActionType.GET_PACKAGE_NAME.buildCommand( templateName );
        s = ActionType.wrapInBash( s );
        for ( Agent phy : phys )
        {

            Command cmd = commandRunner.createCommand( new RequestBuilder( s ), new HashSet<>( Arrays.asList( phy ) ) );
            commandRunner.runCommand( cmd );

            if ( cmd.hasSucceeded() )
            {
                return cmd.getResults().get( phy.getUuid() ).getStdOut().trim();
            }
        }
        return null;
    }


    @Override
    public String getDebianPackageName( String templateName )
    {
        Set<Agent> phys = agentManager.getPhysicalAgents();
        if ( phys.isEmpty() )
        {
            logger.severe( "No physical agents connected" );
            return null;
        }
        // run on each physical server one by one until we get successful result
        String s = ActionType.GET_DEB_PACKAGE_NAME.buildCommand( templateName );
        s = ActionType.wrapInBash( s );
        for ( Agent phy : phys )
        {
            Command cmd = commandRunner.createCommand( new RequestBuilder( s ), new HashSet<>( Arrays.asList( phy ) ) );
            commandRunner.runCommand( cmd );

            if ( cmd.hasSucceeded() )
            {
                return cmd.getResults().get( phy.getUuid() ).getStdOut().trim();
            }
        }
        return null;
    }


    private String getExportedPackageFilePath( Agent a, String templateName )
    {
        Set<Agent> set = new HashSet<>( Arrays.asList( a ) );
        Command cmd = commandRunner.createCommand( new RequestBuilder( "echo $SUBUTAI_TMPDIR" ), set );
        commandRunner.runCommand( cmd );
        AgentResult res = cmd.getResults().get( a.getUuid() );
        if ( res.getExitCode() != null && res.getExitCode() == 0 )
        {
            String dir = res.getStdOut();
            String s = ActionType.GET_DEB_PACKAGE_NAME.buildCommand( templateName );
            cmd = commandRunner.createCommand( new RequestBuilder( s ), set );
            commandRunner.runCommand( cmd );
            if ( cmd.hasSucceeded() )
            {
                res = cmd.getResults().get( a.getUuid() );
                return Paths.get( dir, res.getStdOut() ).toString();
            }
            else
            { // TODO: to be removed
                templateName = templateName + "-subutai-template.deb";
                return Paths.get( dir, templateName ).toString();
            }
        }
        return null;
    }


    protected void prepareTemplates( Agent a, String templateName ) throws TemplateException
    {
        // check parents first
        List<Template> parents = templateRegistry.getParentTemplates( templateName );
        for ( Template p : parents )
        {
            checkTemplate( a, p );
        }

        Template p = templateRegistry.getTemplate( templateName );
        checkTemplate( a, p );
    }


    private void checkTemplate( final Agent a, final Template p ) throws TemplateException
    {

        if ( isTemplateReady( a, p ) )
        {
            return;
        }

        importTemplate( a, p );
        if ( isTemplateReady( a, p ) )
        {
            return;
        }
        // trying add repository
        updateRepository( a, p );
        importTemplate( a, p );
        if ( !isTemplateReady( a, p ) )
        {
            throw new TemplateException(
                    String.format( "Could not prepare template %s on %s", p.getTemplateName(), a.getHostname() ) );
        }
    }


    protected boolean isTemplateReady( final Agent a, final Template p )
    {
        return scriptExecutor.execute( a, ActionType.LIST_TEMPLATES, p.getTemplateName() );
    }


    protected void importTemplate( Agent agent, Template template ) throws TemplateException
    {
        scriptExecutor.execute( agent, ActionType.IMPORT, template.getTemplateName() );
    }


    //    private String getPackageName( final Template template )
    //    {
    //        return String.format( "%s-subutai-template", template.getTemplateName() );
    //    }


    protected void updateRepository( Agent agent, Template template )
    {
        if ( template.isRemote() )
        {
            scriptExecutor.execute( agent, ActionType.ADD_SOURCE, template.getPeerId().toString() );
            scriptExecutor.execute( agent, ActionType.APT_GET_UPDATE );
        }
    }
}
