package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;


public class AdvancedSettingsPojo implements AdvancedSettings
{
    private String karafLogs;


    public String getKarafLogs()
    {
        return karafLogs;
    }


    public void setKarafLogs( final String karafLogs )
    {
        this.karafLogs = karafLogs;
    }
}
