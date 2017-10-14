package io.subutai.core.systemmanager.impl.pojo;


import java.util.Set;

import com.google.common.collect.Sets;

import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;


public class AdvancedSettingsPojo implements AdvancedSettings
{
    private String karafLogs;

    private Set<String> karafLogFiles = Sets.newHashSet();


    public Set<String> getKarafLogFiles()
    {
        return karafLogFiles;
    }


    public void setKarafLogFiles( final Set<String> karafLogFiles )
    {
        this.karafLogFiles = karafLogFiles;
    }


    public String getKarafLogs()
    {
        return karafLogs;
    }


    public void setKarafLogs( final String karafLogs )
    {
        this.karafLogs = karafLogs;
    }
}
