package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

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
        Preconditions.checkNotNull( alertValue.getValue().getResourceType() );
        Preconditions.checkNotNull( alertValue.getValue().getCurrentValue() );
        Preconditions.checkNotNull( alertValue.getValue().getQuotaValue() );
        this.createdTime = createdTime;
    }


    @JsonIgnore
    @Override
    public String getId()
    {
        return getHostId() + ":" + alert.getValue().getResourceType();
    }


//    @JsonIgnore
//    @Override
//    public AlertType getType()
//    {
//        return AlertType.ENVIRONMENT_ALERT;
//    }
}
