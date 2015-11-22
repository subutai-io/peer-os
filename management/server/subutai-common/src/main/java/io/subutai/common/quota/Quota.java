package io.subutai.common.quota;


public abstract class Quota
{
    public abstract String getKey();

    public abstract String getValue();

    public abstract QuotaType getType();


    @Override
    public String toString()
    {
        return getKey() + ": " + getValue();
    }
}
