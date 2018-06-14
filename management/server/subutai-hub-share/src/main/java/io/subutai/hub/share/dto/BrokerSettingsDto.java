package io.subutai.hub.share.dto;


import java.util.List;

@Deprecated
public class BrokerSettingsDto
{
    private List<String> brokers;


    public BrokerSettingsDto( final List<String> brokers )
    {
        this.brokers = brokers;
    }


    public BrokerSettingsDto()
    {
    }


    public List<String> getBrokers()
    {
        return brokers;
    }
}
