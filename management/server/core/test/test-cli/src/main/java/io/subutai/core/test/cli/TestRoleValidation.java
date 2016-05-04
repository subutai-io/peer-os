package io.subutai.core.test.cli;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfo;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


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
            List<Relation> relationList = relationManager.getRelations();
            for ( final Relation relation : relationList )
            {
                System.out.println( String.format( "%s %s", relation.getId(), relation.getRelationStatus() ) );
                RelationInfo relationInfo = relation.getRelationInfo();
                for ( final Map.Entry<String, String> entry : relationInfo.getRelationTraits().entrySet() )
                {
                    String keyValue = String.format( "\t%-24s => %s", entry.getKey(), entry.getValue() );
                    System.out.println( keyValue );
                }
                String links = String.format( "\t%s, %s, %s", relation.getSource().getLinkId(), relation.getTarget().getLinkId(),
                        relation.getTrustedObject().getLinkId() );
                System.out.println( links );
            }
            System.out.println();
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
