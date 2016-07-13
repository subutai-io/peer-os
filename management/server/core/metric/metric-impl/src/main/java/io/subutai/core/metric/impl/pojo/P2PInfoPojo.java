package io.subutai.core.metric.impl.pojo;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.subutai.core.metric.api.pojo.P2Pinfo;


public class P2PInfoPojo implements P2Pinfo
{
    private String rhId;
    private String rhVersion;
    private String p2pVersion;
    private int p2pStatus;
    private int p2pVersionCheck;
    private int rhVersionCheck;
    private List<String> state;
    private List<String> p2pErrorLogs;


    public String getRhId()
    {
        return rhId;
    }


    public void setRhId( final String rhId )
    {
        this.rhId = rhId;
    }


    public String getRhVersion()
    {
        return rhVersion;
    }


    public void setRhVersion( final String rhVersion )
    {
        this.rhVersion = rhVersion;
    }


    public int getP2pStatus()
    {
        return p2pStatus;
    }


    public void setP2pStatus( final int p2pStatus )
    {
        this.p2pStatus = p2pStatus;
    }


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


    public int getRhVersionCheck()
    {
        return rhVersionCheck;
    }


    public void setRhVersionCheck( final int rhVersionCheck )
    {
        this.rhVersionCheck = rhVersionCheck;
    }


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
        if( !checkOutdatedVersion( this.p2pVersion, to ))
        {
            setP2pVersionCheck( checkOutdatedVersion( this.p2pVersion, from ) ? 1 : 2 );
            return;
        }

        setP2pVersionCheck(0);
    }

    public void setRhVersionCheck( final String from, final String to )
    {
        if( !checkOutdatedVersion( this.rhVersion, to ))
        {
            setRhVersionCheck( checkOutdatedVersion( this.rhVersion, from ) ? 1 : 2 );
            return;
        }

        setRhVersionCheck(0);
    }

    private boolean checkOutdatedVersion( String version, String compVersion )
    {
//        version

        Pattern p = Pattern.compile("((\\d+)[\\.\\-])*(RC)?(\\d*)");
        Matcher m = p.matcher(version);

        String curVer = "";

        if( m.find() )
        {
            if( m.group(0) == null )
                return false;

            curVer = m.group(0);
        }

        m = p.matcher(compVersion);

        if( m.find() )
        {
            if( m.group(0) == null )
                return false;

            compVersion = m.group(0);
        }

        String[] curVerAr = curVer.split(".");

        String[] compVersionAr = curVer.split(".");
        int curVersionArIt = 0;

        for( String lex : compVersionAr)
        {
            if( curVersionArIt == curVerAr.length ) return false;
            if( !compareLexem( curVerAr[curVersionArIt++], lex )) return false;
        }

        return true;
    }

    private boolean compareLexem( String lex1, String lex2 )
    {
        lex1 = lex1.replace( "RC", "" );
        lex2 = lex2.replace( "RC", "" );

        String[] lex1Ar = lex1.split("-");
        String[] lex2Ar = lex2.split("-");

        for( int i = 0; i < lex1Ar.length; i++ )
        {
            if( Integer.parseInt( lex1Ar[i] ) < Integer.parseInt( lex2Ar[i] ) ) return false;
        }

        if( lex1Ar.length < lex2Ar.length ) return false;

        return true;
    }
}
