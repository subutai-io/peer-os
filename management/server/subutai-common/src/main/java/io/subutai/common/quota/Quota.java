package io.subutai.common.quota;


import org.codehaus.jackson.annotate.JsonIgnore;


public abstract class Quota
{
    @JsonIgnore
    public abstract String getKey();

    @JsonIgnore
    public abstract String getValue();

    @JsonIgnore
    public abstract QuotaType getType();


    @Override
    public String toString()
    {
        return getKey() + ": " + getValue();
    }
}
