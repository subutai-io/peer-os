package io.subutai.common.quota;


public abstract class QuotaInfo
{
    public abstract String getQuotaKey();

    public abstract String getQuotaValue();

    public abstract QuotaType getQuotaType();


    @Override
    public String toString()
    {
        return getQuotaKey() + ": " + getQuotaValue();
    }
}
