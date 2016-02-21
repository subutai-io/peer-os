package io.subutai.core.localpeer.api;


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
}
