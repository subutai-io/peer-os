package io.subutai.core.test.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.object.relation.api.RelationManager;


@Command( scope = "test", name = "test", description = "test command" )
public class TestRoleValidation extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );

    private RelationManager relationManager;


    public void setRelationManager( final RelationManager relationManager )
    {
        this.relationManager = relationManager;
    }


    @Override
    public Object execute( final CommandSession session ) throws Exception
    {
        try
        {
            System.out.println( "Hello there!" );
            System.out.println( relationManager.getRelations() );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in test", e );
        }

        return null;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        return null;
    }
}
