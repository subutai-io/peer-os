package io.subutai.core.kurjun.api;


/**
 * Interface that exposes methods to set repository quota values.
 *
 */
public interface QuotaManagedRepository
{

    /**
     * Gets disk quota value in MB for the supplied context of the repository. If context is not applicable to the
     * repository then context value is ignored.
     *
     * @param context context to get transfer quota for, if applicable; ignored if n/a
     * @return the Integer
     */
    Long getDiskQuota( String context );


    /**
     * Sets disk quota for the repository.
     *
     * @param size disk quota value in MB
     * @param context context for which to set quota; pass {@code null} if not applicable
     * @return {@code true} if quota value successfully sey; {@code false} otherwise
     */
    boolean setDiskQuota( long size, String context );


    /**
     * Gets transfer quota info for the supplied context of the repository. If context is not applicable to the
     * repository then context value is ignored.
     *
     * @param context context to get transfer quota for, if applicable; ignored if n/a
     * @return
     */
    KurjunTransferQuota getTransferQuota( String context );


    /**
     * Sets transfer quota for the repository.
     *
     * @param quota transfer quota info
     * @param context context for which to set quota; pass {@code null} if not applicable
     * @return the boolean
     */
    boolean setTransferQuota( KurjunTransferQuota quota, String context );

}

