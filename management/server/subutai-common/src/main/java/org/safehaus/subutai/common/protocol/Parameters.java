package org.safehaus.subutai.common.protocol;


import java.util.List;

import com.google.gson.annotations.SerializedName;


/**
 * Created by daralbaev on 7/20/14.
 */
public class Parameters
{
    @SerializedName("parameters")
    public List<Setting> parameters;
}
