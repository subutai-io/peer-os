package io.subutai.core.metric.impl.pojo;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

import io.subutai.core.metric.api.pojo.P2Pinfo;


public class P2PInfoPojo implements P2Pinfo
{
    private String rhId;
    private String rhName;
    private String rhVersion;
    private String p2pVersion;
    private int p2pStatus;
    private int p2pVersionCheck;
    private int rhVersionCheck;
    private List<String> state;
    private List<String> p2pErrorLogs;


    @Override
    public String getRhName()
    {
        return rhName;
    }


    public void setRhName( final String rhName )
    {
        this.rhName = rhName;
    }


    @Override
    public String getRhId()
    {
        return rhId;
    }


    public void setRhId( final String rhId )
    {
        this.rhId = rhId;
    }


    @Override
    public String getRhVersion()
    {
        return rhVersion;
    }


    public void setRhVersion( final String rhVersion )
    {
        this.rhVersion = rhVersion;
    }


    @Override
    public int getP2pStatus()
    {
        return p2pStatus;
    }


    public void setP2pStatus( final int p2pStatus )
    {
        this.p2pStatus = p2pStatus;
    }


    @Override
    public String getP2pVersion()
    {
        return p2pVersion;
    }


    public void setP2pVersion( final String p2pVersion )
    {
        this.p2pVersion = p2pVersion;
    }


    @Override
    public List<String> getState()
    {
        return state;
    }


    public void setState( final List<String> state )
    {
        this.state = state;
    }


    @Override
    public List<String> getP2pErrorLogs()
    {
        return p2pErrorLogs;
    }


    public void setP2pErrorLogs( final List<String> p2pErrorLogs )
    {
        this.p2pErrorLogs = p2pErrorLogs;
    }


    @Override
    public int getRhVersionCheck()
    {
        return rhVersionCheck;
    }


    public void setRhVersionCheck( final int rhVersionCheck )
    {
        this.rhVersionCheck = rhVersionCheck;
    }


    @Override
    public int getP2pVersionCheck()
    {
        return p2pVersionCheck;
    }


    public void setP2pVersionCheck( final int p2pVersionCheck )
    {
        this.p2pVersionCheck = p2pVersionCheck;
    }


    public void setP2pVersionCheck( final String from, final String to )
    {
        if ( !checkOutdatedVersion( this.p2pVersion, to ) )
        {
            setP2pVersionCheck( checkOutdatedVersion( this.p2pVersion, from ) ? 1 : 2 );
            return;
        }

        setP2pVersionCheck( 0 );
    }


    public void setRhVersionCheck( final String from, final String to )
    {
        if ( !checkOutdatedVersion( this.rhVersion, to ) )
        {
            setRhVersionCheck( checkOutdatedVersion( this.rhVersion, from ) ? 1 : 2 );
            return;
        }

        setRhVersionCheck( 0 );
    }


    private boolean checkOutdatedVersion( String version, String compareVersion )
    {
        //        version

        Pattern p = Pattern.compile( "((\\d+)[\\.\\-])*((RC)|(SNAPSHOT))?(\\d*)" );
        Matcher m = p.matcher( version );

        String curVer = "";
        String compVersion = "";

        if ( m.find() )
        {
            if ( Strings.isNullOrEmpty( m.group( 0 ) ) )
            {
                return false;
            }

            curVer = m.group( 0 );
        }

        m = p.matcher( compareVersion );

        if ( m.find() )
        {
            if ( Strings.isNullOrEmpty( m.group( 0 ) ) )
            {
                return false;
            }

            compVersion = m.group( 0 );
        }

        String[] curVerAr = curVer.split( "\\." );

        String[] compVersionAr = compVersion.split( "\\." );
        int curVersionArIt = 0;

        for ( String lex : compVersionAr )
        {
            if ( curVersionArIt == curVerAr.length )
            {
                return false;
            }
            if ( !compareLexem( curVerAr[curVersionArIt++], lex ) )
            {
                return false;
            }
        }

        return true;
    }


    private boolean compareLexem( String lex1, String lex2 )
    {
        lex1 = lex1.replace( "RC", "" ).replace( "SNAPSHOT", "" );
        lex2 = lex2.replace( "RC", "" ).replace( "SNAPSHOT", "" );

        String[] lex1Ar = lex1.split( "-" );
        String[] lex2Ar = lex2.split( "-" );

        int j = 0;

        for ( int i = 0; i < lex1Ar.length; i++ )
        {
            if ( j == lex2Ar.length )
            {
                return false;
            }
            if ( Integer.parseInt( lex1Ar[i] ) < Integer.parseInt( lex2Ar[j++] ) )
            {
                return false;
            }
        }

        if ( lex1Ar.length < lex2Ar.length )
        {
            return false;
        }

        return true;
    }
}
