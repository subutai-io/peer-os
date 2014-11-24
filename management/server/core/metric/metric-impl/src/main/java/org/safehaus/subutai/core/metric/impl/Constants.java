package org.safehaus.subutai.core.metric.impl;


public class Constants
{

    private Constants()
    {
    }


    //max length of subscriber id to store in database varchar(100) field
    public static final int MAX_SUBSCRIBER_ID_LEN = 100;

    //metric request timeout in seconds
    public static final int METRIC_REQUEST_TIMEOUT = 60;

    //notifyOnAlert timeout in seconds
    public static final int ALERT_TIMEOUT = 30;
}
