package org.safehaus.subutai.core.packge.impl.handler;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.packge.api.PackageInfo;
import org.safehaus.subutai.core.packge.impl.DebPackageManager;
import org.safehaus.subutai.core.packge.impl.info.DebPackageInfo;
import org.safehaus.subutai.core.packge.impl.info.PackageFlag;
import org.safehaus.subutai.core.packge.impl.info.PackageState;
import org.safehaus.subutai.core.packge.impl.info.SelectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ListHandler extends AbstractHandler<Collection<PackageInfo>>
{
    private static final Logger LOG = LoggerFactory.getLogger( ListHandler.class.getName() );

    private Pattern lineStartPattern = Pattern.compile( "^[a-z]{2,3}\\s+" );
    private String namePattern;
    private boolean fromFile;


    public ListHandler( DebPackageManager pm, String hostname )
    {
        super( pm, hostname );
    }


    public String getNamePattern()
    {
        return namePattern;
    }


    public void setNamePattern( String namePattern )
    {
        this.namePattern = namePattern;
    }


    public boolean isFromFile()
    {
        return fromFile;
    }


    public void setFromFile( boolean fromFile )
    {
        this.fromFile = fromFile;
    }


    @Override
    Logger getLogger()
    {
        return LoggerFactory.getLogger( ListHandler.class );
    }


    @Override
    public Collection<PackageInfo> performAction()
    {
        Agent agent = getAgent();
        if ( agent != null )
        {
            AgentRequestBuilder rb = new AgentRequestBuilder( agent, makeCommand() );
            Command cmd = packageManager.getCommandRunner().createCommand( new HashSet<>( Arrays.asList( rb ) ) );
            packageManager.getCommandRunner().runCommand( cmd );
            if ( cmd.hasSucceeded() )
            {
                Collection<PackageInfo> ls = new ArrayList<>();
                AgentResult res = cmd.getResults().get( agent.getUuid() );
                String s = res.getStdOut();
                Pattern delim = Pattern.compile( "\\s+" );
                try ( BufferedReader br = new BufferedReader( new StringReader( s ) ) )
                {
                    int cols = 0;
                    while ( ( s = br.readLine() ) != null )
                    {
                        // count occurences of a seperator minus sign
                        // http://stackoverflow.com/questions/275944
                        if ( cols == 0 && s.startsWith( "+++" ) )
                        {
                            cols = 1 + s.length() - s.replace( "-", "" ).length();
                        }
                        PackageInfo pi = parseLine( s, cols, delim );
                        if ( pi != null )
                        {
                            ls.add( pi );
                        }
                    }
                }
                catch ( IOException ex )
                {
                    LOG.error( "Error in performAction", ex );
                }
                return ls;
            }
        }
        return Collections.emptyList();
    }


    private String makeCommand()
    {
        StringBuilder sb = new StringBuilder();
        if ( fromFile )
        {
            sb.append( "cd " ).append( packageManager.getLocation() ).append( " && " );
            sb.append( "cat " ).append( packageManager.getFilename() );
        }
        else
        {
            sb.append( "dpkg -l" );
            if ( namePattern != null && !namePattern.isEmpty() )
            {
                sb.append( " '" ).append( namePattern ).append( "'" );
            }
        }
        return sb.toString();
    }


    DebPackageInfo parseLine( String s, int columns, Pattern delim )
    {
        if ( !lineStartPattern.matcher( s ).find() )
        {
            return null;
        }
        String[] arr = delim.split( s, columns );
        DebPackageInfo p = null;
        if ( arr.length > columns - 1 )
        {
            char[] status = arr[0].toCharArray();
            p = new DebPackageInfo( arr[1], arr[2] );
            if ( columns == 5 )
            {
                p.setArch( arr[3] );
                p.setDescription( arr[4] );
            }
            else
            {
                p.setDescription( arr[3] );
            }
            p.setSelectionState( SelectionState.getByAbbrev( status[0] ) );
            p.setState( PackageState.getByAbbrev( status[1] ) );
            if ( status.length > 2 )
            {
                PackageFlag f = PackageFlag.getByAbbrev( status[2] );
                if ( f != null )
                {
                    p.getFlags().add( f );
                }
            }
        }
        return p;
    }
}
