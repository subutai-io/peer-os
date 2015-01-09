package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 12/3/14.
 */
public abstract class QuotaInfo
{
    public abstract String getQuotaKey();

    public abstract String getQuotaValue();


    @Override
    public String toString()
    {
        return getQuotaKey() + ": " + getQuotaValue();
    }
}
