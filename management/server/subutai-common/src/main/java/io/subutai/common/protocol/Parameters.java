package io.subutai.common.protocol;


import java.util.List;

import com.google.gson.annotations.SerializedName;


public class Parameters
{
    @SerializedName( "parameters" )
    public List<Setting> parameters;
}
