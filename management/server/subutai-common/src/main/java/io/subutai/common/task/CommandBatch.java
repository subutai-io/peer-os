package io.subutai.common.task;


import java.util.ArrayList;
import java.util.List;

import io.subutai.common.util.JsonUtil;


/**
 * Commands batch
 */
public class CommandBatch
{
    public enum Type
    {
        STANDARD, CHAIN, JSON
    }


    private Type type;


    List<Command> commands = new ArrayList<>();


    public CommandBatch( final Type type )
    {
        this.type = type;
    }


    public CommandBatch( final Command command )
    {
        this.type = Type.STANDARD;
        commands.add( command );
    }


    public void addCommand( Command comm )
    {
        if ( this.type == Type.STANDARD && !commands.isEmpty() )
        {
            throw new IllegalArgumentException( "Standard command already contains a command" );
        }

        commands.add( comm );
    }


    @Override
    public String toString()
    {
        if ( commands.isEmpty() )
        {
            throw new IllegalStateException( "There is no command in command batch" );
        }

        if ( type == Type.JSON )
        {
            return String.format( "subutai batch '%s'", asJson() );
        }
        else
        {
            return asChain();
        }
    }


    public String asJson()
    {
        return JsonUtil.toJson( commands );
    }


    public String asChain()
    {
        StringBuilder sb = new StringBuilder();
        for ( Command command : commands )
        {
            sb.append( "&& subutai " ).append( command.getAction() );
            for ( String arg : command.getArguments() )
            {
                sb.append( " " ).append( arg );
            }
        }
        return sb.toString().substring( 3 );
    }
}
