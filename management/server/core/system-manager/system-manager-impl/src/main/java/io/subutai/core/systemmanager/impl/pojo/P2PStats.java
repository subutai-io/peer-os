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

    public P2PStats(String rhId) {
        this.rhId = rhId;
        this.rhVersion = "Connection problem";
        this.p2pVersion = "Connection problem";
    }

    // @todo temporary
    public P2PStats(String rhId, String rhVersion, String p2pVersion) {
        this.rhId = rhId;
        this.rhVersion = rhVersion;
        this.p2pVersion = p2pVersion;

        this.p2pVersionCheck = 1;
        this.p2pVersionCheck = 1;

        this.p2pStatus = 0;
        this.p2pErrorMessage = "Error message";
    }

    public P2PStats(String rhId, String rhVersion, String p2pVersion, int p2pStatus, String p2pErrorMessage) {
        this.rhId = rhId;
        this.rhVersion = rhVersion;
        this.p2pVersion = p2pVersion;
        this.p2pStatus = p2pStatus;

        this.p2pVersionCheck = p2pVersionCheck();
        this.rhVersionCheck = rhVersionCheck();

        this.p2pErrorMessage = p2pErrorMessage;
    }

    public String getRhId() {
        return rhId;
    }

    public String getRhVersion() {
        return rhVersion;
    }

    public String getP2pVersion() {
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
}
