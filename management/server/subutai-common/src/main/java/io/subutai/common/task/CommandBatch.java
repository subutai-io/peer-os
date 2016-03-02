package io.subutai.common.task;


import java.util.ArrayList;
import java.util.List;

import io.subutai.common.util.JsonUtil;


/**
 * Commands batch
 */
public class CommandBatch
{
    enum Type
    {
        STANDARD, CHAIN, JSON;
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
        if ( this.type == Type.STANDARD && commands.size() > 0 )
        {
            throw new IllegalArgumentException( "Standrad command already contain command." );
        }

        commands.add( comm );
    }


    @Override
    public String toString()
    {
        if ( commands.size() < 1 )
        {
            throw new IllegalStateException( "There is no command in command batch." );
        }

        if ( type == Type.JSON )
        {
            return JsonUtil.toJson( commands );
        }
        else
        {
            return asChain();
        }
    }


    private String asChain()
    {
        StringBuilder sb = new StringBuilder();
        for ( Command command : commands )
        {
            sb.append( "&& subutai " + command.getName() );
            for ( String arg : command.getArguments() )
            {
                sb.append( " " + arg );
            }
        }
        return sb.toString().substring( 3 );
    }
}
