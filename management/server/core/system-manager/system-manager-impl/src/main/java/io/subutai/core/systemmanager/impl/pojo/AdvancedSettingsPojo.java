package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;


/**
 * Created by ermek on 2/26/16.
 */
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
