package org.safehaus.agent.command.model;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * The request used to execute a command on the Kiskis Agent.
 *
 * @TODO - this is a work in progress ... just setting up for now
 */
public class ExecuteCommand extends AbstractRequest implements Request
{
    public static final String DEFAULT_CWD = "/";
    public static final String DEFAULT_RUN_AS = "root";
    public static final List<String> DEFAULT_ARGS = Collections.EMPTY_LIST;
    public static final Map<String,String> DEFAULT_ENV = Collections.EMPTY_MAP;

    private String cwd = DEFAULT_CWD;
    private StreamHandling stderr = StreamHandling.CAPTURE_AND_RETURN;
    private StreamHandling stdout = StreamHandling.CAPTURE_AND_RETURN;
    private final String program;
    private String runAs = DEFAULT_RUN_AS;
    private List<String> args = DEFAULT_ARGS;
    private Map<String,String> env = DEFAULT_ENV;


    public ExecuteCommand( String program, int requestSeqNum, UUID issuerId, UUID agentId, UUID messageId )
    {
        super( requestSeqNum, issuerId, agentId, messageId );

        this.program = program;
    }


    public String getCwd()
    {
        return cwd;
    }


    public StreamHandling getStderrHandling()
    {
        return stderr;
    }


    public StreamHandling getStdoutHandling()
    {
        return stdout;
    }


    public String getProgram()
    {
        return program;
    }


    public String getRunAs()
    {
        return runAs;
    }


    public List<String> getArgs()
    {
        return Collections.unmodifiableList( args );
    }


    public void addArg( String arg )
    {
        args.add( arg );
    }


    public void addArg( int index, String arg )
    {
        args.add( index, arg );
    }


    public void removeArg( String arg )
    {
        args.remove( arg );
    }


    public void removeArg( int index )
    {
        args.remove( index );
    }


    public Map<String,String> getEnv()
    {
        return Collections.unmodifiableMap( env );
    }


    public String getEnv( String var )
    {
        return env.get( var );
    }


    public String putEnv( String var, String val )
    {
        return env.put( var, val );
    }


    public String removeEnv( String var )
    {
        return env.remove( var );
    }


    public void clearEnv()
    {
        env.clear();
    }


    @Override
    public final Type getType()
    {
        return Type.EXECUTE_REQUEST;
    }
}
