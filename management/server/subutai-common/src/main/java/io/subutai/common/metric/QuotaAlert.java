package io.subutai.common.metric;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;


/**
 * Resource alert value
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class QuotaAlert extends AbstractAlert<QuotaAlertValue> implements Alert
{
    public QuotaAlert( @JsonProperty( "alert" ) final QuotaAlertValue alertValue,
                       @JsonProperty( "createdTime" ) Long createdTime )
    {
        super(alertValue.getValue().getHostId(), alertValue);
        Preconditions.checkNotNull( createdTime );
        Preconditions.checkNotNull( alertValue );
        Preconditions.checkNotNull( alertValue.getValue() );
        Preconditions.checkNotNull( alertValue.getValue().getHostId() );
        Preconditions.checkNotNull( alertValue.getValue().getContainerResourceType() );
        Preconditions.checkNotNull( alertValue.getValue().getCurrentValue() );
        Preconditions.checkNotNull( alertValue.getValue().getQuotaValue() );
        this.createdTime = createdTime;
    }


    @JsonIgnore
    @Override
    public String getId()
    {
        return getHostId() + ":" + alert.getValue().getContainerResourceType();
    }


//    @JsonIgnore
//    @Override
//    public AlertType getType()
//    {
//        return AlertType.ENVIRONMENT_ALERT;
//    }
}
