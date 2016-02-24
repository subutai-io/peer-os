package io.subutai.common.task;


import java.util.ArrayList;
import java.util.List;

import io.subutai.common.util.JsonUtil;


/**
 * Commands batch
 */
public class CommandBatch
{
    List<Command> commands = new ArrayList<>();


    public void addCommand( Command comm )
    {
        commands.add( comm );
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
            sb.append( "&& subutai " + command.getName() );
            for ( String arg : command.getArguments() )
            {
                sb.append( " " + arg );
            }
        }
        return sb.toString().substring( 3 );
    }
}
