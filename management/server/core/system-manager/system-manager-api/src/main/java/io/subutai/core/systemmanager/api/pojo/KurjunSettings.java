package io.subutai.core.systemmanager.api.pojo;


import java.util.concurrent.TimeUnit;


/**
 * Created by ermek on 2/6/16.
 */
public interface KurjunSettings
{
    public String getGlobalKurjunUrls();

    public void setGlobalKurjunUrls( final String globalKurjunUrls );

    public Long getPublicDiskQuota();


    public void setPublicDiskQuota( final Long publicDiskQuota );


    public Long getPublicThreshold();


    public void setPublicThreshold( final Long publicThreshold );


    public Long getPublicTimeFrame();


    public void setPublicTimeFrame( final Long publicTimeFrame );


    public TimeUnit getPublicTimeUnit();


    public void setPublicTimeUnit( final TimeUnit publicTimeUnit );


    public Long getTrustDiskQuota();


    public void setTrustDiskQuota( final Long trustDiskQuota );


    public Long getTrustThreshold();


    public void setTrustThreshold( final Long trustThreshold );


    public Long getTrustTimeFrame();


    public void setTrustTimeFrame( final Long trustTimeFrame );


    public TimeUnit getTrustTimeUnit();


    public void setTrustTimeUnit( final TimeUnit trustTimeUnit );
}
