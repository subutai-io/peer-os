package io.subutai.core.environment.metadata.impl;


public class BrokerSettingException extends Exception
{

    public BrokerSettingException( final String message )
    {
        super( message );
    }


    public BrokerSettingException( final Exception e )
    {
        super( e );
    }
}
