package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.KurjunSettings;


/**
 * Created by ermek on 2/6/16.
 */
public class KurjunSettingsPojo implements KurjunSettings
{
    private String globalKurjunUrls;


    public String getGlobalKurjunUrls()
    {
        return globalKurjunUrls;
    }


    public void setGlobalKurjunUrls( final String globalKurjunUrls )
    {
        this.globalKurjunUrls = globalKurjunUrls;
    }
}
