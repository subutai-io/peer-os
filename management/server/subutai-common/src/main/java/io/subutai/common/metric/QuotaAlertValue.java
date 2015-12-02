package io.subutai.common.metric;


import com.google.common.base.Preconditions;


/**
 * Quota alert class
 */
public class QuotaAlertValue implements AlertValue<ExceededQuota>
{

    private ExceededQuota value;


    public QuotaAlertValue( final ExceededQuota value )
    {
        this.value = value;
    }


    @Override
    public ExceededQuota getValue()
    {
        return value;
    }


    @Override
    public boolean validate()
    {
        try
        {
            Preconditions.checkNotNull( value, "Value is null" );
            Preconditions.checkNotNull( value.getHostId(), "Host id is null" );
            Preconditions.checkNotNull( value.getHostId().getId(), "Host id is null" );
            Preconditions.checkNotNull( value.getResourceType(), "Resource type is null" );
            Preconditions.checkNotNull( value.getCurrentValue(), "Current value is null" );
            Preconditions.checkNotNull( value.getQuotaValue(), "Quota value is null" );
        }
        catch ( Exception e )
        {
            return false;
        }
        return true;
    }
}
