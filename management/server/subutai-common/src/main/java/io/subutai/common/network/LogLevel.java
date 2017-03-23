package io.subutai.common.network;


public enum LogLevel
{
    EMERGENCY( "emerg" ), ALERT( "alert" ), CRITICAL( "crit" ), ERROR( "err" ), WARNING( "warn" ), NOTICE( "notice" ),
    INFO( "info" ), DEBUG( "debug" ), ALL( "" );

    private String cliParam;


    LogLevel( final String cliParam )
    {
        this.cliParam = cliParam;
    }


    public String getCliParam()
    {
        return cliParam;
    }
}
