package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;


public class AdvancedSettingsPojo implements AdvancedSettings
{
    private String karafLogs;

    @Override
    public String getKarafLogs()
    {
        return karafLogs;
    }

    @Override
    public void setKarafLogs( final String karafLogs )
    {
        this.karafLogs = karafLogs;
    }
}
