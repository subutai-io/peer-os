package io.subutai.core.kurjun.api;


import java.util.concurrent.TimeUnit;


/**
 * Interface that exposes methods to set repository quota values.
 *
 */
public interface QuotaManagedRepository
{

    /**
     * Sets disk quota for the repository.
     *
     * @param size disk quota value in MB
     * @param context context for which to set quota; pass {@code null} if not applicable
     * @return {@code true} if quota value successfully sey; {@code false} otherwise
     */
    boolean setDiskQuota( int size, String context );


    /**
     * Sets transfer quota for the repository.
     *
     * @param threshold transfer quota value in MB
     * @param timeFrame time frame for the supplied threshold value
     * @param timeUnit unit of the time frame
     * @param context context for which to set quota; pass {@code null} if not applicable
     * @return {@code true} if quota value successfully sey; {@code false} otherwise
     */
    boolean setTransferQuota( int threshold, int timeFrame, TimeUnit timeUnit, String context );

}

