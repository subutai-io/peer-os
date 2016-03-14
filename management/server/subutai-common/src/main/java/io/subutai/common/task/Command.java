package io.subutai.common.task;


import java.util.ArrayList;
import java.util.List;


/**
 * Batch action
 */
public class Command
{
    private String action;
    private List<String> args = new ArrayList<>();


    public Command( final String action, final List<String> args )
    {
        this.action = action;
        this.args = args;
    }


    public Command( final String action )
    {
        this.action = action;
    }


    public String getAction()
    {
        return action;
    }


    public void addArgument( final String arg )
    {
        if ( arg == null )
        {
            throw new IllegalArgumentException( "Command argument could not be null." );
        }
        this.args.add( arg.trim() );
    }


    public List<String> getArguments()
    {
        return args;
    }
}
