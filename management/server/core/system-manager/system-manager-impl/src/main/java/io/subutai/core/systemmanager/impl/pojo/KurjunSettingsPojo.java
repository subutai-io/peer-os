package io.subutai.core.systemmanager.impl.pojo;


import java.util.concurrent.TimeUnit;

import io.subutai.core.systemmanager.api.pojo.KurjunSettings;


/**
 * Created by ermek on 2/6/16.
 */
public class KurjunSettingsPojo implements KurjunSettings
{
    private String globalKurjunUrls;
    private Long publicDiskQuota;
    private Long publicThreshold;
    private Long publicTimeFrame;
    private TimeUnit publicTimeUnit;
    private Long trustDiskQuota;
    private Long trustThreshold;
    private Long trustTimeFrame;
    private TimeUnit trustTimeUnit;


    public String getGlobalKurjunUrls()
    {
        return globalKurjunUrls;
    }


    public void setGlobalKurjunUrls( final String globalKurjunUrls )
    {
        this.globalKurjunUrls = globalKurjunUrls;
    }


    public Long getPublicDiskQuota()
    {
        return publicDiskQuota;
    }


    public void setPublicDiskQuota( final Long publicDiskQuota )
    {
        this.publicDiskQuota = publicDiskQuota;
    }


    public Long getPublicThreshold()
    {
        return publicThreshold;
    }


    public void setPublicThreshold( final Long publicThreshold )
    {
        this.publicThreshold = publicThreshold;
    }


    public Long getPublicTimeFrame()
    {
        return publicTimeFrame;
    }


    public void setPublicTimeFrame( final Long publicTimeFrame )
    {
        this.publicTimeFrame = publicTimeFrame;
    }


    public TimeUnit getPublicTimeUnit()
    {
        return publicTimeUnit;
    }


    public void setPublicTimeUnit( final TimeUnit publicTimeUnit )
    {
        this.publicTimeUnit = publicTimeUnit;
    }


    public Long getTrustDiskQuota()
    {
        return trustDiskQuota;
    }


    public void setTrustDiskQuota( final Long trustDiskQuota )
    {
        this.trustDiskQuota = trustDiskQuota;
    }


    public Long getTrustThreshold()
    {
        return trustThreshold;
    }


    public void setTrustThreshold( final Long trustThreshold )
    {
        this.trustThreshold = trustThreshold;
    }


    public Long getTrustTimeFrame()
    {
        return trustTimeFrame;
    }


    public void setTrustTimeFrame( final Long trustTimeFrame )
    {
        this.trustTimeFrame = trustTimeFrame;
    }


    public TimeUnit getTrustTimeUnit()
    {
        return trustTimeUnit;
    }


    public void setTrustTimeUnit( final TimeUnit trustTimeUnit )
    {
        this.trustTimeUnit = trustTimeUnit;
    }
}
