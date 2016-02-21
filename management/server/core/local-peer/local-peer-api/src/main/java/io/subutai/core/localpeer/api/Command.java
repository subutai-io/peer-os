package io.subutai.core.localpeer.api;


import java.util.ArrayList;
import java.util.List;


/**
 * Batch action
 */
public class Command
{
    private String name;
    private List<String> args = new ArrayList<>();


    public Command( final String name, final List<String> args )
    {
        this.name = name;
        this.args = args;
    }


    public Command( final String name )
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }


    public void addArgument( final String arg )
    {
        this.args.add( arg );
    }
}
