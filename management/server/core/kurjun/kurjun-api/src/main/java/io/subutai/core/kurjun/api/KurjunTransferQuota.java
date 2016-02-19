package io.subutai.core.kurjun.api;


import java.util.concurrent.TimeUnit;


/**
 * Pojo to hold transfer quota info for repositories.
 *
 */
public class KurjunTransferQuota
{

    private long threshold;
    private long timeFrame;
    private TimeUnit timeUnit = TimeUnit.HOURS;


    public KurjunTransferQuota()
    {
    }


    public KurjunTransferQuota( long threshold, long timeFrame, TimeUnit timeUnit )
    {
        this.threshold = threshold;
        this.timeFrame = timeFrame;
        this.timeUnit = timeUnit;
    }


    /**
     * Gets transfer quota value in MB.
     *
     * @return
     */
    public long getThreshold()
    {
        return threshold;
    }


    /**
     * Sets transfer quota value in MB.
     *
     * @param threshold
     */
    public void setThreshold( long threshold )
    {
        this.threshold = threshold;
    }


    /**
     * Gets time frame within which threshold value should not exceed.
     *
     * @return
     */
    public long getTimeFrame()
    {
        return timeFrame;
    }


    /**
     * Sets time frame within which threshold value should not exceed.
     *
     * @param timeFrame
     */
    public void setTimeFrame( long timeFrame )
    {
        this.timeFrame = timeFrame;
    }


    /**
     * Unit of the time frame for this quota.
     *
     * @return
     */
    public TimeUnit getTimeUnit()
    {
        return timeUnit;
    }


    /**
     * Sets unit of time frame.
     *
     * @param timeUnit
     */
    public void setTimeUnit( TimeUnit timeUnit )
    {
        this.timeUnit = timeUnit;
    }


}

