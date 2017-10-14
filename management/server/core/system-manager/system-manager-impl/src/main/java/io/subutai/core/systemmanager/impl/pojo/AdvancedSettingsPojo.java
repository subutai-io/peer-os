package io.subutai.core.systemmanager.impl.pojo;


import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;


public class AdvancedSettingsPojo implements AdvancedSettings
{
    private String karafLogs;

    private List<String> karafLogFiles = Lists.newArrayList();


    public List<String> getKarafLogFiles()
    {
        return karafLogFiles;
    }


    public void setKarafLogFiles( final List<String> karafLogFiles )
    {
        Preconditions.checkNotNull( karafLogFiles );

        this.karafLogFiles = karafLogFiles;
    }


    public String getKarafLogs()
    {
        return karafLogs;
    }


    public void setKarafLogs( final String karafLogs )
    {
        Preconditions.checkNotNull( karafLogs );

        this.karafLogs = karafLogs;
    }
}
