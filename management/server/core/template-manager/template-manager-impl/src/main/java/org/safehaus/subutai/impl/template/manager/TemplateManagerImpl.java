package org.safehaus.subutai.impl.template.manager;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.common.protocol.Agent;


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
    public boolean clone( String hostName, String templateName, String cloneName )
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        if ( !prepareTemplates( a, templateName ) )
        {
            return false;
        }

        return scriptExecutor.execute( a, ActionType.CLONE, templateName, cloneName, " &" );
        // TODO: script does not return w/o redirecting outputs!!!
        // for now, script is run in background
    }


    @Override
    public boolean clone( String hostName, String templateName, Set<String> cloneNames )
    {
        Agent a = agentManager.getAgentByHostname( hostName );
        if ( !prepareTemplates( a, templateName ) )
        {
            return false;
        }

        boolean result = true;
        for ( String cloneName : cloneNames )
        {
            result &= scriptExecutor.execute( a, ActionType.CLONE, templateName, cloneName, "&" );
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

        Command cmd = getCommandRunner().createCommand(
            new RequestBuilder( cmdBuilder.toString() ).withTimeout( 180 ),
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
                    logger.log(Level.SEVERE, "Failed to install parent templates: {0}", p.getTemplateName());
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
        String s = ActionType.GET_PACKAGE_NAME.buildCommand(templateName);
        s = ActionType.wrapInBash(s);
        for(Agent phy : phys) {

            Command cmd = commandRunner.createCommand(new RequestBuilder(s),
                    new HashSet<>(Arrays.asList(phy)));
            commandRunner.runCommand(cmd);

            if(cmd.hasSucceeded())
                return cmd.getResults().get(phy.getUuid()).getStdOut().trim();
        }
        return null;
    }

    @Override
    public String getDebianPackageName(String templateName) {
        Set<Agent> phys = agentManager.getPhysicalAgents();
        if(phys.isEmpty()) {
            logger.severe("No physical agents connected");
            return null;
        }
        // run on each physical server one by one until we get successful result
        String s = ActionType.GET_DEB_PACKAGE_NAME.buildCommand(templateName);
        s = ActionType.wrapInBash(s);
        for(Agent phy : phys) {
            Command cmd = commandRunner.createCommand(new RequestBuilder(s),
                    new HashSet<>(Arrays.asList(phy)));
            commandRunner.runCommand(cmd);

            if(cmd.hasSucceeded())
                return cmd.getResults().get(phy.getUuid()).getStdOut().trim();
        }
        return null;
    }

    private boolean prepareTemplates( Agent a, String templateName )
    {
        // check parents first
        List<Template> parents = templateRegistry.getParentTemplates( templateName );
        for ( Template p : parents )
        {
            boolean exists = scriptExecutor.execute( a, ActionType.LIST_TEMPLATES, p.getTemplateName() );
            if ( !exists )
            {
                String pack = getPackageName( p.getTemplateName() );
                boolean b = scriptExecutor.execute( a, ActionType.INSTALL, pack );
                if ( !b )
                {
                    logger.log(Level.SEVERE, "Failed to install parent templates: {0}", p.getTemplateName());
                    return false;
                }
            }
        }
        // check template itself
        if ( !scriptExecutor.execute( a, ActionType.LIST_TEMPLATES, templateName ) )
        {
            String pack = getPackageName( templateName );
            if ( !scriptExecutor.execute( a, ActionType.INSTALL, pack ) )
            {
                logger.log(Level.SEVERE, "Failed to install template: {0}", templateName);
                return false;
            }
        }
        return true;
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

}
