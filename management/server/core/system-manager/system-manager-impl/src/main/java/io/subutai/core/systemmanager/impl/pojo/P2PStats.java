package io.subutai.core.systemmanager.impl.pojo;


public class P2PStats
{
    private String rhId;
    private String rhVersion;
    private String p2pVersion;
    private int rhVersionCheck;
    private int p2pVersionCheck;
    private int p2pStatus;
    private String p2pErrorMessage;


    public P2PStats( String rhId )
    {
        this.rhId = rhId;
        this.rhVersion = "Connection problem";
        this.p2pVersion = "Connection problem";
        this.rhVersionCheck = 2;
        this.p2pVersionCheck = 2;
        this.p2pStatus = 2;
        this.p2pErrorMessage = "";
    }


    public P2PStats( String rhId, String rhVersion, String p2pVersion, String p2pErrorMessage )
    {
        this.rhId = rhId;
        this.rhVersion = rhVersion;
        this.p2pVersion = p2pVersion;

        p2pStatus = p2pStatusAnalysis( p2pErrorMessage );

        this.p2pVersionCheck = p2pVersionCheck();
        this.rhVersionCheck = rhVersionCheck();

        this.p2pErrorMessage = p2pErrorMessage;
    }


    public String getRhId()
    {
        return rhId;
    }


    public String getRhVersion()
    {
        return rhVersion;
    }


    public String getP2pVersion()
    {
        return p2pVersion;
    }


    private int p2pVersionCheck()
    {
        // @todo add method checking version
        return 1;
    }


    private int rhVersionCheck()
    {
        // @todo add method checking version
        return 1;
    }


    private int p2pStatusAnalysis( String log )
    {
        StringBuilder output = new StringBuilder();
        int errCnt = 0;
        int statCnt = 0;
        for ( String row : log.split( "\\r?\\n" ) )
        {
            if ( row.contains( "Status" ) && row.contains( "LastError" ) )
            {
                errCnt++;
            }
            if ( row.contains( "Status" ) )
            {
                statCnt++;

                output.append( row );
            }
        }

        this.p2pErrorMessage = output.toString();

        if ( errCnt > 0 )
        {
            return errCnt < statCnt ? 1 : 2;
        }

        return 0;
    }
}
